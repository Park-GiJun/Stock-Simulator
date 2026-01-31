// WebSocket Store - Real-time connection management
import { writable, derived, get } from 'svelte/store';
import { browser } from '$app/environment';

// Types
export type ConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'error';

export interface WebSocketMessage {
	type: string;
	data: unknown;
	timestamp: string;
}

export interface PriceUpdate {
	stockId: string;
	price: number;
	change: number;
	changePercent: number;
	volume: number;
	timestamp: string;
}

export interface OrderBookUpdate {
	stockId: string;
	asks: Array<{ price: number; quantity: number }>;
	bids: Array<{ price: number; quantity: number }>;
	timestamp: string;
}

// Configuration
const WS_URL = browser ? (import.meta.env.VITE_WS_URL ?? 'ws://localhost:9832/ws') : '';
const RECONNECT_DELAY = 3000;
const MAX_RECONNECT_ATTEMPTS = 5;
const HEARTBEAT_INTERVAL = 30000;

// Store state
interface WebSocketState {
	status: ConnectionStatus;
	reconnectAttempts: number;
	lastError: string | null;
}

function createWebSocketStore() {
	const state = writable<WebSocketState>({
		status: 'disconnected',
		reconnectAttempts: 0,
		lastError: null
	});

	const priceUpdates = writable<Map<string, PriceUpdate>>(new Map());
	const orderBookUpdates = writable<Map<string, OrderBookUpdate>>(new Map());
	const subscriptions = new Set<string>();

	let ws: WebSocket | null = null;
	let heartbeatTimer: ReturnType<typeof setInterval> | null = null;
	let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

	function connect() {
		if (!browser || ws?.readyState === WebSocket.OPEN) return;

		state.update((s) => ({ ...s, status: 'connecting' }));

		try {
			ws = new WebSocket(WS_URL);

			ws.onopen = () => {
				state.update((s) => ({
					...s,
					status: 'connected',
					reconnectAttempts: 0,
					lastError: null
				}));

				// Resubscribe to all previous subscriptions
				subscriptions.forEach((stockId) => {
					sendMessage({ type: 'subscribe', stockId });
				});

				// Start heartbeat
				startHeartbeat();
			};

			ws.onmessage = (event) => {
				try {
					const message: WebSocketMessage = JSON.parse(event.data);
					handleMessage(message);
				} catch (error) {
					console.error('Failed to parse WebSocket message:', error);
				}
			};

			ws.onerror = () => {
				state.update((s) => ({
					...s,
					status: 'error',
					lastError: 'WebSocket connection error'
				}));
			};

			ws.onclose = () => {
				state.update((s) => ({ ...s, status: 'disconnected' }));
				stopHeartbeat();
				scheduleReconnect();
			};
		} catch {
			state.update((s) => ({
				...s,
				status: 'error',
				lastError: 'Failed to create WebSocket connection'
			}));
		}
	}

	function disconnect() {
		if (reconnectTimer) {
			clearTimeout(reconnectTimer);
			reconnectTimer = null;
		}
		stopHeartbeat();

		if (ws) {
			ws.close();
			ws = null;
		}

		state.update((s) => ({
			...s,
			status: 'disconnected',
			reconnectAttempts: 0
		}));
	}

	function scheduleReconnect() {
		const currentState = get(state);
		if (currentState.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
			state.update((s) => ({
				...s,
				lastError: 'Max reconnection attempts reached'
			}));
			return;
		}

		const delay = RECONNECT_DELAY * Math.pow(2, currentState.reconnectAttempts);
		reconnectTimer = setTimeout(() => {
			state.update((s) => ({
				...s,
				reconnectAttempts: s.reconnectAttempts + 1
			}));
			connect();
		}, delay);
	}

	function startHeartbeat() {
		heartbeatTimer = setInterval(() => {
			sendMessage({ type: 'ping' });
		}, HEARTBEAT_INTERVAL);
	}

	function stopHeartbeat() {
		if (heartbeatTimer) {
			clearInterval(heartbeatTimer);
			heartbeatTimer = null;
		}
	}

	function sendMessage(message: Record<string, unknown>) {
		if (ws?.readyState === WebSocket.OPEN) {
			ws.send(JSON.stringify(message));
		}
	}

	function handleMessage(message: WebSocketMessage) {
		switch (message.type) {
			case 'price': {
				const update = message.data as PriceUpdate;
				priceUpdates.update((map) => {
					map.set(update.stockId, update);
					return new Map(map);
				});
				break;
			}
			case 'orderbook': {
				const update = message.data as OrderBookUpdate;
				orderBookUpdates.update((map) => {
					map.set(update.stockId, update);
					return new Map(map);
				});
				break;
			}
			case 'pong':
				// Heartbeat response
				break;
			default:
				console.log('Unknown message type:', message.type);
		}
	}

	function subscribe(stockId: string) {
		subscriptions.add(stockId);
		if (ws?.readyState === WebSocket.OPEN) {
			sendMessage({ type: 'subscribe', stockId });
		}
	}

	function unsubscribe(stockId: string) {
		subscriptions.delete(stockId);
		if (ws?.readyState === WebSocket.OPEN) {
			sendMessage({ type: 'unsubscribe', stockId });
		}
		priceUpdates.update((map) => {
			map.delete(stockId);
			return new Map(map);
		});
		orderBookUpdates.update((map) => {
			map.delete(stockId);
			return new Map(map);
		});
	}

	// Derived stores
	const status = derived(state, ($state) => $state.status);
	const isConnected = derived(state, ($state) => $state.status === 'connected');
	const error = derived(state, ($state) => $state.lastError);

	function getPrice(stockId: string) {
		return derived(priceUpdates, ($updates) => $updates.get(stockId));
	}

	function getOrderBook(stockId: string) {
		return derived(orderBookUpdates, ($updates) => $updates.get(stockId));
	}

	return {
		subscribe: state.subscribe,
		connect,
		disconnect,
		subscribeStock: subscribe,
		unsubscribeStock: unsubscribe,
		status,
		isConnected,
		error,
		priceUpdates,
		orderBookUpdates,
		getPrice,
		getOrderBook
	};
}

export const websocketStore = createWebSocketStore();

// Auto-connect when imported in browser
if (browser) {
	// Delay connection to allow app initialization
	setTimeout(() => {
		websocketStore.connect();
	}, 1000);
}
