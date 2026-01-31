// User API - Session-based Authentication (Redis Session)
import { api, useMock, type ApiResponse } from './api.js';
import type {
	User,
	LoginRequest,
	SignupRequest,
	SignUpResponse,
	LoginResponse
} from '$lib/types/user.js';
import { getMockUser } from '$lib/mock/user.js';

// API Endpoints (API Gateway를 통한 요청)
const ENDPOINTS = {
	signup: '/user-service/api/v1/users/signup',
	login: '/user-service/api/v1/users/login',
	logout: '/user-service/api/v1/users/logout',
	me: '/user-service/api/v1/users/me'
};

/**
 * 회원가입
 * @param data 회원가입 데이터 (email, username, password)
 * @returns SignUpResponse (userId, email, username)
 */
export async function signup(data: SignupRequest): Promise<ApiResponse<SignUpResponse>> {
	if (useMock()) {
		const mockResponse: SignUpResponse = {
			userId: Math.floor(Math.random() * 10000),
			username: data.username,
			email: data.email
		};
		return {
			success: true,
			data: mockResponse,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.post<SignUpResponse>(ENDPOINTS.signup, data);
}

/**
 * 로그인 (Session 생성)
 * @param credentials 로그인 정보 (email, password)
 * @returns LoginResponse (userId, email, username, role, sessionId)
 * 
 * ⭐ Session Cookie는 브라우저가 자동으로 저장/관리
 */
export async function login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
	if (useMock()) {
		const mockUser = getMockUser();
		const mockResponse: LoginResponse = {
			userId: Number(mockUser.userId) || 1,
			username: mockUser.username,
			email: mockUser.email,
			role: 'ROLE_USER',
			sessionId: 'mock-session-id'
		};
		return {
			success: true,
			data: mockResponse,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	// Cookie는 credentials: 'include'로 자동 전송/저장
	return api.post<LoginResponse>(ENDPOINTS.login, credentials);
}

/**
 * 로그아웃 (Session 무효화)
 * @returns void
 * 
 * ⭐ Backend에서 Redis Session 삭제, Cookie 만료
 */
export async function logout(): Promise<ApiResponse<void>> {
	if (useMock()) {
		return {
			success: true,
			data: null,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	// Cookie는 credentials: 'include'로 자동 전송
	return api.post<void>(ENDPOINTS.logout);
}

/**
 * 현재 사용자 정보 조회
 * @returns User (userId, email, username, role)
 * 
 * ⭐ Session Cookie가 자동으로 전송되어 인증됨
 */
export async function getCurrentUser(): Promise<ApiResponse<User>> {
	if (useMock()) {
		const mockUser = getMockUser();
		const user: User = {
			userId: Number(mockUser.userId) || 1,
			username: mockUser.username,
			email: mockUser.email,
			role: 'ROLE_USER'
		};
		return {
			success: true,
			data: user,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	// Cookie는 credentials: 'include'로 자동 전송
	return api.get<User>(ENDPOINTS.me);
}
