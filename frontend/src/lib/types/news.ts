// News & Event Types

import type { Sector } from './stock.js';

export type EventLevel = 'SOCIETY' | 'INDUSTRY' | 'COMPANY';
export type Sentiment = 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';

export interface NewsEvent {
	eventId: string;
	level: EventLevel;
	targetSector: Sector | null; // For INDUSTRY level
	targetStockId: string | null; // For COMPANY level
	targetStockName: string | null;
	sentiment: Sentiment;
	intensity: number; // 0.1 ~ 2.0
	duration: number; // Game minutes
	headline: string;
	content: string;
	createdAt: string;
	expiresAt: string;
	isActive: boolean;
}

export interface NewsListItem {
	eventId: string;
	level: EventLevel;
	sentiment: Sentiment;
	headline: string;
	createdAt: string;
	targetSector?: Sector;
	targetStockName?: string;
}

export interface NewsFilter {
	level?: EventLevel;
	sector?: Sector;
	sentiment?: Sentiment;
	search?: string;
}

// Event level display names
export const EVENT_LEVEL_NAMES: Record<EventLevel, string> = {
	SOCIETY: '사회',
	INDUSTRY: '산업',
	COMPANY: '기업'
};

export const SENTIMENT_NAMES: Record<Sentiment, string> = {
	POSITIVE: '호재',
	NEGATIVE: '악재',
	NEUTRAL: '중립'
};
