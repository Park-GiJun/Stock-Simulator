// Trading API - Order and portfolio related API functions
import { api, useMock, type ApiResponse } from './api.js';
import type { Trade } from '$lib/types/stock.js';

// Types
export interface PlaceOrderResponse {
	orderId: string;
	userId: string;
	stockId: string;
	orderType: 'BUY' | 'SELL';
	orderKind: 'LIMIT' | 'MARKET';
	price: number | null;
	quantity: number;
	filledQuantity: number;
	status: 'PENDING' | 'PARTIALLY_FILLED' | 'FILLED' | 'CANCELLED' | 'REJECTED';
	matches: MatchDetail[];
}

export interface MatchDetail {
	tradeId: string;
	price: number;
	quantity: number;
}

export interface Order {
	orderId: string;
	stockId: string;
	stockName: string;
	side: 'BUY' | 'SELL';
	orderType: 'LIMIT' | 'MARKET';
	status: 'PENDING' | 'PARTIALLY_FILLED' | 'FILLED' | 'CANCELLED' | 'REJECTED';
	price: number;
	quantity: number;
	filledQuantity: number;
	createdAt: string;
	updatedAt: string;
}

export interface PortfolioItem {
	stockId: string;
	stockName: string;
	quantity: number;
	averagePrice: number;
	currentPrice: number;
	totalValue: number;
	profitLoss: number;
	profitLossPercent: number;
}

export interface Portfolio {
	userId: string;
	cash: number;
	totalValue: number;
	totalProfitLoss: number;
	totalProfitLossPercent: number;
	items: PortfolioItem[];
}

export interface OrderRequest {
	userId: string;
	stockId: string;
	orderType: 'BUY' | 'SELL';
	orderKind: 'LIMIT' | 'MARKET';
	price?: number | null;
	quantity: number;
}

export interface OrdersResponse {
	orders: Order[];
	total: number;
	page: number;
	size: number;
}

export interface TradeHistoryResponse {
	trades: Trade[];
	total: number;
	page: number;
	size: number;
}

// API Endpoints
const ENDPOINTS = {
	orders: '/trading-service/api/trading/orders',
	order: (id: string) => `/trading-service/api/trading/orders/${id}`,
	portfolio: '/trading-service/api/trading/portfolio',
	history: '/trading-service/api/trading/history'
};

// API Functions
export async function getOrders(
	params?: { status?: string; page?: number; size?: number }
): Promise<ApiResponse<OrdersResponse>> {
	if (useMock()) {
		return {
			success: true,
			data: {
				orders: [],
				total: 0,
				page: 1,
				size: 20
			},
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<OrdersResponse>(ENDPOINTS.orders, { params });
}

export async function getOrder(orderId: string): Promise<ApiResponse<Order>> {
	if (useMock()) {
		return {
			success: false,
			data: null,
			error: '주문을 찾을 수 없습니다.',
			timestamp: new Date().toISOString()
		};
	}

	return api.get<Order>(ENDPOINTS.order(orderId));
}

export async function createOrder(order: OrderRequest): Promise<ApiResponse<PlaceOrderResponse>> {
	return api.post<PlaceOrderResponse>(ENDPOINTS.orders, order);
}

export async function cancelOrder(orderId: string, userId: string): Promise<ApiResponse<void>> {
	return api.delete<void>(ENDPOINTS.order(orderId), { params: { userId } });
}

export async function getPortfolio(): Promise<ApiResponse<Portfolio>> {
	if (useMock()) {
		const mockPortfolio: Portfolio = {
			userId: 'mock-user',
			cash: 5000000,
			totalValue: 5850000,
			totalProfitLoss: 850000,
			totalProfitLossPercent: 17.0,
			items: [
				{
					stockId: 'stock-1',
					stockName: '테크노소프트',
					quantity: 100,
					averagePrice: 45000,
					currentPrice: 52000,
					totalValue: 5200000,
					profitLoss: 700000,
					profitLossPercent: 15.56
				},
				{
					stockId: 'stock-2',
					stockName: '그린팜',
					quantity: 50,
					averagePrice: 12000,
					currentPrice: 13000,
					totalValue: 650000,
					profitLoss: 50000,
					profitLossPercent: 8.33
				}
			]
		};
		return {
			success: true,
			data: mockPortfolio,
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<Portfolio>(ENDPOINTS.portfolio);
}

export async function getTradeHistory(
	params?: { page?: number; size?: number; stockId?: string }
): Promise<ApiResponse<TradeHistoryResponse>> {
	if (useMock()) {
		return {
			success: true,
			data: {
				trades: [],
				total: 0,
				page: 1,
				size: 20
			},
			error: null,
			timestamp: new Date().toISOString()
		};
	}

	return api.get<TradeHistoryResponse>(ENDPOINTS.history, { params });
}
