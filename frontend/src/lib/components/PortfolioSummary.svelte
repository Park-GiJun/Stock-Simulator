<script lang="ts">
	import { TrendingUp, TrendingDown, Wallet, PieChart } from 'lucide-svelte';
	import type { Portfolio } from '$lib/api/tradingApi.js';
	import { Card } from '$lib/components/index.js';

	interface Props {
		portfolio: Portfolio | null;
		loading?: boolean;
	}

	let {
		portfolio,
		loading = false
	}: Props = $props();

	function formatMoney(amount: number): string {
		if (amount >= 100000000) {
			return (amount / 100000000).toFixed(1) + '억';
		}
		if (amount >= 10000) {
			return (amount / 10000).toFixed(0) + '만';
		}
		return amount.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<Card>
	{#snippet header()}
		<div class="flex items-center gap-sm">
			<PieChart size={20} />
			<h3 class="card-title">포트폴리오 요약</h3>
		</div>
	{/snippet}

	{#if loading}
		<div class="portfolio-skeleton">
			<div class="skeleton skeleton-text" style="width: 50%"></div>
			<div class="skeleton skeleton-title mt-sm"></div>
			<div class="portfolio-stats mt-md">
				<div class="skeleton skeleton-text" style="width: 100%"></div>
				<div class="skeleton skeleton-text" style="width: 100%"></div>
			</div>
		</div>
	{:else if portfolio}
		<div class="portfolio-summary">
			<div class="portfolio-total">
				<div class="portfolio-total-label">총 자산</div>
				<div class="portfolio-total-value">₩{formatMoney(portfolio.totalValue)}</div>
				<div
					class="portfolio-total-change"
					class:up={portfolio.totalProfitLoss >= 0}
					class:down={portfolio.totalProfitLoss < 0}
				>
					{#if portfolio.totalProfitLoss >= 0}
						<TrendingUp size={14} />
					{:else}
						<TrendingDown size={14} />
					{/if}
					{formatMoney(Math.abs(portfolio.totalProfitLoss))}
					({formatPercent(portfolio.totalProfitLossPercent)})
				</div>
			</div>

			<div class="portfolio-stats">
				<div class="portfolio-stat">
					<div class="portfolio-stat-icon">
						<Wallet size={16} />
					</div>
					<div class="portfolio-stat-info">
						<div class="portfolio-stat-label">예수금</div>
						<div class="portfolio-stat-value">₩{formatMoney(portfolio.cash)}</div>
					</div>
				</div>
				<div class="portfolio-stat">
					<div class="portfolio-stat-icon">
						<PieChart size={16} />
					</div>
					<div class="portfolio-stat-info">
						<div class="portfolio-stat-label">보유종목</div>
						<div class="portfolio-stat-value">{portfolio.items.length}개</div>
					</div>
				</div>
			</div>

			{#if portfolio.items.length > 0}
				<div class="portfolio-holdings">
					<div class="portfolio-holdings-header">보유 종목</div>
					{#each portfolio.items.slice(0, 5) as item (item.stockId)}
						<div class="portfolio-holding">
							<div class="portfolio-holding-info">
								<div class="portfolio-holding-name">{item.stockName}</div>
								<div class="portfolio-holding-qty">{item.quantity}주</div>
							</div>
							<div class="portfolio-holding-value">
								<div class="portfolio-holding-amount">₩{formatMoney(item.totalValue)}</div>
								<div
									class="portfolio-holding-change"
									class:up={item.profitLoss >= 0}
									class:down={item.profitLoss < 0}
								>
									{formatPercent(item.profitLossPercent)}
								</div>
							</div>
						</div>
					{/each}
					{#if portfolio.items.length > 5}
						<div class="portfolio-holdings-more">
							+{portfolio.items.length - 5}개 더보기
						</div>
					{/if}
				</div>
			{/if}
		</div>
	{:else}
		<div class="portfolio-empty">
			<p>포트폴리오 정보를 불러올 수 없습니다.</p>
		</div>
	{/if}
</Card>

<style>
	.portfolio-summary {
		display: flex;
		flex-direction: column;
		gap: var(--spacing-md);
	}

	.portfolio-total {
		text-align: center;
		padding: var(--spacing-md);
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-md);
	}

	.portfolio-total-label {
		font-size: var(--font-size-sm);
		color: var(--color-text-secondary);
	}

	.portfolio-total-value {
		font-size: var(--font-size-2xl);
		font-weight: 700;
		font-family: var(--font-mono);
		color: var(--color-text-primary);
		margin: var(--spacing-xs) 0;
	}

	.portfolio-total-change {
		display: inline-flex;
		align-items: center;
		gap: var(--spacing-xs);
		font-size: var(--font-size-sm);
		font-family: var(--font-mono);
	}

	.portfolio-total-change.up {
		color: var(--color-stock-up);
	}

	.portfolio-total-change.down {
		color: var(--color-stock-down);
	}

	.portfolio-stats {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: var(--spacing-sm);
	}

	.portfolio-stat {
		display: flex;
		align-items: center;
		gap: var(--spacing-sm);
		padding: var(--spacing-sm);
		background: var(--color-bg-secondary);
		border-radius: var(--radius-md);
	}

	.portfolio-stat-icon {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 32px;
		height: 32px;
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-md);
		color: var(--color-text-secondary);
	}

	.portfolio-stat-label {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
	}

	.portfolio-stat-value {
		font-weight: 600;
		font-family: var(--font-mono);
	}

	.portfolio-holdings {
		border-top: 1px solid var(--color-border);
		padding-top: var(--spacing-md);
	}

	.portfolio-holdings-header {
		font-size: var(--font-size-xs);
		font-weight: 600;
		color: var(--color-text-secondary);
		text-transform: uppercase;
		margin-bottom: var(--spacing-sm);
	}

	.portfolio-holding {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: var(--spacing-xs) 0;
		border-bottom: 1px solid var(--color-border);
	}

	.portfolio-holding:last-of-type {
		border-bottom: none;
	}

	.portfolio-holding-name {
		font-size: var(--font-size-sm);
		font-weight: 500;
	}

	.portfolio-holding-qty {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
	}

	.portfolio-holding-amount {
		font-size: var(--font-size-sm);
		font-family: var(--font-mono);
		text-align: right;
	}

	.portfolio-holding-change {
		font-size: var(--font-size-xs);
		font-family: var(--font-mono);
		text-align: right;
	}

	.portfolio-holding-change.up {
		color: var(--color-stock-up);
	}

	.portfolio-holding-change.down {
		color: var(--color-stock-down);
	}

	.portfolio-holdings-more {
		text-align: center;
		padding: var(--spacing-sm);
		font-size: var(--font-size-sm);
		color: var(--color-text-link);
		cursor: pointer;
	}

	.portfolio-empty {
		text-align: center;
		padding: var(--spacing-lg);
		color: var(--color-text-secondary);
	}

	.portfolio-skeleton {
		padding: var(--spacing-md);
	}

	.skeleton {
		height: 1em;
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-sm);
	}

	.skeleton-title {
		height: 2em;
	}
</style>
