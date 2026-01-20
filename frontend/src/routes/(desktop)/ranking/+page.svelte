<script lang="ts">
	import { Card } from '$lib/components';
	import { getRankingByType } from '$lib/mock/ranking.js';
	import { RANKING_TYPE_NAMES, type RankingType } from '$lib/types/ranking.js';

	let selectedType = $state<RankingType>('RETURN_RATE');

	const ranking = $derived(() => getRankingByType(selectedType));

	function formatValue(entry: typeof ranking.entries[0]): string {
		switch (selectedType) {
			case 'RETURN_RATE':
				return `${entry.returnRate >= 0 ? '+' : ''}${entry.returnRate.toFixed(1)}%`;
			case 'TOTAL_ASSET':
				return `₩${entry.totalAsset.toLocaleString()}`;
			case 'TRADING_VOLUME':
				return `₩${(entry.tradingVolume / 1000000).toFixed(0)}M`;
		}
	}

	const types: RankingType[] = ['RETURN_RATE', 'TOTAL_ASSET', 'TRADING_VOLUME'];
</script>

<div class="ranking-page">
	<h1 class="text-2xl font-bold mb-lg">랭킹</h1>

	<!-- Type Selector -->
	<div class="flex gap-sm mb-lg">
		{#each types as type}
			<button
				class="btn btn-md"
				class:btn-primary={selectedType === type}
				class:btn-secondary={selectedType !== type}
				onclick={() => selectedType = type}
			>
				{RANKING_TYPE_NAMES[type]}
			</button>
		{/each}
	</div>

	<!-- Ranking Table -->
	<Card>
		<div class="table-wrapper border-none">
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
					{#each ranking().entries as entry}
						<tr>
							<td class="rank-col">
								<span
									class="rank-badge"
									class:gold={entry.rank === 1}
									class:silver={entry.rank === 2}
									class:bronze={entry.rank === 3}
								>
									{entry.rank}
								</span>
							</td>
							<td>
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
									<span class="text-success">▲{entry.rankChange}</span>
								{:else if entry.rankChange < 0}
									<span class="text-error">▼{Math.abs(entry.rankChange)}</span>
								{:else}
									<span class="text-secondary">-</span>
								{/if}
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</Card>
</div>
