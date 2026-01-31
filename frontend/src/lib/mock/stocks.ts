// Stock Mock Data

import type { Stock, StockListItem, OrderBook, Candle, Sector } from '$lib/types/stock.js';

const SECTORS: Sector[] = ['IT', 'AGRICULTURE', 'MANUFACTURING', 'SERVICE', 'REALESTATE', 'LUXURY', 'FOOD'];

export const MOCK_STOCKS: Stock[] = [
	{
		stockId: 'STK001',
		stockName: '테크놀로지',
		sector: 'IT',
		basePrice: 50000,
		currentPrice: 52300,
		previousClose: 50000,
		totalShares: 10000000,
		marketCapGrade: 'LARGE',
		volatility: 1.5,
		per: 25.5,
		dividendRate: 0.01,
		growthRate: 0.15,
		eventSensitivity: 1.2,
		volume: 1250000,
		high: 53000,
		low: 49500,
		open: 50200
	},
	{
		stockId: 'STK002',
		stockName: '그린팜',
		sector: 'AGRICULTURE',
		basePrice: 15000,
		currentPrice: 14800,
		previousClose: 15000,
		totalShares: 5000000,
		marketCapGrade: 'MID',
		volatility: 0.8,
		per: 12.3,
		dividendRate: 0.03,
		growthRate: 0.05,
		eventSensitivity: 1.5,
		volume: 320000,
		high: 15200,
		low: 14600,
		open: 15000
	},
	{
		stockId: 'STK003',
		stockName: '메가일렉',
		sector: 'MANUFACTURING',
		basePrice: 85000,
		currentPrice: 87500,
		previousClose: 85000,
		totalShares: 8000000,
		marketCapGrade: 'LARGE',
		volatility: 1.1,
		per: 18.7,
		dividendRate: 0.02,
		growthRate: 0.08,
		eventSensitivity: 1.0,
		volume: 890000,
		high: 88000,
		low: 84500,
		open: 85200
	},
	{
		stockId: 'STK004',
		stockName: '서비스플러스',
		sector: 'SERVICE',
		basePrice: 32000,
		currentPrice: 31500,
		previousClose: 32000,
		totalShares: 6000000,
		marketCapGrade: 'MID',
		volatility: 0.9,
		per: 15.2,
		dividendRate: 0.025,
		growthRate: 0.06,
		eventSensitivity: 0.8,
		volume: 450000,
		high: 32200,
		low: 31200,
		open: 32000
	},
	{
		stockId: 'STK005',
		stockName: '프라임부동산',
		sector: 'REALESTATE',
		basePrice: 120000,
		currentPrice: 118000,
		previousClose: 120000,
		totalShares: 4000000,
		marketCapGrade: 'LARGE',
		volatility: 0.6,
		per: 8.5,
		dividendRate: 0.045,
		growthRate: 0.03,
		eventSensitivity: 1.3,
		volume: 180000,
		high: 121000,
		low: 117500,
		open: 120000
	},
	{
		stockId: 'STK006',
		stockName: '럭스브랜드',
		sector: 'LUXURY',
		basePrice: 250000,
		currentPrice: 265000,
		previousClose: 250000,
		totalShares: 2000000,
		marketCapGrade: 'LARGE',
		volatility: 1.8,
		per: 35.0,
		dividendRate: 0.005,
		growthRate: 0.12,
		eventSensitivity: 1.5,
		volume: 85000,
		high: 270000,
		low: 248000,
		open: 252000
	},
	{
		stockId: 'STK007',
		stockName: '푸드마스터',
		sector: 'FOOD',
		basePrice: 28000,
		currentPrice: 28500,
		previousClose: 28000,
		totalShares: 7000000,
		marketCapGrade: 'MID',
		volatility: 0.5,
		per: 10.8,
		dividendRate: 0.04,
		growthRate: 0.04,
		eventSensitivity: 0.7,
		volume: 520000,
		high: 28800,
		low: 27800,
		open: 28000
	},
	{
		stockId: 'STK008',
		stockName: '클라우드테크',
		sector: 'IT',
		basePrice: 75000,
		currentPrice: 78500,
		previousClose: 75000,
		totalShares: 6000000,
		marketCapGrade: 'LARGE',
		volatility: 1.6,
		per: 28.3,
		dividendRate: 0.0,
		growthRate: 0.22,
		eventSensitivity: 1.4,
		volume: 980000,
		high: 79500,
		low: 74000,
		open: 75500
	},
	{
		stockId: 'STK009',
		stockName: '미래건설',
		sector: 'MANUFACTURING',
		basePrice: 45000,
		currentPrice: 44200,
		previousClose: 45000,
		totalShares: 5500000,
		marketCapGrade: 'MID',
		volatility: 1.0,
		per: 14.2,
		dividendRate: 0.02,
		growthRate: 0.07,
		eventSensitivity: 1.1,
		volume: 380000,
		high: 45500,
		low: 44000,
		open: 45000
	},
	{
		stockId: 'STK010',
		stockName: '헬스케어',
		sector: 'SERVICE',
		basePrice: 62000,
		currentPrice: 64500,
		previousClose: 62000,
		totalShares: 4500000,
		marketCapGrade: 'MID',
		volatility: 1.2,
		per: 20.5,
		dividendRate: 0.015,
		growthRate: 0.10,
		eventSensitivity: 0.9,
		volume: 290000,
		high: 65000,
		low: 61500,
		open: 62500
	}
];

