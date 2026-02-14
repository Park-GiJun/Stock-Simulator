// Stock API - Stock related API functions
import { api, useMock, type ApiResponse } from './api.js';
import type { Stock, StockListItem, OrderBook, Candle, Trade } from '$lib/types/stock.js';
import {
	getMockStocks,
	getMockStockById,
	getMockCandleData,
	getMockOrderBook
} from '$lib/mock/stocks.js';

// API Endpoints
const ENDPOINTS = {
	stocks: '/stock-service/api/stocks',
	stock: (id: string) => `/stock-service/api/stocks/${id}`,
	orderBook: (id: string) => `/stock-service/api/stocks/${id}/orderbook`,
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
	if (useMock()) {
		const stocks = getMockStocks();
		return {
			success: true,
			data: {
				stocks,
				total: stocks.length,
				page: params?.page ?? 0,
				size: params?.size ?? 20
			},
			error: null,
			timestamp: new Date().toISOString()
		};
	}

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
	if (useMock()) {
		const stock = getMockStockById(stockId);
		if (!stock) {
			return {
				success: false,
				data: null,
				error: '종목을 찾을 수 없습니다.',
				timestamp: new Date().toISOString()
			};
		}
		return {
			success: true,
			data: stock,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<Stock>(ENDPOINTS.stock(stockId));
}

export async function getOrderBook(stockId: string): Promise<ApiResponse<OrderBook>> {
	if (useMock()) {
		const orderBook = getMockOrderBook(stockId);
		return {
			success: true,
			data: orderBook,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<OrderBook>(ENDPOINTS.orderBook(stockId));
}

export async function getCandles(
	stockId: string,
	params?: CandlesQueryParams
): Promise<ApiResponse<CandlesResponse>> {
	if (useMock()) {
		const candles = getMockCandleData(stockId, params?.limit ?? 30);
		return {
			success: true,
			data: {
				candles,
				stockId,
				interval: params?.interval ?? '1d'
			},
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<CandlesResponse>(ENDPOINTS.candles(stockId), { params: params as Record<string, string | number | boolean | undefined> });
}

export async function getTrades(
	stockId: string,
	limit?: number
): Promise<ApiResponse<TradesResponse>> {
	if (useMock()) {
		const stock = getMockStockById(stockId);
		const trades: Trade[] = [];
		if (stock) {
			for (let i = 0; i < (limit ?? 20); i++) {
				trades.push({
					tradeId: `trade-${i}`,
					stockId,
					price: stock.currentPrice + (Math.random() - 0.5) * stock.currentPrice * 0.01,
					quantity: Math.floor(Math.random() * 1000) + 1,
					side: Math.random() > 0.5 ? 'BUY' : 'SELL',
					timestamp: new Date(Date.now() - i * 60000).toISOString()
				});
			}
		}
		return {
			success: true,
			data: { trades, stockId },
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<TradesResponse>(ENDPOINTS.trades(stockId), { params: { limit } });
}

// Search stocks
export async function searchStocks(query: string): Promise<ApiResponse<StockListItem[]>> {
	if (useMock()) {
		const allStocks = getMockStocks();
		const filtered = allStocks.filter(
			(s) =>
				s.stockName.toLowerCase().includes(query.toLowerCase()) ||
				s.stockId.toLowerCase().includes(query.toLowerCase())
		);
		return {
			success: true,
			data: filtered,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<StockListItem[]>(`${ENDPOINTS.stocks}/search`, { params: { q: query } });
}
