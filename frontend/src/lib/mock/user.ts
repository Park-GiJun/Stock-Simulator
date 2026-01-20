// User & Portfolio Mock Data

import type { User, Portfolio, Holding, Order, Transaction } from '$lib/types/user.js';

export const MOCK_USER: User = {
	userId: 'USR001',
	username: '테스트유저',
	email: 'test@example.com',
	capital: 5850000,
	initialCapital: 5000000,
	createdAt: '2024-01-01T00:00:00Z'
};

export const MOCK_HOLDINGS: Holding[] = [
	{
		stockId: 'STK001',
		stockName: '테크놀로지',
		quantity: 50,
		averagePrice: 48000,
		currentPrice: 52300,
		totalValue: 2615000,
		profitLoss: 215000,
		profitLossPercent: 8.96
	},
	{
		stockId: 'STK003',
		stockName: '메가일렉',
		quantity: 20,
		averagePrice: 82000,
		currentPrice: 87500,
		totalValue: 1750000,
		profitLoss: 110000,
		profitLossPercent: 6.71
	},
	{
		stockId: 'STK007',
		stockName: '푸드마스터',
		quantity: 30,
		averagePrice: 29000,
		currentPrice: 28500,
		totalValue: 855000,
		profitLoss: -15000,
		profitLossPercent: -1.72
	}
];

export const MOCK_PORTFOLIO: Portfolio = {
	userId: 'USR001',
	holdings: MOCK_HOLDINGS,
	cashBalance: 630000,
	totalStockValue: 5220000,
	totalAssetValue: 5850000,
	totalProfitLoss: 850000,
	totalProfitLossPercent: 17.0
};

export const MOCK_TRANSACTIONS: Transaction[] = [
	{
		transactionId: 'TXN001',
		orderId: 'ORD001',
		stockId: 'STK001',
		stockName: '테크놀로지',
		side: 'BUY',
		quantity: 50,
		price: 48000,
		totalAmount: 2400000,
		timestamp: '2024-01-15T10:30:00Z'
	},
	{
		transactionId: 'TXN002',
		orderId: 'ORD002',
		stockId: 'STK003',
		stockName: '메가일렉',
		side: 'BUY',
		quantity: 20,
		price: 82000,
		totalAmount: 1640000,
		timestamp: '2024-01-16T14:15:00Z'
	},
	{
		transactionId: 'TXN003',
		orderId: 'ORD003',
		stockId: 'STK007',
		stockName: '푸드마스터',
		side: 'BUY',
		quantity: 30,
		price: 29000,
		totalAmount: 870000,
		timestamp: '2024-01-17T11:00:00Z'
	},
	{
		transactionId: 'TXN004',
		orderId: 'ORD004',
		stockId: 'STK002',
		stockName: '그린팜',
		side: 'SELL',
		quantity: 20,
		price: 15500,
		totalAmount: 310000,
		timestamp: '2024-01-18T09:45:00Z'
	}
];

export function getMockUser(): User {
	return MOCK_USER;
}

export function getMockPortfolio(): Portfolio {
	return MOCK_PORTFOLIO;
}

export function getMockTransactions(): Transaction[] {
	return MOCK_TRANSACTIONS;
}