export function getStockListItems(): StockListItem[] {
	return MOCK_STOCKS.map((stock) => ({
		stockId: stock.stockId,
		stockName: stock.stockName,
		sector: stock.sector,
		currentPrice: stock.currentPrice,
		change: stock.currentPrice - stock.previousClose,
		changePercent: ((stock.currentPrice - stock.previousClose) / stock.previousClose) * 100,
		volume: stock.volume,
		marketCapGrade: stock.marketCapGrade
	}));
}

export function getStockById(id: string): Stock | null {
	return MOCK_STOCKS.find((stock) => stock.stockId === id) ?? null;
}

export function getTopGainers(count: number = 5): StockListItem[] {
	return getStockListItems()
		.sort((a, b) => b.changePercent - a.changePercent)
		.slice(0, count);
}

export function getTopLosers(count: number = 5): StockListItem[] {
	return getStockListItems()
		.sort((a, b) => a.changePercent - b.changePercent)
		.slice(0, count);
}

export function getTopVolume(count: number = 5): StockListItem[] {
	return getStockListItems()
		.sort((a, b) => b.volume - a.volume)
		.slice(0, count);
}

// Export functions for API compatibility
export function getMockStocks(): StockListItem[] {
	return getStockListItems();
}

export function getMockStockById(stockId: string): Stock | undefined {
	return getStockById(stockId);
}

export function generateMockCandles(stockId: string, days: number = 30): Candle[] {
	return getMockCandleData(stockId, days);
}

export function generateMockOrderBook(stockId: string): OrderBook {
	return getMockOrderBook(stockId);
}

// Mock Order Book
export function getMockOrderBook(stockId: string): OrderBook {
	const stock = getStockById(stockId);
	const basePrice = stock?.currentPrice ?? 50000;

	const asks = Array.from({ length: 5 }, (_, i) => ({
		price: basePrice + (i + 1) * 100,
		quantity: Math.floor(Math.random() * 500 + 100),
		count: Math.floor(Math.random() * 10 + 1)
	}));

	const bids = Array.from({ length: 5 }, (_, i) => ({
		price: basePrice - (i + 1) * 100,
		quantity: Math.floor(Math.random() * 500 + 100),
		count: Math.floor(Math.random() * 10 + 1)
	}));

	return {
		stockId,
		asks,
		bids,
		timestamp: new Date().toISOString()
	};
}

// Helper for async mock response
export function createMockResponse<T>(data: T, delay: number = 300): Promise<T> {
	return new Promise((resolve) => {
		setTimeout(() => resolve(data), delay);
	});
}

// Mock Candle Data for Charts
export function getMockCandleData(stockId: string, days: number = 30): Candle[] {
	const stock = getStockById(stockId);
	const basePrice = stock?.currentPrice ?? 50000;
	const volatility = stock?.volatility ?? 1;

	const candles: Candle[] = [];
	let currentPrice = basePrice * 0.9; // Start lower to show growth trend

	const now = new Date();

	for (let i = days; i >= 0; i--) {
		const date = new Date(now);
		date.setDate(date.getDate() - i);

		// Random price movement based on volatility
		const change = (Math.random() - 0.45) * volatility * 0.02 * currentPrice;
		const open = currentPrice;
		const close = currentPrice + change;
		const high = Math.max(open, close) * (1 + Math.random() * volatility * 0.01);
		const low = Math.min(open, close) * (1 - Math.random() * volatility * 0.01);
		const volume = Math.floor(Math.random() * 500000 + 100000);

		candles.push({
			timestamp: date.toISOString(),
			open: Math.round(open),
			high: Math.round(high),
			low: Math.round(low),
			close: Math.round(close),
			volume
		});

		currentPrice = close;
	}

	return candles;
}

