// API Response Types

export interface ApiResponse<T> {
	success: boolean;
	data: T | null;
	message: string | null;
	errorCode: string | null;
}

export interface PaginatedResponse<T> {
	items: T[];
	total: number;
	page: number;
	pageSize: number;
	totalPages: number;
}

export interface ApiError {
	status: number;
	message: string;
	errorCode: string;
}
