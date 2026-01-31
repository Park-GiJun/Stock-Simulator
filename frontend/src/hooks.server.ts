import type { Handle, HandleServerError } from '@sveltejs/kit';
import {
	httpRequestsTotal,
	httpRequestDuration,
	activeRequests,
	pageViews,
	errorsTotal,
	ssrRenderDuration,
	suspiciousRequestsTotal
} from '$lib/server/metrics';

// Patterns for detecting malicious/suspicious requests
const SUSPICIOUS_PATTERNS = {
	env_files: /\.(env|environment|envs|envrc)($|\.)/i,
	config_files: /\.(yml|yaml|json|ini|conf|config|xml|properties)($|\.)/i,
	backup_files: /\.(bak|backup|old|save|sql|tar\.gz|zip)$/i,
	sensitive_paths: /\/(\.aws|\.azure|\.gcloud|\.git|\.docker|\.github|\.gitlab|\.circleci|\.bitbucket)\//i,
	secrets: /(secret|credential|token|password|api[_-]?key|stripe|payment|oauth)/i,
	php_files: /\.(php|asp|aspx|jsp)$/i,
	admin_paths: /\/(admin|administrator|phpinfo|diagnostic|probe|check|test)\.php$/i,
	docker_files: /(docker-compose|Dockerfile|\.dockerignore|\.dockerenv)/i
};

/**
 * Detect if a request is suspicious (security scanning, hacking attempt, etc.)
 */
function isSuspiciousRequest(pathname: string): { suspicious: boolean; patternType?: string } {
	// Check each pattern
	if (SUSPICIOUS_PATTERNS.env_files.test(pathname)) {
		return { suspicious: true, patternType: 'env_files' };
	}
	if (SUSPICIOUS_PATTERNS.config_files.test(pathname)) {
		return { suspicious: true, patternType: 'config_files' };
	}
	if (SUSPICIOUS_PATTERNS.backup_files.test(pathname)) {
		return { suspicious: true, patternType: 'backup_files' };
	}
	if (SUSPICIOUS_PATTERNS.sensitive_paths.test(pathname)) {
		return { suspicious: true, patternType: 'sensitive_paths' };
	}
	if (SUSPICIOUS_PATTERNS.secrets.test(pathname)) {
		return { suspicious: true, patternType: 'secrets' };
	}
	if (SUSPICIOUS_PATTERNS.php_files.test(pathname)) {
		return { suspicious: true, patternType: 'php_files' };
	}
	if (SUSPICIOUS_PATTERNS.admin_paths.test(pathname)) {
		return { suspicious: true, patternType: 'admin_paths' };
	}
	if (SUSPICIOUS_PATTERNS.docker_files.test(pathname)) {
		return { suspicious: true, patternType: 'docker_files' };
	}

	return { suspicious: false };
}

export const handle: Handle = async ({ event, resolve }) => {
	const start = performance.now();
	const { request, url } = event;
	const method = request.method;
	const route = url.pathname;

	// Skip metrics endpoint to avoid recursion
	if (route === '/metrics') {
		return resolve(event);
	}

	// Check if request is suspicious
	const { suspicious, patternType } = isSuspiciousRequest(route);

	// Track suspicious requests separately
	if (suspicious && patternType) {
		suspiciousRequestsTotal.inc({ method, pattern_type: patternType });
		
		// Log suspicious request (optional)
		console.warn('[SECURITY] Suspicious request detected:', {
			method,
			path: route,
			pattern: patternType,
			ip: request.headers.get('x-forwarded-for') || request.headers.get('x-real-ip') || 'unknown',
			userAgent: request.headers.get('user-agent')
		});
		
		// Return early for suspicious requests - don't track in normal metrics
		return new Response('Not Found', { status: 404 });
	}

	// Track active requests
	activeRequests.inc();

	try {
		// Determine device type from route
		const deviceType = route.startsWith('/m/') ? 'mobile' : 'desktop';

		// Track page views (only for GET requests to actual pages, not API routes)
		if (method === 'GET' && !route.startsWith('/api')) {
			pageViews.inc({ route: normalizeRoute(route), device_type: deviceType });
		}

		// Measure SSR render time
		const ssrStart = performance.now();
		const response = await resolve(event);
		const ssrDuration = (performance.now() - ssrStart) / 1000;

		// Track SSR duration for HTML responses
		const contentType = response.headers.get('content-type') || '';
		if (contentType.includes('text/html')) {
			ssrRenderDuration.observe({ route: normalizeRoute(route) }, ssrDuration);
		}

		const statusCode = response.status.toString();
		const duration = (performance.now() - start) / 1000;

		// Track request metrics (only for legitimate requests)
		httpRequestsTotal.inc({
			method,
			route: normalizeRoute(route),
			status_code: statusCode
		});
		httpRequestDuration.observe(
			{
				method,
				route: normalizeRoute(route),
				status_code: statusCode
			},
			duration
		);

		return response;
	} catch (error) {
		const duration = (performance.now() - start) / 1000;

		// Track error
		errorsTotal.inc({
			type: 'unhandled',
			route: normalizeRoute(route)
		});

		httpRequestsTotal.inc({
			method,
			route: normalizeRoute(route),
			status_code: '500'
		});
		httpRequestDuration.observe(
			{
				method,
				route: normalizeRoute(route),
				status_code: '500'
			},
			duration
		);

		throw error;
	} finally {
		activeRequests.dec();
	}
};

export const handleError: HandleServerError = async ({ error, event, status, message }) => {
	const route = event.url.pathname;

	// Track server errors
	errorsTotal.inc({
		type: 'server_error',
		route: normalizeRoute(route)
	});

	console.error('Server error:', { error, status, message, route });

	return {
		message: 'Internal server error'
	};
};

/**
 * Normalize route to prevent high cardinality labels
 * e.g., /stocks/123 -> /stocks/[id]
 */
function normalizeRoute(pathname: string): string {
	// Remove trailing slash
	let route = pathname.endsWith('/') && pathname !== '/' ? pathname.slice(0, -1) : pathname;

	// Replace dynamic segments with placeholders
	// /stocks/123 -> /stocks/[stockId]
	route = route.replace(/\/stocks\/[^/]+/, '/stocks/[stockId]');

	// /users/123 -> /users/[userId]
	route = route.replace(/\/users\/[^/]+/, '/users/[userId]');

	// /news/123 -> /news/[newsId]
	route = route.replace(/\/news\/[^/]+/, '/news/[newsId]');

	// Generic UUID pattern replacement
	route = route.replace(
		/\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/gi,
		'/[uuid]'
	);

	// Generic numeric ID replacement
	route = route.replace(/\/\d+/g, '/[id]');

	return route || '/';
}
