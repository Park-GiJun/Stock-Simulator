<script lang="ts">
	import { TrendingUp, TrendingDown } from 'lucide-svelte';
	import { getTopGainers, getTopLosers, getTopVolume } from '$lib/mock/stocks.js';

	const tickerStocks = [
		...getTopGainers(3).map((s) => ({ ...s, type: 'gainer' as const })),
		...getTopLosers(2).map((s) => ({ ...s, type: 'loser' as const })),
		...getTopVolume(2).map((s) => ({ ...s, type: 'volume' as const }))
	];

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="market-ticker">
	<div class="market-ticker-track">
		{#each [...tickerStocks, ...tickerStocks] as stock, i}
			<a href="/stocks/{stock.stockId}" class="market-ticker-item">
				<span class="market-ticker-name">{stock.stockName}</span>
				<span class="market-ticker-price">{formatPrice(stock.currentPrice)}</span>
				<span
					class="market-ticker-change"
					class:up={stock.changePercent >= 0}
					class:down={stock.changePercent < 0}
				>
					{#if stock.changePercent >= 0}
						<TrendingUp size={12} />
					{:else}
						<TrendingDown size={12} />
					{/if}
					{formatPercent(stock.changePercent)}
				</span>
			</a>
		{/each}
	</div>
</div>
