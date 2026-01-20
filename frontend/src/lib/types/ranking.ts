// Ranking Types

export type RankingType = 'RETURN_RATE' | 'TOTAL_ASSET' | 'TRADING_VOLUME';

export interface RankingEntry {
	rank: number;
	userId: string;
	username: string;
	returnRate: number; // Percentage
	totalAsset: number;
	tradingVolume: number;
	previousRank: number;
	rankChange: number;
}

export interface RankingList {
	type: RankingType;
	entries: RankingEntry[];
	updatedAt: string;
	seasonId: string;
}

export interface Season {
	seasonId: string;
	seasonNumber: number;
	startDate: string;
	endDate: string | null;
	isActive: boolean;
}

export interface SeasonHistory {
	seasonId: string;
	seasonNumber: number;
	finalRank: number;
	returnRate: number;
	totalAsset: number;
	totalTrades: number;
}

// Ranking type display names
export const RANKING_TYPE_NAMES: Record<RankingType, string> = {
	RETURN_RATE: '수익률',
	TOTAL_ASSET: '총자산',
	TRADING_VOLUME: '거래왕'
};
