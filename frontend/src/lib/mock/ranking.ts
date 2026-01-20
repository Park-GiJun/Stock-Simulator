// Ranking Mock Data

import type { RankingEntry, RankingList, RankingType } from '$lib/types/ranking.js';

const MOCK_RANKING_ENTRIES: RankingEntry[] = [
	{ rank: 1, userId: 'USR001', username: '투자의신', returnRate: 45.8, totalAsset: 7290000, tradingVolume: 125000000, previousRank: 1, rankChange: 0 },
	{ rank: 2, userId: 'USR002', username: '주식고수', returnRate: 38.2, totalAsset: 6910000, tradingVolume: 98000000, previousRank: 3, rankChange: 1 },
	{ rank: 3, userId: 'USR003', username: '워렌버핏', returnRate: 32.5, totalAsset: 6625000, tradingVolume: 156000000, previousRank: 2, rankChange: -1 },
	{ rank: 4, userId: 'USR004', username: '차트마스터', returnRate: 28.9, totalAsset: 6445000, tradingVolume: 78000000, previousRank: 5, rankChange: 1 },
	{ rank: 5, userId: 'USR005', username: '안전투자', returnRate: 24.3, totalAsset: 6215000, tradingVolume: 45000000, previousRank: 4, rankChange: -1 },
	{ rank: 6, userId: 'USR006', username: '단타왕', returnRate: 21.7, totalAsset: 6085000, tradingVolume: 210000000, previousRank: 8, rankChange: 2 },
	{ rank: 7, userId: 'USR007', username: '가치투자', returnRate: 18.5, totalAsset: 5925000, tradingVolume: 32000000, previousRank: 6, rankChange: -1 },
	{ rank: 8, userId: 'USR008', username: '스윙트레이더', returnRate: 15.2, totalAsset: 5760000, tradingVolume: 67000000, previousRank: 7, rankChange: -1 },
	{ rank: 9, userId: 'USR009', username: '배당러버', returnRate: 12.8, totalAsset: 5640000, tradingVolume: 28000000, previousRank: 10, rankChange: 1 },
	{ rank: 10, userId: 'USR010', username: '초보투자자', returnRate: 8.5, totalAsset: 5425000, tradingVolume: 18000000, previousRank: 9, rankChange: -1 }
];

export function getRankingByType(type: RankingType): RankingList {
	let sortedEntries = [...MOCK_RANKING_ENTRIES];

	switch (type) {
		case 'RETURN_RATE':
			sortedEntries.sort((a, b) => b.returnRate - a.returnRate);
			break;
		case 'TOTAL_ASSET':
			sortedEntries.sort((a, b) => b.totalAsset - a.totalAsset);
			break;
		case 'TRADING_VOLUME':
			sortedEntries.sort((a, b) => b.tradingVolume - a.tradingVolume);
			break;
	}

	// Re-assign ranks after sorting
	sortedEntries = sortedEntries.map((entry, index) => ({
		...entry,
		rank: index + 1
	}));

	return {
		type,
		entries: sortedEntries,
		updatedAt: new Date().toISOString(),
		seasonId: 'SEASON_001'
	};
}

export function getTopRankers(count: number = 10): RankingEntry[] {
	return MOCK_RANKING_ENTRIES.slice(0, count);
}
