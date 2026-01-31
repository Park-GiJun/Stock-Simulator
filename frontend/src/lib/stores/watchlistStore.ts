// WatchList Store - Manage user's favorite stocks
import { writable, derived, get } from 'svelte/store';
import { browser } from '$app/environment';
import type { StockListItem } from '$lib/types/stock.js';

const STORAGE_KEY = 'stock-simulator-watchlist';

function createWatchlistStore() {
	// Load from localStorage
	const initialList: string[] = browser
		? JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]')
		: [];

	const { subscribe, update, set } = writable<string[]>(initialList);

	// Persist to localStorage
	if (browser) {
		subscribe((list) => {
			localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
		});
	}

	function add(stockId: string) {
		update((list) => {
			if (list.includes(stockId)) return list;
			return [...list, stockId];
		});
	}

	function remove(stockId: string) {
		update((list) => list.filter((id) => id !== stockId));
	}

	function toggle(stockId: string) {
		update((list) => {
			if (list.includes(stockId)) {
				return list.filter((id) => id !== stockId);
			}
			return [...list, stockId];
		});
	}

	function isWatched(stockId: string): boolean {
		return get({ subscribe }).includes(stockId);
	}

	function clear() {
		set([]);
	}

	const count = derived({ subscribe }, ($list) => $list.length);

	return {
		subscribe,
		add,
		remove,
		toggle,
		isWatched,
		clear,
		count
	};
}

export const watchlistStore = createWatchlistStore();
