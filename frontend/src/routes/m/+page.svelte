<script lang="ts">
	import { TrendingUp, TrendingDown, ChevronRight } from 'lucide-svelte';
	import { Card } from '$lib/components';
	import { getTopGainers, getTopLosers, getStockListItems } from '$lib/mock/stocks.js';
	import { getActiveNews } from '$lib/mock/news.js';
	import { currentUser } from '$lib/stores/authStore.js';
	import { SECTOR_NAMES } from '$lib/types/stock.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES } from '$lib/types/news.js';

	const topGainers = getTopGainers(3);
	const topLosers = getTopLosers(3);
	const stocks = getStockListItems().slice(0, 5);
	const activeNews = getActiveNews().slice(0, 3);

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="m-dashboard">
	<!-- User Card -->
	{#if $currentUser}
		<div class="mobile-user-card mb-md">
			<div class="mobile-user-avatar">
				{$currentUser.username.charAt(0)}
			</div>
			<div class="mobile-user-info">
				<div class="mobile-user-name">{$currentUser.username}</div>
				<div class="mobile-user-capital">₩{$currentUser.capital.toLocaleString()}</div>
				<div class="mobile-user-return positive">+17.0%</div>
			</div>
		</div>
	{/if}

	<!-- Quick Actions -->
	<div class="grid grid-cols-2-mobile gap-sm mb-lg">
		<a href="/m/stocks" class="btn btn-outline btn-md btn-full flex items-center justify-center gap-sm">
			<TrendingUp size={18} />
			주식 투자
		</a>
		<a href="/m/mypage" class="btn btn-outline btn-md btn-full flex items-center justify-center gap-sm">
			포트폴리오
		</a>
	</div>

	<!-- Top Movers -->
	<section class="mb-lg">
		<div class="flex justify-between items-center mb-md">
			<h2 class="text-lg font-semibold">오늘의 시장</h2>
		</div>

		<div class="grid grid-cols-2-mobile gap-sm">
			<!-- Gainers -->
			<Card>
				{#snippet header()}
					<div class="flex items-center gap-xs">
						<TrendingUp size={16} class="text-stock-up" />
						<span class="text-sm font-medium">상승</span>
					</div>
				{/snippet}

				<div class="stack-sm">
					{#each topGainers as stock}
						<a href="/m/stocks/{stock.stockId}" class="flex justify-between items-center">
							<span class="text-sm truncate">{stock.stockName}</span>
							<span class="text-sm text-stock-up font-medium">{formatPercent(stock.changePercent)}</span>
						</a>
					{/each}
				</div>
			</Card>

			<!-- Losers -->
			<Card>
				{#snippet header()}
					<div class="flex items-center gap-xs">
						<TrendingDown size={16} class="text-stock-down" />
						<span class="text-sm font-medium">하락</span>
					</div>
				{/snippet}

				<div class="stack-sm">
					{#each topLosers as stock}
						<a href="/m/stocks/{stock.stockId}" class="flex justify-between items-center">
							<span class="text-sm truncate">{stock.stockName}</span>
							<span class="text-sm text-stock-down font-medium">{formatPercent(stock.changePercent)}</span>
						</a>
					{/each}
				</div>
			</Card>
		</div>
	</section>

	<!-- Stock List -->
	<section class="mb-lg">
		<div class="flex justify-between items-center mb-md">
			<h2 class="text-lg font-semibold">종목 목록</h2>
			<a href="/m/stocks" class="text-sm text-link flex items-center gap-xs">
				더보기 <ChevronRight size={14} />
			</a>
		</div>

		<div class="stack-sm">
			{#each stocks as stock}
				<a href="/m/stocks/{stock.stockId}">
					<Card hover clickable>
						<div class="flex justify-between items-center">
							<div>
								<div class="font-medium">{stock.stockName}</div>
								<div class="text-xs text-secondary">{SECTOR_NAMES[stock.sector]}</div>
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
	</section>

	<!-- Recent News -->
	<section>
		<div class="flex justify-between items-center mb-md">
			<h2 class="text-lg font-semibold">최신 뉴스</h2>
			<a href="/m/news" class="text-sm text-link flex items-center gap-xs">
				더보기 <ChevronRight size={14} />
			</a>
		</div>

		<div class="stack-sm">
			{#each activeNews as news}
				<Card hover>
					<div class="flex items-start gap-sm">
						<span class="badge badge-{news.level === 'SOCIETY' ? 'error' : news.level === 'INDUSTRY' ? 'info' : 'success'} flex-shrink-0">
							{EVENT_LEVEL_NAMES[news.level]}
						</span>
						<div class="flex-1 min-w-0">
							<div class="font-medium line-clamp-2">{news.headline}</div>
						</div>
					</div>
				</Card>
			{/each}
		</div>
	</section>
</div>
