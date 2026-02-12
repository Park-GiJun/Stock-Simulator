<script lang="ts">
	import { TrendingUp, TrendingDown, DollarSign, BarChart3, Newspaper, Trophy } from 'lucide-svelte';
	import { Card } from '$lib/components';
	import { getTopGainers, getTopLosers, getTopVolume } from '$lib/mock/stocks.js';
	import { getActiveNews } from '$lib/mock/news.js';
	import { getTopRankers } from '$lib/mock/ranking.js';
	import { currentUser } from '$lib/stores/authStore.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES } from '$lib/types/news.js';

	const topGainers = getTopGainers(5);
	const topLosers = getTopLosers(5);
	const topVolume = getTopVolume(5);
	const activeNews = getActiveNews().slice(0, 5);
	const topRankers = getTopRankers(5);

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	function formatVolume(volume: number): string {
		if (volume >= 1000000) {
			return (volume / 1000000).toFixed(1) + 'M';
		}
		if (volume >= 1000) {
			return (volume / 1000).toFixed(0) + 'K';
		}
		return volume.toString();
	}

	function getTimeAgo(dateStr: string): string {
		const date = new Date(dateStr);
		const now = new Date();
		const diffMs = now.getTime() - date.getTime();
		const diffMins = Math.floor(diffMs / 60000);

		if (diffMins < 1) return '방금 전';
		if (diffMins < 60) return `${diffMins}분 전`;
		if (diffMins < 1440) return `${Math.floor(diffMins / 60)}시간 전`;
		return `${Math.floor(diffMins / 1440)}일 전`;
	}
</script>

