// User API - Authentication and user related API functions
import { api, useMock, type ApiResponse } from './api.js';
import type { User, UserProfile } from '$lib/types/user.js';
import { getMockUser } from '$lib/mock/user.js';
import { browser } from '$app/environment';

// Types
export interface LoginRequest {
	email: string;
	password: string;
}

export interface RegisterRequest {
	email: string;
	password: string;
	username: string;
}

export interface AuthResponse {
	user: User;
	accessToken: string;
	refreshToken: string;
}

export interface UpdateProfileRequest {
	username?: string;
	avatarUrl?: string;
}

// API Endpoints
const ENDPOINTS = {
	login: '/api/auth/login',
	register: '/api/auth/register',
	logout: '/api/auth/logout',
	refresh: '/api/auth/refresh',
	me: '/api/users/me',
	profile: '/api/users/profile',
	updateProfile: '/api/users/profile'
};

// Token management
export function setTokens(accessToken: string, refreshToken: string): void {
	if (!browser) return;
	localStorage.setItem('accessToken', accessToken);
	localStorage.setItem('refreshToken', refreshToken);
}

export function clearTokens(): void {
	if (!browser) return;
	localStorage.removeItem('accessToken');
	localStorage.removeItem('refreshToken');
}

export function getAccessToken(): string | null {
	if (!browser) return null;
	return localStorage.getItem('accessToken');
}

// API Functions
export async function login(credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> {
	if (useMock()) {
		const mockUser = getMockUser();
		const response: AuthResponse = {
			user: mockUser,
			accessToken: 'mock-access-token',
			refreshToken: 'mock-refresh-token'
		};
		setTokens(response.accessToken, response.refreshToken);
		return {
			success: true,
			data: response,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	const response = await api.post<AuthResponse>(ENDPOINTS.login, credentials);
	if (response.success && response.data) {
		setTokens(response.data.accessToken, response.data.refreshToken);
	}
	return response;
}

export async function register(data: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
	if (useMock()) {
		const mockUser: User = {
			userId: `user-${Date.now()}`,
			username: data.username,
			email: data.email,
			capital: 5000000,
			initialCapital: 5000000,
			createdAt: new Date().toISOString()
		};
		const response: AuthResponse = {
			user: mockUser,
			accessToken: 'mock-access-token',
			refreshToken: 'mock-refresh-token'
		};
		setTokens(response.accessToken, response.refreshToken);
		return {
			success: true,
			data: response,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	const response = await api.post<AuthResponse>(ENDPOINTS.register, data);
	if (response.success && response.data) {
		setTokens(response.data.accessToken, response.data.refreshToken);
	}
	return response;
}

export async function logout(): Promise<ApiResponse<void>> {
	clearTokens();

	if (useMock()) {
		return {
			success: true,
			data: null,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.post<void>(ENDPOINTS.logout);
}

export async function getCurrentUser(): Promise<ApiResponse<User>> {
	if (useMock()) {
		const token = getAccessToken();
		if (!token) {
			return {
				success: false,
				data: null,
				error: '로그인이 필요합니다.',
				timestamp: new Date().toISOString()
			};
		}
		return {
			success: true,
			data: getMockUser(),
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<User>(ENDPOINTS.me);
}

export async function getProfile(): Promise<ApiResponse<UserProfile>> {
	if (useMock()) {
		const user = getMockUser();
		const profile: UserProfile = {
			...user,
			investorType: 'USER',
			rank: 15,
			returnRate: 17.0
		};
		return {
			success: true,
			data: profile,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<UserProfile>(ENDPOINTS.profile);
}

export async function updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<User>> {
	if (useMock()) {
		const user = getMockUser();
		return {
			success: true,
			data: { ...user, ...data },
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.patch<User>(ENDPOINTS.updateProfile, data);
}

export async function refreshToken(): Promise<ApiResponse<AuthResponse>> {
	if (!browser) {
		return {
			success: false,
			data: null,
			error: 'Not in browser',
			timestamp: new Date().toISOString()
		};
	}

	const refreshToken = localStorage.getItem('refreshToken');
	if (!refreshToken) {
		return {
			success: false,
			data: null,
			error: 'No refresh token',
			timestamp: new Date().toISOString()
		};
	}

	if (useMock()) {
		return {
			success: true,
			data: {
				user: getMockUser(),
				accessToken: 'mock-access-token-new',
				refreshToken: 'mock-refresh-token-new'
			},
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	const response = await api.post<AuthResponse>(ENDPOINTS.refresh, { refreshToken });
	if (response.success && response.data) {
		setTokens(response.data.accessToken, response.data.refreshToken);
	}
	return response;
}
