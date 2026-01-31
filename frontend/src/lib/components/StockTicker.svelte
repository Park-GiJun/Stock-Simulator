<script lang="ts">
	import type { Candle } from '$lib/types/stock.js';
	import { TrendingUp, TrendingDown } from 'lucide-svelte';
	import StockChart from './StockChart.svelte';

	interface Props {
		stockId: string;
		stockName: string;
		currentPrice: number;
		previousClose: number;
		chartData: Candle[];
	}

	let {
		stockId,
		stockName,
		currentPrice,
		previousClose,
		chartData
	}: Props = $props();

	const change = $derived(currentPrice - previousClose);
	const changePercent = $derived(((currentPrice - previousClose) / previousClose) * 100);
	const direction = $derived(change >= 0 ? 'up' : 'down');

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="stock-ticker">
	<div class="stock-ticker-info">
		<div class="stock-ticker-name">{stockName}</div>
		<div class="stock-ticker-code">{stockId}</div>
	</div>

	<div class="mini-chart">
		<StockChart
			data={chartData}
			width={80}
			height={32}
			compact
			showGrid={false}
			showTooltip={false}
		/>
	</div>

	<div class="stock-ticker-price">
		<div class="stock-ticker-current {direction}">
			₩{formatPrice(currentPrice)}
		</div>
		<div class="stock-ticker-change {direction}">
			{#if direction === 'up'}
				<TrendingUp size={12} />
			{:else}
				<TrendingDown size={12} />
			{/if}
			{formatPercent(changePercent)}
		</div>
	</div>
</div>
