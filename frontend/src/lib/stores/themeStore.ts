// Theme Store

import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';

export type Theme = 'light' | 'dark' | 'system';

function getSystemTheme(): 'light' | 'dark' {
	if (browser && window.matchMedia('(prefers-color-scheme: dark)').matches) {
		return 'dark';
	}
	return 'light';
}

function getEffectiveTheme(theme: Theme): 'light' | 'dark' {
	return theme === 'system' ? getSystemTheme() : theme;
}

function getInitialTheme(): Theme {
	if (browser) {
		const saved = localStorage.getItem('theme') as Theme | null;
		if (saved && (saved === 'light' || saved === 'dark' || saved === 'system')) {
			return saved;
		}
	}
	return 'system';
}

function createThemeStore() {
	const { subscribe, set, update } = writable<Theme>(getInitialTheme());

	function applyTheme(theme: Theme) {
		if (browser) {
			const effectiveTheme = getEffectiveTheme(theme);
			localStorage.setItem('theme', theme);
			document.documentElement.setAttribute('data-theme', effectiveTheme);
		}
	}

	return {
		subscribe,

		toggle: () => {
			update((current) => {
				const effectiveCurrent = getEffectiveTheme(current);
				const next = effectiveCurrent === 'light' ? 'dark' : 'light';
				applyTheme(next);
				return next;
			});
		},

		setTheme: (theme: Theme) => {
			set(theme);
			applyTheme(theme);
		},

		initialize: () => {
			const theme = getInitialTheme();
			set(theme);
			applyTheme(theme);
		}
	};
}

export const themeStore = createThemeStore();
export const theme = themeStore; // Alias for convenience

// Exported functions for direct use
export const toggleTheme = themeStore.toggle;
export const setTheme = themeStore.setTheme;

export const isDarkMode = derived(themeStore, ($theme) => getEffectiveTheme($theme) === 'dark');
