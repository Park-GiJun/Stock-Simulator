// API Client - Base HTTP client with error handling and interceptors
import { toastStore } from '$lib/stores/toastStore.js';
import { browser } from '$app/environment';

// API Response type matching backend
export interface ApiResponse<T> {
	success: boolean;
	data: T | null;
	error: string | null;
	timestamp: string;
}

// API Error
export class ApiError extends Error {
	constructor(
		public status: number,
		public code: string,
		message: string
	) {
		super(message);
		this.name = 'ApiError';
	}
}

// Request options
interface RequestOptions extends RequestInit {
	params?: Record<string, string | number | boolean | undefined>;
	showError?: boolean;
}

// API Configuration
const config = {
	baseUrl: browser ? (import.meta.env.VITE_API_URL ?? 'http://localhost:9832') : '',
	timeout: 30000,
	useMock: import.meta.env.VITE_USE_MOCK === 'true'
};

// Build URL with query params
function buildUrl(endpoint: string, params?: Record<string, string | number | boolean | undefined>): string {
	const url = new URL(endpoint, config.baseUrl);

	if (params) {
		Object.entries(params).forEach(([key, value]) => {
			if (value !== undefined && value !== null) {
				url.searchParams.append(key, String(value));
			}
		});
	}

	return url.toString();
}

// Get auth token from localStorage
function getAuthToken(): string | null {
	if (!browser) return null;
	return localStorage.getItem('accessToken');
}

// Base fetch wrapper
async function request<T>(
	endpoint: string,
	options: RequestOptions = {}
): Promise<ApiResponse<T>> {
	const { params, showError = true, ...fetchOptions } = options;

	const url = buildUrl(endpoint, params);

	// Default headers
	const headers: HeadersInit = {
		'Content-Type': 'application/json',
		...fetchOptions.headers
	};

	// Add auth header if token exists
	const token = getAuthToken();
	if (token) {
		(headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
	}

	try {
		const controller = new AbortController();
		const timeoutId = setTimeout(() => controller.abort(), config.timeout);

		const response = await fetch(url, {
			...fetchOptions,
			headers,
			signal: controller.signal
		});

		clearTimeout(timeoutId);

		// Parse response
		const data: ApiResponse<T> = await response.json();

		// Handle API errors
		if (!response.ok || !data.success) {
			const error = new ApiError(
				response.status,
				'API_ERROR',
				data.error ?? '알 수 없는 오류가 발생했습니다.'
			);

			if (showError) {
				toastStore.error(error.message);
			}

			throw error;
		}

		return data;
	} catch (error) {
		if (error instanceof ApiError) {
			throw error;
		}

		// Handle network errors
		if (error instanceof Error) {
			if (error.name === 'AbortError') {
				const apiError = new ApiError(408, 'TIMEOUT', '요청 시간이 초과되었습니다.');
				if (showError) toastStore.error(apiError.message);
				throw apiError;
			}

			const apiError = new ApiError(0, 'NETWORK_ERROR', '네트워크 연결을 확인해주세요.');
			if (showError) toastStore.error(apiError.message);
			throw apiError;
		}

		throw error;
	}
}

// HTTP Methods
export const api = {
	get<T>(endpoint: string, options?: RequestOptions): Promise<ApiResponse<T>> {
		return request<T>(endpoint, { ...options, method: 'GET' });
	},

	post<T>(endpoint: string, body?: unknown, options?: RequestOptions): Promise<ApiResponse<T>> {
		return request<T>(endpoint, {
			...options,
			method: 'POST',
			body: body ? JSON.stringify(body) : undefined
		});
	},

	put<T>(endpoint: string, body?: unknown, options?: RequestOptions): Promise<ApiResponse<T>> {
		return request<T>(endpoint, {
			...options,
			method: 'PUT',
			body: body ? JSON.stringify(body) : undefined
		});
	},

	patch<T>(endpoint: string, body?: unknown, options?: RequestOptions): Promise<ApiResponse<T>> {
		return request<T>(endpoint, {
			...options,
			method: 'PATCH',
			body: body ? JSON.stringify(body) : undefined
		});
	},

	delete<T>(endpoint: string, options?: RequestOptions): Promise<ApiResponse<T>> {
		return request<T>(endpoint, { ...options, method: 'DELETE' });
	}
};

// Check if using mock data
export function useMock(): boolean {
	return config.useMock;
}

// Re-export types
export type { RequestOptions };
