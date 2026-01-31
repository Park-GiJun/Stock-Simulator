<script lang="ts">
	import { Activity, TrendingUp, TrendingDown, BarChart3 } from 'lucide-svelte';
	import { Card } from '$lib/components/index.js';

	interface MarketData {
		totalStocks: number;
		tradingStocks: number;
		totalVolume: number;
		totalValue: number;
		advancers: number;
		decliners: number;
		unchanged: number;
		marketIndex: number;
		indexChange: number;
		indexChangePercent: number;
	}

	interface Props {
		data?: MarketData | null;
		loading?: boolean;
	}

	let {
		data = null,
		loading = false
	}: Props = $props();

	// Mock data for display
	const mockData: MarketData = {
		totalStocks: 523,
		tradingStocks: 498,
		totalVolume: 125000000,
		totalValue: 2500000000000,
		advancers: 245,
		decliners: 198,
		unchanged: 80,
		marketIndex: 2850.45,
		indexChange: 23.15,
		indexChangePercent: 0.82
	};

	const displayData = $derived(data ?? mockData);

	function formatVolume(volume: number): string {
		if (volume >= 100000000) {
			return (volume / 100000000).toFixed(1) + '억';
		}
		if (volume >= 10000) {
			return (volume / 10000).toFixed(0) + '만';
		}
		return volume.toLocaleString();
	}

	function formatMoney(amount: number): string {
		if (amount >= 1000000000000) {
			return (amount / 1000000000000).toFixed(1) + '조';
		}
		if (amount >= 100000000) {
			return (amount / 100000000).toFixed(0) + '억';
		}
		return amount.toLocaleString();
	}
</script>

<Card>
	{#snippet header()}
		<div class="flex items-center gap-sm">
			<Activity size={20} />
			<h3 class="card-title">시장 현황</h3>
		</div>
	{/snippet}

	{#if loading}
		<div class="market-skeleton">
			<div class="skeleton skeleton-title"></div>
			<div class="market-stats mt-md">
				{#each Array(4) as _, i (i)}
					<div class="skeleton skeleton-text"></div>
				{/each}
			</div>
		</div>
	{:else}
		<div class="market-summary">
			<!-- Market Index -->
			<div class="market-index">
				<div class="market-index-label">종합 지수</div>
				<div class="market-index-value">{displayData.marketIndex.toLocaleString()}</div>
				<div
					class="market-index-change"
					class:up={displayData.indexChange >= 0}
					class:down={displayData.indexChange < 0}
				>
					{#if displayData.indexChange >= 0}
						<TrendingUp size={16} />
					{:else}
						<TrendingDown size={16} />
					{/if}
					{displayData.indexChange >= 0 ? '+' : ''}{displayData.indexChange.toFixed(2)}
					({displayData.indexChangePercent >= 0 ? '+' : ''}{displayData.indexChangePercent.toFixed(2)}%)
				</div>
			</div>

			<!-- Market Breadth -->
			<div class="market-breadth">
				<div class="market-breadth-bar">
					<div
						class="market-breadth-segment up"
						style="width: {(displayData.advancers / displayData.totalStocks) * 100}%"
					></div>
					<div
						class="market-breadth-segment unchanged"
						style="width: {(displayData.unchanged / displayData.totalStocks) * 100}%"
					></div>
					<div
						class="market-breadth-segment down"
						style="width: {(displayData.decliners / displayData.totalStocks) * 100}%"
					></div>
				</div>
				<div class="market-breadth-labels">
					<span class="up">상승 {displayData.advancers}</span>
					<span class="unchanged">보합 {displayData.unchanged}</span>
					<span class="down">하락 {displayData.decliners}</span>
				</div>
			</div>

			<!-- Stats Grid -->
			<div class="market-stats">
				<div class="market-stat">
					<BarChart3 size={16} class="text-secondary" />
					<div class="market-stat-info">
						<div class="market-stat-label">거래대금</div>
						<div class="market-stat-value">₩{formatMoney(displayData.totalValue)}</div>
					</div>
				</div>
				<div class="market-stat">
					<Activity size={16} class="text-secondary" />
					<div class="market-stat-info">
						<div class="market-stat-label">거래량</div>
						<div class="market-stat-value">{formatVolume(displayData.totalVolume)}주</div>
					</div>
				</div>
			</div>
		</div>
	{/if}
</Card>

<style>
	.market-summary {
		display: flex;
		flex-direction: column;
		gap: var(--spacing-md);
	}

	.market-index {
		text-align: center;
		padding: var(--spacing-md);
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-md);
	}

	.market-index-label {
		font-size: var(--font-size-sm);
		color: var(--color-text-secondary);
	}

	.market-index-value {
		font-size: var(--font-size-2xl);
		font-weight: 700;
		font-family: var(--font-mono);
		color: var(--color-text-primary);
		margin: var(--spacing-xs) 0;
	}

	.market-index-change {
		display: inline-flex;
		align-items: center;
		gap: var(--spacing-xs);
		font-size: var(--font-size-sm);
		font-family: var(--font-mono);
	}

	.market-index-change.up {
		color: var(--color-stock-up);
	}

	.market-index-change.down {
		color: var(--color-stock-down);
	}

	.market-breadth {
		display: flex;
		flex-direction: column;
		gap: var(--spacing-xs);
	}

	.market-breadth-bar {
		display: flex;
		height: 8px;
		border-radius: var(--radius-full);
		overflow: hidden;
	}

	.market-breadth-segment {
		height: 100%;
	}

	.market-breadth-segment.up {
		background: var(--color-stock-up);
	}

	.market-breadth-segment.unchanged {
		background: var(--color-text-disabled);
	}

	.market-breadth-segment.down {
		background: var(--color-stock-down);
	}

	.market-breadth-labels {
		display: flex;
		justify-content: space-between;
		font-size: var(--font-size-xs);
	}

	.market-breadth-labels .up {
		color: var(--color-stock-up);
	}

	.market-breadth-labels .unchanged {
		color: var(--color-text-disabled);
	}

	.market-breadth-labels .down {
		color: var(--color-stock-down);
	}

	.market-stats {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: var(--spacing-sm);
	}

	.market-stat {
		display: flex;
		align-items: center;
		gap: var(--spacing-sm);
		padding: var(--spacing-sm);
		background: var(--color-bg-secondary);
		border-radius: var(--radius-md);
	}

	.market-stat-label {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
	}

	.market-stat-value {
		font-weight: 600;
		font-family: var(--font-mono);
		font-size: var(--font-size-sm);
	}

	.market-skeleton {
		padding: var(--spacing-md);
	}

	.skeleton {
		height: 1em;
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-sm);
	}

	.skeleton-title {
		height: 3em;
	}
</style>
