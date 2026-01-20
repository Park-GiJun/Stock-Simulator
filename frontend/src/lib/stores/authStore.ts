// Auth Store

import { writable, derived } from 'svelte/store';
import type { User, AuthState } from '$lib/types/user.js';

const initialState: AuthState = {
	isAuthenticated: false,
	user: null,
	token: null,
	isLoading: false
};

function createAuthStore() {
	const { subscribe, set, update } = writable<AuthState>(initialState);

	return {
		subscribe,

		login: (user: User, token: string) => {
			set({
				isAuthenticated: true,
				user,
				token,
				isLoading: false
			});
			// Store token in localStorage
			if (typeof localStorage !== 'undefined') {
				localStorage.setItem('auth_token', token);
				localStorage.setItem('auth_user', JSON.stringify(user));
			}
		},

		logout: () => {
			set(initialState);
			if (typeof localStorage !== 'undefined') {
				localStorage.removeItem('auth_token');
				localStorage.removeItem('auth_user');
			}
		},

		setLoading: (isLoading: boolean) => {
			update((state) => ({ ...state, isLoading }));
		},

		updateUser: (userData: Partial<User>) => {
			update((state) => ({
				...state,
				user: state.user ? { ...state.user, ...userData } : null
			}));
		},

		updateCapital: (capital: number) => {
			update((state) => ({
				...state,
				user: state.user ? { ...state.user, capital } : null
			}));
		},

		initialize: () => {
			if (typeof localStorage !== 'undefined') {
				const token = localStorage.getItem('auth_token');
				const userStr = localStorage.getItem('auth_user');

				if (token && userStr) {
					try {
						const user = JSON.parse(userStr) as User;
						set({
							isAuthenticated: true,
							user,
							token,
							isLoading: false
						});
					} catch {
						set(initialState);
					}
				}
			}
		},

		reset: () => set(initialState)
	};
}

export const authStore = createAuthStore();

// Derived stores for convenience
export const isAuthenticated = derived(authStore, ($auth) => $auth.isAuthenticated);
export const currentUser = derived(authStore, ($auth) => $auth.user);
export const userCapital = derived(authStore, ($auth) => $auth.user?.capital ?? 0);
