<script lang="ts">
	import { Card } from '$lib/components';
	import { getMockPortfolio } from '$lib/mock/user.js';
	import { currentUser } from '$lib/stores/authStore.js';

	const portfolio = getMockPortfolio();

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="m-mypage">
	<!-- User Card -->
	{#if $currentUser}
		<div class="mobile-user-card mb-lg">
			<div class="mobile-user-avatar">
				{$currentUser.username.charAt(0)}
			</div>
			<div class="mobile-user-info">
				<div class="mobile-user-name">{$currentUser.username}</div>
				<div class="mobile-user-capital">₩{portfolio.totalAssetValue.toLocaleString()}</div>
				<div class="mobile-user-return" class:positive={portfolio.totalProfitLossPercent >= 0} class:negative={portfolio.totalProfitLossPercent < 0}>
					{formatPercent(portfolio.totalProfitLossPercent)}
				</div>
			</div>
		</div>
	{/if}

	<!-- Stats Grid -->
	<div class="grid grid-cols-2-mobile gap-sm mb-lg">
		<Card>
			<div class="text-center p-sm">
				<div class="text-secondary text-xs mb-xs">보유 현금</div>
				<div class="font-bold">₩{formatPrice(portfolio.cashBalance)}</div>
			</div>
		</Card>
		<Card>
			<div class="text-center p-sm">
				<div class="text-secondary text-xs mb-xs">주식 평가금</div>
				<div class="font-bold">₩{formatPrice(portfolio.totalStockValue)}</div>
			</div>
		</Card>
	</div>

	<!-- Holdings -->
	<h2 class="text-lg font-semibold mb-md">보유 종목</h2>
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
</div>
