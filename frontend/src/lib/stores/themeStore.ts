// Theme Store

import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';

export type Theme = 'light' | 'dark';

function getInitialTheme(): Theme {
	if (browser) {
		const saved = localStorage.getItem('theme') as Theme | null;
		if (saved && (saved === 'light' || saved === 'dark')) {
			return saved;
		}
		// Check system preference
		if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
			return 'dark';
		}
	}
	return 'light';
}

function createThemeStore() {
	const { subscribe, set, update } = writable<Theme>(getInitialTheme());

	return {
		subscribe,

		toggle: () => {
			update((current) => {
				const next = current === 'light' ? 'dark' : 'light';
				if (browser) {
					localStorage.setItem('theme', next);
					document.documentElement.setAttribute('data-theme', next);
				}
				return next;
			});
		},

		setTheme: (theme: Theme) => {
			set(theme);
			if (browser) {
				localStorage.setItem('theme', theme);
				document.documentElement.setAttribute('data-theme', theme);
			}
		},

		initialize: () => {
			const theme = getInitialTheme();
			set(theme);
			if (browser) {
				document.documentElement.setAttribute('data-theme', theme);
			}
		}
	};
}

export const themeStore = createThemeStore();
export const isDarkMode = derived(themeStore, ($theme) => $theme === 'dark');
