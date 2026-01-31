<script lang="ts">
	import { History, ArrowUpRight, ArrowDownRight } from 'lucide-svelte';
	import type { Trade } from '$lib/types/stock.js';

	interface Props {
		trades: Trade[];
		loading?: boolean;
		maxItems?: number;
	}

	let {
		trades,
		loading = false,
		maxItems = 20
	}: Props = $props();

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatTime(timestamp: string): string {
		const date = new Date(timestamp);
		return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
	}

	function formatDate(timestamp: string): string {
		const date = new Date(timestamp);
		return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
	}
</script>

<div class="trade-history">
	<div class="trade-history-header">
		<History size={18} />
		<span>체결 내역</span>
	</div>

	<div class="trade-history-body">
		{#if loading}
			{#each Array(5) as _, i (i)}
				<div class="trade-history-item skeleton-row">
					<div class="skeleton skeleton-text" style="width: 30%"></div>
					<div class="skeleton skeleton-text" style="width: 25%"></div>
					<div class="skeleton skeleton-text" style="width: 20%"></div>
				</div>
			{/each}
		{:else if trades.length === 0}
			<div class="trade-history-empty">
				<History size={32} class="text-disabled" />
				<p>체결 내역이 없습니다.</p>
			</div>
		{:else}
			<div class="trade-history-list">
				{#each trades.slice(0, maxItems) as trade (trade.tradeId)}
					<div class="trade-history-item" class:buy={trade.side === 'BUY'} class:sell={trade.side === 'SELL'}>
						<div class="trade-history-side">
							{#if trade.side === 'BUY'}
								<ArrowDownRight size={14} class="icon-buy" />
								<span class="side-label buy">매수</span>
							{:else}
								<ArrowUpRight size={14} class="icon-sell" />
								<span class="side-label sell">매도</span>
							{/if}
						</div>
						<div class="trade-history-price">
							<span class="price">₩{formatPrice(trade.price)}</span>
							<span class="quantity">{trade.quantity}주</span>
						</div>
						<div class="trade-history-time">
							<span class="time">{formatTime(trade.timestamp)}</span>
							<span class="date">{formatDate(trade.timestamp)}</span>
						</div>
					</div>
				{/each}
			</div>
		{/if}
	</div>
</div>

<style>
	.trade-history {
		background: var(--color-bg-primary);
		border-radius: var(--radius-lg);
		border: 1px solid var(--color-border);
		overflow: hidden;
	}

	.trade-history-header {
		display: flex;
		align-items: center;
		gap: var(--spacing-xs);
		padding: var(--spacing-sm) var(--spacing-md);
		background: var(--color-bg-tertiary);
		border-bottom: 1px solid var(--color-border);
		font-weight: 600;
		font-size: var(--font-size-sm);
	}

	.trade-history-body {
		max-height: 400px;
		overflow-y: auto;
	}

	.trade-history-list {
		display: flex;
		flex-direction: column;
	}

	.trade-history-item {
		display: grid;
		grid-template-columns: 60px 1fr 80px;
		align-items: center;
		gap: var(--spacing-sm);
		padding: var(--spacing-sm) var(--spacing-md);
		border-bottom: 1px solid var(--color-border);
		font-size: var(--font-size-sm);
	}

	.trade-history-item:last-child {
		border-bottom: none;
	}

	.trade-history-side {
		display: flex;
		align-items: center;
		gap: var(--spacing-xs);
	}

	:global(.icon-buy) {
		color: var(--color-stock-down);
	}

	:global(.icon-sell) {
		color: var(--color-stock-up);
	}

	.side-label {
		font-weight: 600;
		font-size: var(--font-size-xs);
	}

	.side-label.buy {
		color: var(--color-stock-down);
	}

	.side-label.sell {
		color: var(--color-stock-up);
	}

	.trade-history-price {
		display: flex;
		flex-direction: column;
	}

	.price {
		font-weight: 600;
		font-family: var(--font-mono);
	}

	.quantity {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
	}

	.trade-history-time {
		display: flex;
		flex-direction: column;
		text-align: right;
	}

	.time {
		font-family: var(--font-mono);
		font-size: var(--font-size-xs);
	}

	.date {
		font-size: 10px;
		color: var(--color-text-disabled);
	}

	.trade-history-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: var(--spacing-sm);
		padding: var(--spacing-xl);
		text-align: center;
		color: var(--color-text-secondary);
	}

	.skeleton-row {
		display: flex;
		justify-content: space-between;
	}

	.skeleton {
		height: 1em;
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-sm);
	}
</style>
