<script lang="ts">
	import { Card } from '$lib/components';
	import { getTopRankers } from '$lib/mock/ranking.js';

	const rankers = getTopRankers(10);
</script>

<div class="m-ranking-page">
	<h1 class="text-lg font-bold mb-md">랭킹</h1>

	<div class="stack-sm">
		{#each rankers as entry}
			<Card>
				<div class="card-ranking">
					<div
						class="card-ranking-rank"
						class:gold={entry.rank === 1}
						class:silver={entry.rank === 2}
						class:bronze={entry.rank === 3}
						class:default={entry.rank > 3}
					>
						{entry.rank}
					</div>
					<div class="card-ranking-info">
						<div class="card-ranking-name">{entry.username}</div>
					</div>
					<div class="card-ranking-value">
						<div class="card-ranking-return" class:positive={entry.returnRate >= 0} class:negative={entry.returnRate < 0}>
							{entry.returnRate >= 0 ? '+' : ''}{entry.returnRate.toFixed(1)}%
						</div>
					</div>
				</div>
			</Card>
		{/each}
	</div>
</div>
