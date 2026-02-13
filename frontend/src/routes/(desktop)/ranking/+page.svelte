<script lang="ts">
	import { getRankingByType } from '$lib/mock/ranking.js';
	import { RANKING_TYPE_NAMES, type RankingType } from '$lib/types/ranking.js';
	import { Podium } from '$lib/components';
	import { currentUser } from '$lib/stores/authStore.js';
	import { TrendingUp, DollarSign, BarChart3, ArrowUp, ArrowDown } from 'lucide-svelte';

	let selectedType = $state<RankingType>('RETURN_RATE');

	const ranking = $derived(getRankingByType(selectedType));

	const types: RankingType[] = ['RETURN_RATE', 'TOTAL_ASSET', 'TRADING_VOLUME'];

	const typeIcons = {
		RETURN_RATE: TrendingUp,
		TOTAL_ASSET: DollarSign,
		TRADING_VOLUME: BarChart3
	} as const;

	function formatValue(entry: (typeof ranking.entries)[0]): string {
		switch (selectedType) {
			case 'RETURN_RATE':
				return `${entry.returnRate >= 0 ? '+' : ''}${entry.returnRate.toFixed(1)}%`;
			case 'TOTAL_ASSET':
				return `₩${entry.totalAsset.toLocaleString()}`;
			case 'TRADING_VOLUME':
				return `₩${(entry.tradingVolume / 1000000).toFixed(0)}M`;
		}
	}

	function formatSubValue(entry: (typeof ranking.entries)[0]): string {
		switch (selectedType) {
			case 'RETURN_RATE':
				return `₩${entry.totalAsset.toLocaleString()}`;
			case 'TOTAL_ASSET':
				return `${entry.returnRate >= 0 ? '+' : ''}${entry.returnRate.toFixed(1)}%`;
			case 'TRADING_VOLUME':
				return `₩${entry.totalAsset.toLocaleString()}`;
		}
	}

	function getRankClass(rank: number): string {
		if (rank === 1) return 'gold';
		if (rank === 2) return 'silver';
		if (rank === 3) return 'bronze';
		return '';
	}

	const podiumEntries = $derived(
		ranking.entries.slice(0, 3).map((entry) => ({
			rank: entry.rank,
			username: entry.username,
			value: formatValue(entry),
			subValue: formatSubValue(entry)
		}))
	);

	const tableEntries = $derived(ranking.entries.slice(3));

	// Mock: current user's rank data
	const myRank = $derived({
		rank: 15,
		returnRate: 17.0,
		totalAsset: 5850000,
		tradingVolume: 42000000,
		rankChange: 3
	});

	function myRankValue(): string {
		switch (selectedType) {
			case 'RETURN_RATE':
				return `${myRank.returnRate >= 0 ? '+' : ''}${myRank.returnRate.toFixed(1)}%`;
			case 'TOTAL_ASSET':
				return `₩${myRank.totalAsset.toLocaleString()}`;
			case 'TRADING_VOLUME':
				return `₩${(myRank.tradingVolume / 1000000).toFixed(0)}M`;
		}
	}
</script>

<div class="ranking-page">
	<h1 class="text-2xl font-bold mb-lg gradient-text">랭킹</h1>

	<!-- Ranking Type Tabs -->
	<div class="ranking-type-tabs">
		{#each types as type}
			{@const Icon = typeIcons[type]}
			<button
				class="ranking-type-tab"
				class:active={selectedType === type}
				onclick={() => (selectedType = type)}
			>
				<Icon size={16} />
				{RANKING_TYPE_NAMES[type]}
			</button>
		{/each}
	</div>

	<!-- Podium Section (Top 3) -->
	<Podium entries={podiumEntries} />

	<!-- My Rank Highlight -->
	{#if $currentUser}
		<div class="my-rank-card">
			<div>
				<div class="my-rank-label">내 순위</div>
				<div class="my-rank-number">#{myRank.rank}</div>
			</div>
			<div class="my-rank-info">
				<div class="my-rank-value" class:text-stock-up={selectedType === 'RETURN_RATE' && myRank.returnRate >= 0} class:text-stock-down={selectedType === 'RETURN_RATE' && myRank.returnRate < 0}>
					{myRankValue()}
				</div>
			</div>
			{#if myRank.rankChange > 0}
				<span class="my-rank-change up">
					<ArrowUp size={12} />
					{myRank.rankChange}
				</span>
			{:else if myRank.rankChange < 0}
				<span class="my-rank-change down">
					<ArrowDown size={12} />
					{Math.abs(myRank.rankChange)}
				</span>
			{/if}
		</div>
	{/if}

	<!-- Ranking Table (4th place onwards) -->
	<div class="table-wrapper">
		<table class="table table-ranking">
			<thead>
				<tr>
					<th class="rank-col">순위</th>
					<th>유저</th>
					<th class="text-right">{RANKING_TYPE_NAMES[selectedType]}</th>
					<th class="text-right">순위 변동</th>
				</tr>
			</thead>
			<tbody>
				{#each tableEntries as entry, i}
					<tr class="animate-slide-up" style="animation-delay: {i * 30}ms">
						<td class="rank-col">
							<span class="rank-badge {getRankClass(entry.rank)}">
								{entry.rank}
							</span>
						</td>
						<td>
							<span class="ranking-row-avatar">
								{entry.username.charAt(0)}
							</span>
							<span class="font-medium">{entry.username}</span>
						</td>
						<td class="text-right">
							<span
								class="font-bold"
								class:text-stock-up={selectedType === 'RETURN_RATE' && entry.returnRate >= 0}
								class:text-stock-down={selectedType === 'RETURN_RATE' && entry.returnRate < 0}
							>
								{formatValue(entry)}
							</span>
						</td>
						<td class="text-right">
							{#if entry.rankChange > 0}
								<span class="text-success">
									<ArrowUp size={12} style="display:inline-block;vertical-align:middle;" />
									{entry.rankChange}
								</span>
							{:else if entry.rankChange < 0}
								<span class="text-error">
									<ArrowDown size={12} style="display:inline-block;vertical-align:middle;" />
									{Math.abs(entry.rankChange)}
								</span>
							{:else}
								<span class="text-secondary">-</span>
							{/if}
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
</div>
