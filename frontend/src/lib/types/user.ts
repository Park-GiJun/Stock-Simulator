// User Types - Backend DTO와 매칭

// Backend: UserResponse (GET /api/v1/users/me)
export interface User {
	userId: number; // Backend: Long
	username: string;
	email: string;
	role: string; // ROLE_USER, ROLE_ADMIN
	capital?: number; // Optional - 포트폴리오 연동 시 사용
}

// Backend: SignUpResponse
export interface SignUpResponse {
	userId: number;
	email: string;
	username: string;
}

// Backend: LoginResponse
export interface LoginResponse {
	userId: number;
	email: string;
	username: string;
	role: string;
	sessionId: string; // Redis Session ID (Cookie로도 전달됨)
}

// Frontend Auth State
export interface AuthState {
	isAuthenticated: boolean;
	user: User | null;
	isLoading: boolean;
}

// Request DTOs
export interface LoginRequest {
	email: string;
	password: string;
}

export interface SignupRequest {
	username: string;
	email: string;
	password: string;
}

// UserProfile (확장된 사용자 정보 - 향후 구현)
export interface UserProfile extends User {
	investorType?: 'USER' | 'NPC' | 'INSTITUTION';
	avatar?: string;
	bio?: string;
	rank?: number;
	returnRate?: number;
	capital?: number;
	initialCapital?: number;
	createdAt?: string;
}

// Portfolio & Holdings (기존 유지 - Trading Service 연동 시 사용)
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

// Order & Transaction (기존 유지 - Trading Service 연동 시 사용)
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
	price: number | null;
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

// User Stats (기존 유지 - 향후 구현)
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
