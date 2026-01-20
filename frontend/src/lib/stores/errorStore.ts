// Error Store

import { writable } from 'svelte/store';

export interface ErrorModalState {
	isOpen: boolean;
	title: string;
	message: string;
	errorCode?: string;
}

const initialState: ErrorModalState = {
	isOpen: false,
	title: '',
	message: '',
	errorCode: undefined
};

export const errorModal = writable<ErrorModalState>(initialState);

export function showErrorModal(error: Omit<ErrorModalState, 'isOpen'>) {
	errorModal.set({
		...error,
		isOpen: true
	});
}

export function closeErrorModal() {
	errorModal.set(initialState);
}
