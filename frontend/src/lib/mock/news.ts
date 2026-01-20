// News Mock Data

import type { NewsEvent, NewsListItem, EventLevel, Sentiment } from '$lib/types/news.js';
import type { Sector } from '$lib/types/stock.js';

export const MOCK_NEWS: NewsEvent[] = [
	{
		eventId: 'EVT001',
		level: 'SOCIETY',
		targetSector: null,
		targetStockId: null,
		targetStockName: null,
		sentiment: 'POSITIVE',
		intensity: 1.2,
		duration: 120,
		headline: '중앙은행, 기준금리 0.25% 인하 결정',
		content: '중앙은행이 경기 부양을 위해 기준금리를 0.25%p 인하하기로 결정했습니다. 이번 금리 인하로 시장에 유동성이 공급되면서 전반적인 주가 상승이 예상됩니다.',
		createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() + 90 * 60 * 1000).toISOString(),
		isActive: true
	},
	{
		eventId: 'EVT002',
		level: 'INDUSTRY',
		targetSector: 'IT',
		targetStockId: null,
		targetStockName: null,
		sentiment: 'POSITIVE',
		intensity: 0.8,
		duration: 60,
		headline: 'AI 산업 성장세 지속, IT 기업 실적 호조 예상',
		content: '글로벌 AI 시장이 지속적으로 성장하면서 국내 IT 기업들의 실적도 호조를 보일 것으로 전망됩니다.',
		createdAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
		isActive: true
	},
	{
		eventId: 'EVT003',
		level: 'COMPANY',
		targetSector: 'IT',
		targetStockId: 'STK001',
		targetStockName: '테크놀로지',
		sentiment: 'POSITIVE',
		intensity: 0.5,
		duration: 45,
		headline: '테크놀로지, 신규 데이터센터 건설 발표',
		content: '테크놀로지가 1조원 규모의 신규 데이터센터 건설 계획을 발표했습니다. 이를 통해 클라우드 서비스 역량이 크게 강화될 것으로 예상됩니다.',
		createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
		isActive: true
	},
	{
		eventId: 'EVT004',
		level: 'INDUSTRY',
		targetSector: 'REALESTATE',
		targetStockId: null,
		targetStockName: null,
		sentiment: 'NEGATIVE',
		intensity: 1.0,
		duration: 90,
		headline: '정부, 부동산 규제 강화 방안 발표',
		content: '정부가 부동산 시장 안정화를 위한 추가 규제 방안을 발표했습니다. 대출 규제 강화와 세금 인상이 포함되어 있어 부동산 관련주에 부정적 영향이 예상됩니다.',
		createdAt: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() + 45 * 60 * 1000).toISOString(),
		isActive: true
	},
	{
		eventId: 'EVT005',
		level: 'COMPANY',
		targetSector: 'LUXURY',
		targetStockId: 'STK006',
		targetStockName: '럭스브랜드',
		sentiment: 'POSITIVE',
		intensity: 0.6,
		duration: 60,
		headline: '럭스브랜드, 해외 명품 브랜드 인수 추진',
		content: '럭스브랜드가 유럽의 유명 명품 브랜드 인수를 추진 중인 것으로 알려졌습니다. 인수가 성사될 경우 글로벌 시장 진출이 가속화될 전망입니다.',
		createdAt: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() + 10 * 60 * 1000).toISOString(),
		isActive: true
	},
	{
		eventId: 'EVT006',
		level: 'SOCIETY',
		targetSector: null,
		targetStockId: null,
		targetStockName: null,
		sentiment: 'NEGATIVE',
		intensity: 0.7,
		duration: 60,
		headline: '글로벌 원자재 가격 급등, 인플레이션 우려',
		content: '국제 원자재 가격이 급등하면서 인플레이션에 대한 우려가 커지고 있습니다. 제조업과 식품업 등에 부정적인 영향이 예상됩니다.',
		createdAt: new Date(Date.now() - 90 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() + 20 * 60 * 1000).toISOString(),
		isActive: false
	},
	{
		eventId: 'EVT007',
		level: 'INDUSTRY',
		targetSector: 'FOOD',
		targetStockId: null,
		targetStockName: null,
		sentiment: 'NEUTRAL',
		intensity: 0.4,
		duration: 30,
		headline: '식품업계, 신제품 출시 경쟁 본격화',
		content: '주요 식품 기업들이 2분기 신제품 출시를 앞두고 마케팅 경쟁에 돌입했습니다.',
		createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
		expiresAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
		isActive: false
	}
];

export function getNewsList(): NewsListItem[] {
	return MOCK_NEWS.map((news) => ({
		eventId: news.eventId,
		level: news.level,
		sentiment: news.sentiment,
		headline: news.headline,
		createdAt: news.createdAt,
		targetSector: news.targetSector ?? undefined,
		targetStockName: news.targetStockName ?? undefined
	}));
}

export function getActiveNews(): NewsListItem[] {
	return getNewsList().filter((news) => {
		const event = MOCK_NEWS.find((n) => n.eventId === news.eventId);
		return event?.isActive ?? false;
	});
}

export function getNewsById(id: string): NewsEvent | null {
	return MOCK_NEWS.find((news) => news.eventId === id) ?? null;
}

export function getNewsByLevel(level: EventLevel): NewsListItem[] {
	return getNewsList().filter((news) => news.level === level);
}

export function getNewsBySector(sector: Sector): NewsListItem[] {
	return getNewsList().filter((news) => news.targetSector === sector);
}
