// Auth Store - Session-based Authentication

import { writable, derived } from 'svelte/store';
import type { User, AuthState } from '$lib/types/user.js';

const initialState: AuthState = {
	isAuthenticated: false,
	user: null,
	isLoading: false
};

function createAuthStore() {
	const { subscribe, set, update } = writable<AuthState>(initialState);

	return {
		subscribe,

		/**
		 * 로그인 성공 시 호출
		 * @param user 사용자 정보
		 * 
		 * ⭐ Session은 Cookie로 관리되므로 token 저장 불필요
		 */
		login: (user: User) => {
			set({
				isAuthenticated: true,
				user,
				isLoading: false
			});
			// 사용자 정보만 localStorage에 저장 (UX 개선용, 선택적)
			if (typeof localStorage !== 'undefined') {
				localStorage.setItem('auth_user', JSON.stringify(user));
			}
		},

		/**
		 * 로그아웃 시 호출
		 * 
		 * ⭐ Session Cookie는 Backend에서 만료 처리
		 */
		logout: () => {
			set(initialState);
			if (typeof localStorage !== 'undefined') {
				localStorage.removeItem('auth_user');
			}
		},

		/**
		 * 로딩 상태 설정
		 */
		setLoading: (isLoading: boolean) => {
			update((state) => ({ ...state, isLoading }));
		},

		/**
		 * 사용자 정보 업데이트 (부분 업데이트)
		 */
		updateUser: (userData: Partial<User>) => {
			update((state) => {
				const updatedUser = state.user ? { ...state.user, ...userData } : null;
				
				// localStorage 동기화
				if (typeof localStorage !== 'undefined' && updatedUser) {
					localStorage.setItem('auth_user', JSON.stringify(updatedUser));
				}
				
				return {
					...state,
					user: updatedUser
				};
			});
		},

		/**
		 * 앱 시작 시 localStorage에서 사용자 정보 복원
		 * 
		 * ⭐ Session 유효성은 getCurrentUser() API로 확인 필요
		 */
		initialize: () => {
			if (typeof localStorage !== 'undefined') {
				const userStr = localStorage.getItem('auth_user');

				if (userStr) {
					try {
						const user = JSON.parse(userStr) as User;
						set({
							isAuthenticated: true,
							user,
							isLoading: false
						});
					} catch {
						// JSON 파싱 실패 시 초기화
						set(initialState);
					}
				}
			}
		},

		/**
		 * 스토어 초기화
		 */
		reset: () => set(initialState)
	};
}

export const authStore = createAuthStore();

// Derived stores for convenience
export const isAuthenticated = derived(authStore, ($auth) => $auth.isAuthenticated);
export const currentUser = derived(authStore, ($auth) => $auth.user);
export const isLoading = derived(authStore, ($auth) => $auth.isLoading);
