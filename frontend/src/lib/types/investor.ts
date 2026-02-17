// Investor Types

export type InstitutionType =
	| 'INSTITUTIONAL_INVESTOR'
	| 'FOREIGN_INVESTOR'
	| 'PENSION_FUNDS'
	| 'ASSET_MANAGEMENT';

export type InvestmentStyle = 'AGGRESSIVE' | 'STABLE' | 'VALUE' | 'RANDOM';

export type TradingFrequency = 'HIGH' | 'MEDIUM' | 'LOW';

export interface Institution {
	institutionId: number;
	institutionName: string;
	institutionType: InstitutionType;
	investmentStyle: InvestmentStyle;
	capital: number;
	dailyIncome: number;
	riskTolerance: number;
	preferredSectors: string[];
	tradingFrequency: TradingFrequency;
	createdAt: string;
}

export interface Npc {
	npcId: number;
	npcName: string;
	investmentStyle: InvestmentStyle;
	capital: number;
	weeklyIncome: number;
	riskTolerance: number;
	preferredSectors: string[];
	tradingFrequency: TradingFrequency;
	createdAt: string;
}

// Display name mappings
export const INSTITUTION_TYPE_NAMES: Record<InstitutionType, string> = {
	INSTITUTIONAL_INVESTOR: '기관투자자',
	FOREIGN_INVESTOR: '외국계',
	PENSION_FUNDS: '연기금',
	ASSET_MANAGEMENT: '자산운용사'
};

export const INVESTMENT_STYLE_NAMES: Record<InvestmentStyle, string> = {
	AGGRESSIVE: '공격형',
	STABLE: '안정형',
	VALUE: '가치투자형',
	RANDOM: '랜덤형'
};

export const TRADING_FREQUENCY_NAMES: Record<TradingFrequency, string> = {
	HIGH: '높음',
	MEDIUM: '보통',
	LOW: '낮음'
};
