// Stock API - Stock related API functions
import { api, type ApiResponse } from './api.js';
import type { Stock, StockListItem, OrderBook, Candle, Trade } from '$lib/types/stock.js';

// API Endpoints
const ENDPOINTS = {
	stocks: '/stock-service/api/stocks',
	stock: (id: string) => `/stock-service/api/stocks/${id}`,
	orderBook: (id: string) => `/trading-service/api/trading/order-book/${id}`,
	candles: (id: string) => `/stock-service/api/stocks/${id}/candles`,
	trades: (id: string) => `/stock-service/api/stocks/${id}/trades`
};

// Backend PageResponse format
interface PageResponse<T> {
	content: T[];
	page: number;
	size: number;
	totalElements: number;
	totalPages: number;
	first: boolean;
	last: boolean;
	empty: boolean;
}

// Types for API responses
export interface StocksResponse {
	stocks: StockListItem[];
	total: number;
	page: number;
	size: number;
}

export interface CandlesResponse {
	candles: Candle[];
	stockId: string;
	interval: string;
}

export interface TradesResponse {
	trades: Trade[];
	stockId: string;
}

// Query params
export interface StocksQueryParams {
	page?: number;
	size?: number;
	sector?: string;
	marketCap?: string;
	sortBy?: string;
	sortOrder?: 'asc' | 'desc';
	search?: string;
}

export interface CandlesQueryParams {
	interval?: '1m' | '5m' | '15m' | '1h' | '1d' | '1w';
	limit?: number;
	from?: string;
	to?: string;
}

// API Functions
export async function getStocks(params?: StocksQueryParams): Promise<ApiResponse<StocksResponse>> {
	const res = await api.get<PageResponse<StockListItem>>(ENDPOINTS.stocks, { params: params as Record<string, string | number | boolean | undefined> });
	return {
		...res,
		data: res.data
			? {
					stocks: res.data.content,
					total: res.data.totalElements,
					page: res.data.page,
					size: res.data.size
				}
			: null
	};
}

export async function getStock(stockId: string): Promise<ApiResponse<Stock>> {
	return api.get<Stock>(ENDPOINTS.stock(stockId));
}

export async function getOrderBook(stockId: string): Promise<ApiResponse<OrderBook>> {
	return api.get<OrderBook>(ENDPOINTS.orderBook(stockId));
}

export async function getCandles(
	stockId: string,
	params?: CandlesQueryParams
): Promise<ApiResponse<CandlesResponse>> {
	return api.get<CandlesResponse>(ENDPOINTS.candles(stockId), { params: params as Record<string, string | number | boolean | undefined> });
}

export async function getTrades(
	stockId: string,
	limit?: number
): Promise<ApiResponse<TradesResponse>> {
	return api.get<TradesResponse>(ENDPOINTS.trades(stockId), { params: { limit } });
}

// Search stocks
export async function searchStocks(query: string): Promise<ApiResponse<StockListItem[]>> {
	return api.get<StockListItem[]>(`${ENDPOINTS.stocks}/search`, { params: { q: query } });
}
