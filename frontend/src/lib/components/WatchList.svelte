<script lang="ts">
	import { Star, TrendingUp, TrendingDown, X } from 'lucide-svelte';
	import { watchlistStore } from '$lib/stores/watchlistStore.js';
	import type { StockListItem } from '$lib/types/stock.js';
	import { SECTOR_NAMES } from '$lib/types/stock.js';

	interface Props {
		stocks: StockListItem[];
		loading?: boolean;
		onSelect?: (stock: StockListItem) => void;
	}

	let {
		stocks,
		loading = false,
		onSelect
	}: Props = $props();

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	function handleRemove(e: MouseEvent, stockId: string) {
		e.stopPropagation();
		watchlistStore.remove(stockId);
	}
</script>

<div class="watchlist">
	<div class="watchlist-header">
		<div class="watchlist-title">
			<Star size={18} class="text-warning" />
			<span>관심종목</span>
		</div>
		<span class="watchlist-count">{stocks.length}개</span>
	</div>

	<div class="watchlist-body">
		{#if loading}
			{#each Array(5) as _, i (i)}
				<div class="watchlist-item skeleton-pulse">
					<div class="skeleton skeleton-text" style="width: 60%"></div>
					<div class="skeleton skeleton-text" style="width: 40%"></div>
				</div>
			{/each}
		{:else if stocks.length === 0}
			<div class="watchlist-empty">
				<Star size={32} class="text-disabled" />
				<p>관심종목이 없습니다.</p>
				<p class="text-sm">종목 상세에서 ★를 눌러 추가하세요.</p>
			</div>
		{:else}
			{#each stocks as stock (stock.stockId)}
				<div
					class="watchlist-item"
					role="button"
					tabindex="0"
					onclick={() => onSelect?.(stock)}
					onkeydown={(e) => e.key === 'Enter' && onSelect?.(stock)}
				>
					<div class="watchlist-item-info">
						<div class="watchlist-item-name">{stock.stockName}</div>
						<div class="watchlist-item-sector">{SECTOR_NAMES[stock.sector]}</div>
					</div>
					<div class="watchlist-item-price">
						<div class="watchlist-item-current">₩{formatPrice(stock.currentPrice)}</div>
						<div
							class="watchlist-item-change"
							class:up={stock.changePercent >= 0}
							class:down={stock.changePercent < 0}
						>
							{#if stock.changePercent >= 0}
								<TrendingUp size={12} />
							{:else}
								<TrendingDown size={12} />
							{/if}
							{formatPercent(stock.changePercent)}
						</div>
					</div>
					<button
						type="button"
						class="watchlist-item-remove"
						onclick={(e) => handleRemove(e, stock.stockId)}
						aria-label="관심종목 해제"
					>
						<X size={14} />
					</button>
				</div>
			{/each}
		{/if}
	</div>
</div>

<style>
	.watchlist {
		background: var(--color-bg-primary);
		border-radius: var(--radius-lg);
		border: 1px solid var(--color-border);
		overflow: hidden;
	}

	.watchlist-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: var(--spacing-sm) var(--spacing-md);
		background: var(--color-bg-tertiary);
		border-bottom: 1px solid var(--color-border);
	}

	.watchlist-title {
		display: flex;
		align-items: center;
		gap: var(--spacing-xs);
		font-weight: 600;
		font-size: var(--font-size-sm);
	}

	.watchlist-count {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
	}

	.watchlist-body {
		max-height: 400px;
		overflow-y: auto;
	}

	.watchlist-item {
		display: flex;
		align-items: center;
		gap: var(--spacing-sm);
		width: 100%;
		padding: var(--spacing-sm) var(--spacing-md);
		border: none;
		background: transparent;
		text-align: left;
		cursor: pointer;
		transition: background var(--transition-fast);
		border-bottom: 1px solid var(--color-border);
	}

	.watchlist-item:last-child {
		border-bottom: none;
	}

	.watchlist-item:hover {
		background: var(--color-bg-hover);
	}

	.watchlist-item-info {
		flex: 1;
		min-width: 0;
	}

	.watchlist-item-name {
		font-weight: 500;
		color: var(--color-text-primary);
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.watchlist-item-sector {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
	}

	.watchlist-item-price {
		text-align: right;
	}

	.watchlist-item-current {
		font-weight: 600;
		font-family: var(--font-mono);
		font-size: var(--font-size-sm);
	}

	.watchlist-item-change {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 2px;
		font-size: var(--font-size-xs);
		font-family: var(--font-mono);
	}

	.watchlist-item-change.up {
		color: var(--color-stock-up);
	}

	.watchlist-item-change.down {
		color: var(--color-stock-down);
	}

	.watchlist-item-remove {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 24px;
		height: 24px;
		padding: 0;
		border: none;
		border-radius: var(--radius-full);
		background: transparent;
		color: var(--color-text-disabled);
		cursor: pointer;
		opacity: 0;
		transition: all var(--transition-fast);
	}

	.watchlist-item:hover .watchlist-item-remove {
		opacity: 1;
	}

	.watchlist-item-remove:hover {
		background: var(--color-bg-tertiary);
		color: var(--color-error);
	}

	.watchlist-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: var(--spacing-sm);
		padding: var(--spacing-xl);
		text-align: center;
		color: var(--color-text-secondary);
	}

	.skeleton {
		height: 1em;
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-sm);
	}
</style>
