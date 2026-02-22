// News API - News related API functions
import { api, type ApiResponse } from './api.js';
import type { NewsArticle } from '$lib/types/news.js';

// API Endpoints
const ENDPOINTS = {
	news: '/event-service/api/news',
	newsDetail: (id: string) => `/event-service/api/news/${id}`,
	newsByStock: (stockId: string) => `/event-service/api/news/stock/${stockId}`
};

// Backend response format
interface NewsListResponse {
	news: NewsArticle[];
	page: number;
	size: number;
	totalElements: number;
	totalPages: number;
}

// Query params
export interface NewsQueryParams {
	page?: number;
	size?: number;
	level?: string;
	sentiment?: string;
	sector?: string;
	stockId?: string;
}

// API Functions
export async function getNewsList(
	params?: NewsQueryParams
): Promise<ApiResponse<NewsListResponse>> {
	return api.get<NewsListResponse>(ENDPOINTS.news, {
		params: params as Record<string, string | number | boolean | undefined>
	});
}

export async function getNewsById(newsId: string): Promise<ApiResponse<NewsArticle>> {
	return api.get<NewsArticle>(ENDPOINTS.newsDetail(newsId));
}

export async function getNewsByStock(
	stockId: string,
	params?: { page?: number; size?: number }
): Promise<ApiResponse<NewsListResponse>> {
	return api.get<NewsListResponse>(ENDPOINTS.newsByStock(stockId), {
		params: params as Record<string, string | number | boolean | undefined>
	});
}