<div class="dashboard">
	<!-- User Stats -->
	{#if $currentUser}
		<section class="dashboard-stats grid grid-cols-4 mb-lg">
			<Card hover={true}>
				<div class="card-stat">
					<div class="card-stat-icon stat-icon-emerald">
						<DollarSign size={24} />
					</div>
					<div class="card-stat-value">{$currentUser.capital?.toLocaleString() ?? '0'}</div>
					<div class="card-stat-label">보유 자산</div>
					<div class="card-stat-change up">
						<TrendingUp size={12} />
						+17.0%
					</div>
				</div>
			</Card>
			<Card hover={true}>
				<div class="card-stat">
					<div class="card-stat-icon stat-icon-violet">
						<BarChart3 size={24} />
					</div>
					<div class="card-stat-value">10</div>
					<div class="card-stat-label">보유 종목</div>
				</div>
			</Card>
			<Card hover={true}>
				<div class="card-stat">
					<div class="card-stat-icon stat-icon-amber">
						<Trophy size={24} />
					</div>
					<div class="card-stat-value">#15</div>
					<div class="card-stat-label">현재 순위</div>
					<div class="card-stat-change up">
						<TrendingUp size={12} />
						+3
					</div>
				</div>
			</Card>
			<Card hover={true}>
				<div class="card-stat">
					<div class="card-stat-icon stat-icon-cyan">
						<TrendingUp size={24} />
					</div>
					<div class="card-stat-value">850,000</div>
					<div class="card-stat-label">총 수익</div>
				</div>
			</Card>
		</section>
	{/if}

	<div class="dashboard-market-cards grid grid-cols-3 gap-lg">
		<!-- Top Gainers -->
		<Card hover={true}>
			{#snippet header()}
				<div class="dashboard-section-title">
					<TrendingUp size={20} class="text-stock-up" />
					<h3 class="card-title gradient-text">상승 TOP</h3>
				</div>
			{/snippet}

			<div class="stack-sm">
				{#each topGainers as stock}
					<a href="/stocks/{stock.stockId}" class="stock-row flex justify-between items-center p-sm rounded-md transition-colors">
						<div>
							<div class="font-medium">{stock.stockName}</div>
							<div class="text-xs text-secondary">{SECTOR_NAMES[stock.sector]}</div>
						</div>
						<div class="text-right">
							<div class="font-semibold">{formatPrice(stock.currentPrice)}</div>
							<div class="text-sm text-stock-up">{formatPercent(stock.changePercent)}</div>
						</div>
					</a>
				{/each}
			</div>
		</Card>

		<!-- Top Losers -->
		<Card hover={true}>
			{#snippet header()}
				<div class="dashboard-section-title">
					<TrendingDown size={20} class="text-stock-down" />
					<h3 class="card-title gradient-text">하락 TOP</h3>
				</div>
			{/snippet}

			<div class="stack-sm">
				{#each topLosers as stock}
					<a href="/stocks/{stock.stockId}" class="stock-row flex justify-between items-center p-sm rounded-md transition-colors">
						<div>
							<div class="font-medium">{stock.stockName}</div>
							<div class="text-xs text-secondary">{SECTOR_NAMES[stock.sector]}</div>
						</div>
						<div class="text-right">
							<div class="font-semibold">{formatPrice(stock.currentPrice)}</div>
							<div class="text-sm text-stock-down">{formatPercent(stock.changePercent)}</div>
						</div>
					</a>
				{/each}
			</div>
		</Card>

		<!-- Top Volume -->
		<Card hover={true}>
			{#snippet header()}
				<div class="dashboard-section-title">
					<BarChart3 size={20} class="text-info" />
					<h3 class="card-title gradient-text">거래량 TOP</h3>
				</div>
			{/snippet}

			<div class="stack-sm">
				{#each topVolume as stock}
					<a href="/stocks/{stock.stockId}" class="stock-row flex justify-between items-center p-sm rounded-md transition-colors">
						<div>
							<div class="font-medium">{stock.stockName}</div>
							<div class="text-xs text-secondary">{formatVolume(stock.volume)}</div>
						</div>
						<div class="text-right">
							<div class="font-semibold">{formatPrice(stock.currentPrice)}</div>
							<div class="text-sm" class:text-stock-up={stock.changePercent >= 0} class:text-stock-down={stock.changePercent < 0}>
								{formatPercent(stock.changePercent)}
							</div>
						</div>
					</a>
				{/each}
			</div>
		</Card>
	</div>

	<!-- News & Ranking -->
	<div class="dashboard-bottom grid grid-cols-2 gap-lg mt-lg">
		<!-- Recent News -->
		<Card hover={true}>
			{#snippet header()}
				<div class="dashboard-section-title">
					<Newspaper size={20} />
					<h3 class="card-title">최신 뉴스</h3>
				</div>
				<a href="/news" class="btn btn-ghost btn-sm">전체보기</a>
			{/snippet}

			<div class="stack">
				{#each activeNews as news}
					<div class="news-item">
						<div class="flex items-center gap-sm mb-xs">
							<span class="badge badge-{news.level === 'SOCIETY' ? 'error' : news.level === 'INDUSTRY' ? 'info' : 'success'}">
								{EVENT_LEVEL_NAMES[news.level]}
							</span>
							<span class="badge badge-outline" class:text-success={news.sentiment === 'POSITIVE'} class:text-error={news.sentiment === 'NEGATIVE'}>
								{SENTIMENT_NAMES[news.sentiment]}
							</span>
						</div>
						<div class="font-medium line-clamp-1">{news.headline}</div>
						<div class="text-xs text-secondary mt-xs">{getTimeAgo(news.createdAt)}</div>
					</div>
				{/each}
			</div>
		</Card>

		<!-- Top Ranking -->
		<Card hover={true}>
			{#snippet header()}
				<div class="dashboard-section-title">
					<Trophy size={20} class="text-warning" />
					<h3 class="card-title">수익률 랭킹</h3>
				</div>
				<a href="/ranking" class="btn btn-ghost btn-sm">전체보기</a>
			{/snippet}

			<div class="stack-sm">
				{#each topRankers as entry}
					<div class="card-ranking">
						<div
							class="card-ranking-rank"
							class:gold={entry.rank === 1}
							class:silver={entry.rank === 2}
							class:bronze={entry.rank === 3}
							class:default={entry.rank > 3}
						>
							{entry.rank}
						</div>
						<div class="card-ranking-info">
							<div class="card-ranking-name">{entry.username}</div>
						</div>
						<div class="card-ranking-value">
							<div class="card-ranking-return" class:positive={entry.returnRate >= 0} class:negative={entry.returnRate < 0}>
								{entry.returnRate >= 0 ? '+' : ''}{entry.returnRate.toFixed(1)}%
							</div>
						</div>
					</div>
				{/each}
			</div>
		</Card>
	</div>
</div>
