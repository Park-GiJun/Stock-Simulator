<script lang="ts">
	import { onMount } from 'svelte';
	import { Search } from 'lucide-svelte';
	import { Card } from '$lib/components';
	import { getStocks } from '$lib/api/stockApi.js';
	import { SECTOR_NAMES, type StockListItem } from '$lib/types/stock.js';

	let searchQuery = $state('');
	let stocks = $state<StockListItem[]>([]);
	let loading = $state(true);

	async function fetchStocks(search?: string) {
		loading = true;
		try {
			const res = await getStocks({
				page: 0,
				size: 50,
				search: search || undefined,
				sortBy: 'stockName',
				sortOrder: 'asc'
			});
			if (res.success && res.data) {
				stocks = res.data.stocks;
			}
		} catch (e) {
			console.error('Failed to fetch stocks:', e);
		} finally {
			loading = false;
		}
	}

	onMount(() => {
		fetchStocks();
	});

	let debounceTimer: ReturnType<typeof setTimeout>;
	function onSearchInput() {
		clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			fetchStocks(searchQuery);
		}, 300);
	}

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
				oninput={onSearchInput}
			/>
		</div>
	</div>

	<!-- Stock List -->
	{#if loading}
		<div class="text-center p-xl text-secondary">로딩 중...</div>
	{:else}
		<div class="stack-sm">
			{#each stocks as stock}
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

		{#if stocks.length === 0}
			<div class="text-center p-xl text-secondary">
				검색 결과가 없습니다
			</div>
		{/if}
	{/if}
</div>
