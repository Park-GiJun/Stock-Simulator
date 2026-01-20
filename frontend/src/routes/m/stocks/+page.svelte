<script lang="ts">
	import { Search } from 'lucide-svelte';
	import { Card } from '$lib/components';
	import { getStockListItems } from '$lib/mock/stocks.js';
	import { SECTOR_NAMES } from '$lib/types/stock.js';

	let searchQuery = $state('');

	const allStocks = getStockListItems();

	const filteredStocks = $derived(() => {
		if (!searchQuery) return allStocks;
		const query = searchQuery.toLowerCase();
		return allStocks.filter(s =>
			s.stockName.toLowerCase().includes(query) ||
			s.stockId.toLowerCase().includes(query)
		);
	});

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="m-stocks-page">
	<!-- Search -->
	<div class="mobile-search mb-md">
		<div class="input-icon-wrapper">
			<span class="input-icon"><Search size={18} /></span>
			<input
				type="search"
				class="input"
				placeholder="종목 검색..."
				bind:value={searchQuery}
			/>
		</div>
	</div>

	<!-- Stock List -->
	<div class="stack-sm">
		{#each filteredStocks() as stock}
			<a href="/m/stocks/{stock.stockId}">
				<Card hover clickable>
					<div class="flex justify-between items-center">
						<div class="flex items-center gap-sm">
							<div class="stock-icon w-10 h-10 rounded-md bg-tertiary flex items-center justify-center font-semibold text-primary">
								{stock.stockName.charAt(0)}
							</div>
							<div>
								<div class="font-medium">{stock.stockName}</div>
								<div class="text-xs text-secondary">{SECTOR_NAMES[stock.sector]}</div>
							</div>
						</div>
						<div class="text-right">
							<div class="font-semibold">₩{formatPrice(stock.currentPrice)}</div>
							<div
								class="text-sm"
								class:text-stock-up={stock.changePercent >= 0}
								class:text-stock-down={stock.changePercent < 0}
							>
								{formatPercent(stock.changePercent)}
							</div>
						</div>
					</div>
				</Card>
			</a>
		{/each}
	</div>

	{#if filteredStocks().length === 0}
		<div class="text-center p-xl text-secondary">
			검색 결과가 없습니다
		</div>
	{/if}
</div>
