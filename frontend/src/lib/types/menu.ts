// Menu Types

export interface MenuItem {
	id: string;
	name: string;
	path: string;
	icon: string;
	children?: MenuItem[];
	order: number;
	isVisible: boolean;
}

export interface MenuState {
	items: MenuItem[];
	isLoading: boolean;
	error: string | null;
}
