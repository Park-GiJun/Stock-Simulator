// Stock Types

export type Sector =
	| 'IT'
	| 'AGRICULTURE'
	| 'MANUFACTURING'
	| 'SERVICE'
	| 'REAL_ESTATE'
	| 'LUXURY'
	| 'FOOD';

export type MarketCapGrade = 'SMALL' | 'MID' | 'LARGE';

export type TradingFrequency = 'HIGH' | 'MEDIUM' | 'LOW';

export interface Stock {
	stockId: string;
	stockName: string;
	sector: Sector;
	basePrice: number;
	currentPrice: number;
	previousClose: number;
	totalShares: number;
	marketCapGrade: MarketCapGrade;
	volatility: number;
	per: number;
	dividendRate: number;
	growthRate: number;
	eventSensitivity: number;
	volume: number;
	high: number;
	low: number;
	open: number;
}

export interface StockChange {
	change: number;
	changePercent: number;
	direction: 'up' | 'down' | 'unchanged';
}

export interface OrderBookEntry {
	price: number;
	quantity: number;
	count: number;
}

export interface OrderBook {
	stockId: string;
	asks: OrderBookEntry[]; // 매도 호가 (높은 가격 → 낮은 가격)
	bids: OrderBookEntry[]; // 매수 호가 (높은 가격 → 낮은 가격)
	timestamp: string;
}

export interface Trade {
	tradeId: string;
	stockId: string;
	price: number;
	quantity: number;
	side: 'BUY' | 'SELL';
	timestamp: string;
}

export interface Candle {
	timestamp: string;
	open: number;
	high: number;
	low: number;
	close: number;
	volume: number;
}

export interface StockListItem {
	stockId: string;
	stockName: string;
	sector: Sector;
	currentPrice: number;
	change: number;
	changePercent: number;
	volume: number;
	marketCapGrade: MarketCapGrade;
}

// Sector display names
export const SECTOR_NAMES: Record<Sector, string> = {
	IT: 'IT',
	AGRICULTURE: '농경',
	MANUFACTURING: '제조업',
	SERVICE: '서비스업',
	REAL_ESTATE: '부동산',
	LUXURY: '럭셔리',
	FOOD: '식료'
};

export const MARKET_CAP_NAMES: Record<MarketCapGrade, string> = {
	SMALL: '소형주',
	MID: '중형주',
	LARGE: '대형주'
};
