// User Types

export interface User {
	userId: string;
	username: string;
	email: string;
	capital: number;
	initialCapital: number;
	createdAt: string;
}

export interface UserProfile extends User {
	investorType: 'USER' | 'NPC' | 'INSTITUTION';
	avatar?: string;
	bio?: string;
	rank?: number;
	returnRate?: number;
}

export interface AuthState {
	isAuthenticated: boolean;
	user: User | null;
	token: string | null;
	isLoading: boolean;
}

export interface LoginRequest {
	email: string;
	password: string;
}

export interface SignupRequest {
	username: string;
	email: string;
	password: string;
}

export interface AuthResponse {
	user: User;
	token: string;
}

// Portfolio & Holdings
export interface Holding {
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
	holdings: Holding[];
	cashBalance: number;
	totalStockValue: number;
	totalAssetValue: number;
	totalProfitLoss: number;
	totalProfitLossPercent: number;
}

// Order & Transaction
export type OrderType = 'MARKET';
export type OrderSide = 'BUY' | 'SELL';
export type OrderStatus = 'PENDING' | 'FILLED' | 'CANCELLED';

export interface Order {
	orderId: string;
	userId: string;
	stockId: string;
	stockName: string;
	orderType: OrderType;
	side: OrderSide;
	quantity: number;
	price: number | null; // null for market orders
	status: OrderStatus;
	filledQuantity: number;
	filledPrice: number | null;
	createdAt: string;
	filledAt: string | null;
}

export interface CreateOrderRequest {
	stockId: string;
	side: OrderSide;
	quantity: number;
}

export interface Transaction {
	transactionId: string;
	orderId: string;
	stockId: string;
	stockName: string;
	side: OrderSide;
	quantity: number;
	price: number;
	totalAmount: number;
	timestamp: string;
}

// User Stats
export interface UserStats {
	totalTrades: number;
	winRate: number;
	bestTrade: {
		stockName: string;
		profitPercent: number;
	} | null;
	worstTrade: {
		stockName: string;
		lossPercent: number;
	} | null;
	averageHoldingDays: number;
}
