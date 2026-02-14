<script lang="ts">
	import {
		TrendingUp,
		TrendingDown,
		DollarSign,
		BarChart3,
		Newspaper,
		Trophy,
		Activity,
		ArrowUpRight,
		ArrowDownRight,
		Minus
	} from 'lucide-svelte';
	import { Card, MiniSparkline } from '$lib/components';
	import { getTopGainers, getTopLosers, getTopVolume, getMockStocks } from '$lib/mock/stocks.js';
	import { getActiveNews } from '$lib/mock/news.js';
	import { getTopRankers } from '$lib/mock/ranking.js';
	import { currentUser } from '$lib/stores/authStore.js';
	import { isMarketOpen } from '$lib/stores/gameTimeStore.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES } from '$lib/types/news.js';

	const topGainers = getTopGainers(5);
	const topLosers = getTopLosers(5);
	const topVolume = getTopVolume(5);
	const activeNews = getActiveNews().slice(0, 5);
	const topRankers = getTopRankers(5);
	const allStocks = getMockStocks();

	const upCount = allStocks.filter((s) => s.changePercent > 0).length;
	const downCount = allStocks.filter((s) => s.changePercent < 0).length;
	const flatCount = allStocks.filter((s) => s.changePercent === 0).length;
	const totalCount = allStocks.length;

	const sparkData7d = [100, 103, 101, 106, 104, 109, 117];
	const sparkDataHoldings = [8, 8, 9, 9, 10, 10, 10];
	const sparkDataRank = [22, 20, 19, 18, 16, 15, 15];
	const sparkDataProfit = [500000, 550000, 600000, 650000, 700000, 780000, 850000];

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	function formatVolume(volume: number): string {
		if (volume >= 1000000) return (volume / 1000000).toFixed(1) + 'M';
		if (volume >= 1000) return (volume / 1000).toFixed(0) + 'K';
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
	<!-- Welcome Banner -->
	<section class="dashboard-hero animate-slide-up">
		<div class="dashboard-hero-content">
			<div class="dashboard-hero-left">
				<h1 class="dashboard-hero-title">
					{#if $currentUser}
						안녕하세요, <span class="gradient-text">{$currentUser.username}</span>님
					{:else}
						<span class="gradient-text">Stock Simulator</span>에 오신 것을 환영합니다
					{/if}
				</h1>
				<p class="dashboard-hero-subtitle">
					{#if $isMarketOpen}
						장이 운영중입니다. 오늘의 투자 기회를 확인하세요.
					{:else}
						장이 마감되었습니다. 내일의 전략을 준비하세요.
					{/if}
				</p>
			</div>
			<div class="dashboard-hero-right">
				<div class="dashboard-hero-market-bar">
					<div class="market-ratio-bar">
						<div class="market-ratio-up" style="width: {(upCount / totalCount) * 100}%"></div>
						<div class="market-ratio-flat" style="width: {(flatCount / totalCount) * 100}%"></div>
						<div class="market-ratio-down" style="width: {(downCount / totalCount) * 100}%"></div>
					</div>
					<div class="market-ratio-legend">
						<span class="market-ratio-item text-stock-up">
							<ArrowUpRight size={14} /> 상승 {upCount}
						</span>
						<span class="market-ratio-item text-secondary">
							<Minus size={14} /> 보합 {flatCount}
						</span>
						<span class="market-ratio-item text-stock-down">
							<ArrowDownRight size={14} /> 하락 {downCount}
						</span>
					</div>
				</div>
			</div>
		</div>
	</section>

	<!-- User Stats -->
	{#if $currentUser}
		<section class="dashboard-stats grid grid-cols-4 mb-lg">
			<Card hover={true}>
				<div class="card-stat">
					<div class="card-stat-header">
						<div class="card-stat-icon stat-icon-emerald">
							<DollarSign size={16} />
						</div>
						<MiniSparkline data={sparkData7d} color="auto" width={60} height={20} />
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
					<div class="card-stat-header">
						<div class="card-stat-icon stat-icon-violet">
							<BarChart3 size={16} />
						</div>
						<MiniSparkline data={sparkDataHoldings} color="#8b5cf6" width={60} height={20} />
					</div>
					<div class="card-stat-value">10</div>
					<div class="card-stat-label">보유 종목</div>
				</div>
			</Card>
			<Card hover={true}>
				<div class="card-stat">
					<div class="card-stat-header">
						<div class="card-stat-icon stat-icon-amber">
							<Trophy size={16} />
						</div>
						<MiniSparkline data={sparkDataRank} color="#f59e0b" width={60} height={20} />
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
					<div class="card-stat-header">
						<div class="card-stat-icon stat-icon-cyan">
							<TrendingUp size={16} />
						</div>
						<MiniSparkline data={sparkDataProfit} color="auto" width={60} height={20} />
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
				{#each topGainers as stock, i}
					<a href="/stocks/{stock.stockId}" class="stock-row flex justify-between items-center p-sm rounded-md transition-colors">
						<div class="flex items-center gap-sm">
							<span class="stock-row-rank rank-up">{i + 1}</span>
							<div>
								<div class="font-medium">{stock.stockName}</div>
								<div class="text-xs text-secondary">{SECTOR_NAMES[stock.sector]}</div>
							</div>
						</div>
						<div class="text-right">
							<div class="font-semibold">{formatPrice(stock.currentPrice)}</div>
							<div class="text-sm text-stock-up font-medium">{formatPercent(stock.changePercent)}</div>
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
				{#each topLosers as stock, i}
					<a href="/stocks/{stock.stockId}" class="stock-row flex justify-between items-center p-sm rounded-md transition-colors">
						<div class="flex items-center gap-sm">
							<span class="stock-row-rank rank-down">{i + 1}</span>
							<div>
								<div class="font-medium">{stock.stockName}</div>
								<div class="text-xs text-secondary">{SECTOR_NAMES[stock.sector]}</div>
							</div>
						</div>
						<div class="text-right">
							<div class="font-semibold">{formatPrice(stock.currentPrice)}</div>
							<div class="text-sm text-stock-down font-medium">{formatPercent(stock.changePercent)}</div>
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
				{#each topVolume as stock, i}
					<a href="/stocks/{stock.stockId}" class="stock-row flex justify-between items-center p-sm rounded-md transition-colors">
						<div class="flex items-center gap-sm">
							<span class="stock-row-rank">{i + 1}</span>
							<div>
								<div class="font-medium">{stock.stockName}</div>
								<div class="text-xs text-secondary">{formatVolume(stock.volume)}</div>
							</div>
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
							<span class="news-sentiment-dot" class:positive={news.sentiment === 'POSITIVE'} class:negative={news.sentiment === 'NEGATIVE'} class:neutral={news.sentiment === 'NEUTRAL'}></span>
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
						<div class="ranking-row-avatar" style="background: {entry.rank <= 3 ? ['linear-gradient(135deg, #FFD700, #FFA500)', 'linear-gradient(135deg, #C0C0C0, #808080)', 'linear-gradient(135deg, #CD7F32, #8B4513)'][entry.rank - 1] : 'linear-gradient(135deg, #8b5cf6, #7c3aed)'}">
							{entry.username.charAt(0)}
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
