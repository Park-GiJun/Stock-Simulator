// Toast Store - Global toast notification state management
import { writable } from 'svelte/store';

export type ToastType = 'success' | 'error' | 'warning' | 'info';
export type ToastPosition =
	| 'top-right'
	| 'top-left'
	| 'top-center'
	| 'bottom-right'
	| 'bottom-left'
	| 'bottom-center';

export interface Toast {
	id: string;
	type: ToastType;
	title?: string;
	message: string;
	duration?: number;
	dismissible?: boolean;
}

export interface ToastOptions {
	type?: ToastType;
	title?: string;
	message: string;
	duration?: number;
	dismissible?: boolean;
}

function createToastStore() {
	const { subscribe, update } = writable<Toast[]>([]);

	let idCounter = 0;

	function generateId(): string {
		return `toast-${++idCounter}-${Date.now()}`;
	}

	function addToast(options: ToastOptions): string {
		const id = generateId();
		const toast: Toast = {
			id,
			type: options.type ?? 'info',
			title: options.title,
			message: options.message,
			duration: options.duration ?? 5000,
			dismissible: options.dismissible ?? true
		};

		update((toasts) => [...toasts, toast]);

		// Auto dismiss
		if (toast.duration && toast.duration > 0) {
			setTimeout(() => {
				removeToast(id);
			}, toast.duration);
		}

		return id;
	}

	function removeToast(id: string): void {
		update((toasts) => toasts.filter((t) => t.id !== id));
	}

	function clearAll(): void {
		update(() => []);
	}

	// Convenience methods
	function success(message: string, title?: string, duration?: number): string {
		return addToast({ type: 'success', message, title, duration });
	}

	function error(message: string, title?: string, duration?: number): string {
		return addToast({ type: 'error', message, title, duration: duration ?? 8000 });
	}

	function warning(message: string, title?: string, duration?: number): string {
		return addToast({ type: 'warning', message, title, duration });
	}

	function info(message: string, title?: string, duration?: number): string {
		return addToast({ type: 'info', message, title, duration });
	}

	return {
		subscribe,
		addToast,
		removeToast,
		clearAll,
		success,
		error,
		warning,
		info
	};
}

export const toastStore = createToastStore();

// Toast position store
export const toastPosition = writable<ToastPosition>('top-right');
