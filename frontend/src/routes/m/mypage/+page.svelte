<script lang="ts">
	import { onMount } from 'svelte';
	import { Card } from '$lib/components';
	import { getEnrichedPortfolio } from '$lib/api/portfolioHelper.js';
	import { currentUser } from '$lib/stores/authStore.js';
	import type { Portfolio } from '$lib/types/user.js';

	let portfolio = $state<Portfolio | null>(null);
	let loading = $state(true);

	onMount(async () => {
		if (!$currentUser) {
			loading = false;
			return;
		}

		try {
			portfolio = await getEnrichedPortfolio(String($currentUser.userId));
		} catch (e) {
			console.error('Failed to fetch portfolio:', e);
		}

		loading = false;
	});

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="m-mypage">
	{#if !$currentUser}
		<div class="text-center p-lg">
			<p class="text-secondary">로그인이 필요합니다.</p>
			<a href="/m/login" class="btn btn-primary mt-md">로그인</a>
		</div>
	{:else if loading}
		<div class="text-center p-lg">
			<div class="text-secondary">로딩 중...</div>
		</div>
	{:else}
		<!-- User Card -->
		<div class="mobile-user-card mb-lg">
			<div class="mobile-user-avatar">
				{$currentUser.username.charAt(0)}
			</div>
			<div class="mobile-user-info">
				<div class="mobile-user-name">{$currentUser.username}</div>
				<div class="mobile-user-capital">₩{portfolio ? formatPrice(portfolio.totalAssetValue) : ($currentUser.capital?.toLocaleString() ?? '0')}</div>
				{#if portfolio}
					<div class="mobile-user-return" class:positive={portfolio.totalProfitLossPercent >= 0} class:negative={portfolio.totalProfitLossPercent < 0}>
						{formatPercent(portfolio.totalProfitLossPercent)}
					</div>
				{/if}
			</div>
		</div>

		<!-- Stats Grid -->
		<div class="grid grid-cols-2-mobile gap-sm mb-lg">
			<Card>
				<div class="text-center p-sm">
					<div class="text-secondary text-xs mb-xs">보유 현금</div>
					<div class="font-bold">₩{formatPrice(portfolio?.cashBalance ?? 0)}</div>
				</div>
			</Card>
			<Card>
				<div class="text-center p-sm">
					<div class="text-secondary text-xs mb-xs">주식 평가금</div>
					<div class="font-bold">₩{formatPrice(portfolio?.totalStockValue ?? 0)}</div>
				</div>
			</Card>
		</div>

		<!-- Holdings -->
		<h2 class="text-lg font-semibold mb-md">보유 종목</h2>
		{#if portfolio && portfolio.holdings.length > 0}
			<div class="stack-sm">
				{#each portfolio.holdings as holding}
					<a href="/m/stocks/{holding.stockId}">
						<Card hover clickable>
							<div class="flex justify-between items-center">
								<div>
									<div class="font-medium">{holding.stockName}</div>
									<div class="text-xs text-secondary">{holding.quantity}주 · 평균 ₩{formatPrice(holding.averagePrice)}</div>
								</div>
								<div class="text-right">
									<div class="font-semibold">₩{formatPrice(holding.totalValue)}</div>
									<div
										class="text-sm"
										class:text-stock-up={holding.profitLoss >= 0}
										class:text-stock-down={holding.profitLoss < 0}
									>
										{holding.profitLoss >= 0 ? '+' : ''}₩{formatPrice(holding.profitLoss)}
									</div>
								</div>
							</div>
						</Card>
					</a>
				{/each}
			</div>
		{:else}
			<div class="text-center text-secondary p-lg">보유 종목이 없습니다</div>
		{/if}
	{/if}
</div>
