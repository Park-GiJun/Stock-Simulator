// Game Time Store - 24시간 연속 운영

import { writable, derived } from 'svelte/store';

export interface GameTimeState {
	gameTime: Date;
	realTime: Date;
	isMarketOpen: boolean;
	speedMultiplier: number; // 1:4 = 4
}

const SPEED_MULTIPLIER = 4;

function createGameTimeStore() {
	// Start with current real time
	const now = new Date();
	const gameTime = new Date(now);

	const initialState: GameTimeState = {
		gameTime,
		realTime: now,
		isMarketOpen: true,
		speedMultiplier: SPEED_MULTIPLIER
	};

	const { subscribe, set, update } = writable<GameTimeState>(initialState);

	let intervalId: ReturnType<typeof setInterval> | null = null;

	return {
		subscribe,

		start: () => {
			if (intervalId) return;

			intervalId = setInterval(() => {
				update((state) => {
					const realNow = new Date();
					const realElapsed = realNow.getTime() - state.realTime.getTime();
					const gameElapsed = realElapsed * state.speedMultiplier;

					const newGameTime = new Date(state.gameTime.getTime() + gameElapsed);

					return {
						...state,
						gameTime: newGameTime,
						realTime: realNow,
						isMarketOpen: true
					};
				});
			}, 1000);
		},

		stop: () => {
			if (intervalId) {
				clearInterval(intervalId);
				intervalId = null;
			}
		},

		setGameTime: (date: Date) => {
			update((state) => ({
				...state,
				gameTime: date,
				realTime: new Date(),
				isMarketOpen: true
			}));
		},

		reset: () => {
			const now = new Date();
			set({
				gameTime: now,
				realTime: now,
				isMarketOpen: true,
				speedMultiplier: SPEED_MULTIPLIER
			});
		}
	};
}

export const gameTimeStore = createGameTimeStore();

// Derived stores
export const gameTime = derived(gameTimeStore, ($state) => $state.gameTime);
export const isMarketOpen = derived(gameTimeStore, ($state) => $state.isMarketOpen);

// Format helpers
export function formatGameTime(date: Date): string {
	return date.toLocaleTimeString('ko-KR', {
		hour: '2-digit',
		minute: '2-digit',
		second: '2-digit',
		hour12: false
	});
}

export function formatGameDate(date: Date): string {
	return date.toLocaleDateString('ko-KR', {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit'
	});
}
