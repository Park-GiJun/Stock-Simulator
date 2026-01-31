import client from 'prom-client';

// Create a Registry
const register = new client.Registry();

// Add default metrics (CPU, memory, event loop, etc.)
client.collectDefaultMetrics({
	register,
	prefix: 'sveltekit_'
});

// Custom metrics

// HTTP request counter
export const httpRequestsTotal = new client.Counter({
	name: 'sveltekit_http_requests_total',
	help: 'Total number of HTTP requests',
	labelNames: ['method', 'route', 'status_code'] as const,
	registers: [register]
});

// Suspicious request counter (separate from normal metrics)
export const suspiciousRequestsTotal = new client.Counter({
	name: 'sveltekit_suspicious_requests_total',
	help: 'Total number of suspicious/malicious requests (e.g., scanning for .env, config files)',
	labelNames: ['method', 'pattern_type'] as const,
	registers: [register]
});

// HTTP request duration histogram
export const httpRequestDuration = new client.Histogram({
	name: 'sveltekit_http_request_duration_seconds',
	help: 'Duration of HTTP requests in seconds',
	labelNames: ['method', 'route', 'status_code'] as const,
	buckets: [0.001, 0.005, 0.015, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1, 2, 5],
	registers: [register]
});

// Active requests gauge
export const activeRequests = new client.Gauge({
	name: 'sveltekit_active_requests',
	help: 'Number of active requests',
	registers: [register]
});

// Page views counter
export const pageViews = new client.Counter({
	name: 'sveltekit_page_views_total',
	help: 'Total page views',
	labelNames: ['route', 'device_type'] as const,
	registers: [register]
});

// API call counter (to backend services)
export const apiCallsTotal = new client.Counter({
	name: 'sveltekit_api_calls_total',
	help: 'Total API calls to backend services',
	labelNames: ['endpoint', 'method', 'status'] as const,
	registers: [register]
});

// Error counter
export const errorsTotal = new client.Counter({
	name: 'sveltekit_errors_total',
	help: 'Total number of errors',
	labelNames: ['type', 'route'] as const,
	registers: [register]
});

// SSR render time
export const ssrRenderDuration = new client.Histogram({
	name: 'sveltekit_ssr_render_duration_seconds',
	help: 'Server-side rendering duration in seconds',
	labelNames: ['route'] as const,
	buckets: [0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5],
	registers: [register]
});

// Export the register for the metrics endpoint
export { register };
