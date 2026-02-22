<script lang="ts">
	import { onMount } from 'svelte';
	import { getNewsList } from '$lib/api/newsApi.js';
	import {
		EVENT_LEVEL_NAMES,
		SENTIMENT_NAMES,
		type EventLevel,
		type Sentiment,
		type NewsArticle
	} from '$lib/types/news.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';
	import { Modal } from '$lib/components';
	import { Zap, Star } from 'lucide-svelte';

	let allNews = $state<NewsArticle[]>([]);
	let loading = $state(true);
	let currentPage = $state(0);
	let totalPages = $state(0);
	let totalElements = $state(0);

	let selectedLevel = $state<EventLevel | 'ALL'>('ALL');
	let selectedSentiment = $state<Sentiment | 'ALL'>('ALL');
	let showModal = $state(false);
	let selectedNews = $state<NewsArticle | null>(null);

	const filteredNews = $derived(() => {
		return allNews;
	});

	const featuredNews = $derived(() => {
		const list = filteredNews();
		return list.length > 0 ? list[0] : null;
	});

	const timelineNews = $derived(() => {
		const list = filteredNews();
		return list.length > 1 ? list.slice(1) : [];
	});

	async function fetchNews() {
		loading = true;
		try {
			const params: Record<string, string | number | undefined> = {
				page: currentPage,
				size: 20
			};
			if (selectedLevel !== 'ALL') params.level = selectedLevel;
			if (selectedSentiment !== 'ALL') params.sentiment = selectedSentiment;

			const res = await getNewsList(params);
			if (res.success && res.data) {
				allNews = res.data.news;
				totalPages = res.data.totalPages;
				totalElements = res.data.totalElements;
			}
		} catch (e) {
			console.error('Failed to fetch news:', e);
		} finally {
			loading = false;
		}
	}

	onMount(() => {
		fetchNews();
	});

	function setLevel(level: EventLevel | 'ALL') {
		selectedLevel = level;
		currentPage = 0;
		fetchNews();
	}

	function setSentiment(sentiment: Sentiment | 'ALL') {
		selectedSentiment = sentiment;
		currentPage = 0;
		fetchNews();
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

	function getSentimentClass(sentiment: Sentiment): string {
		switch (sentiment) {
			case 'POSITIVE':
				return 'sentiment-positive';
			case 'NEGATIVE':
				return 'sentiment-negative';
			default:
				return 'sentiment-neutral';
		}
	}

	function getSentimentDotClass(sentiment: Sentiment): string {
		switch (sentiment) {
			case 'POSITIVE':
				return 'positive';
			case 'NEGATIVE':
				return 'negative';
			default:
				return 'neutral';
		}
	}

	function getImpactLevel(intensity: number): string {
		if (intensity >= 1.0) return 'high';
		if (intensity >= 0.5) return 'medium';
		return 'low';
	}

	function getImpactPercent(intensity: number): number {
		return Math.min((intensity / 2.0) * 100, 100);
	}

	function openNewsDetail(newsId: string) {
		const found = allNews.find((n) => n.newsId === newsId);
		if (found) {
			selectedNews = found;
			showModal = true;
		}
	}

	function getLevelBadgeVariant(level: EventLevel): string {
		switch (level) {
			case 'SOCIETY':
				return 'badge-error';
			case 'INDUSTRY':
				return 'badge-info';
			case 'COMPANY':
				return 'badge-success';
		}
	}

	const levels: (EventLevel | 'ALL')[] = ['ALL', 'SOCIETY', 'INDUSTRY', 'COMPANY'];
	const sentiments: (Sentiment | 'ALL')[] = ['ALL', 'POSITIVE', 'NEGATIVE', 'NEUTRAL'];
</script>

<div class="news-page">
	<div class="news-page-header">
		<h1 class="text-2xl font-bold gradient-text">뉴스</h1>
	</div>

	<!-- Filter Bar -->
	<div class="news-filter-bar">
		<div class="news-filter-group">
			{#each levels as level}
				<button
					class="btn btn-sm"
					class:btn-primary={selectedLevel === level}
					class:btn-secondary={selectedLevel !== level}
					onclick={() => setLevel(level)}
				>
					{level === 'ALL' ? '전체' : EVENT_LEVEL_NAMES[level]}
				</button>
			{/each}
		</div>

		<div class="news-filter-divider"></div>

		<div class="news-filter-group">
			{#each sentiments as sentiment}
				<button
					class="btn btn-sm"
					class:btn-primary={selectedSentiment === sentiment}
					class:btn-secondary={selectedSentiment !== sentiment}
					onclick={() => setSentiment(sentiment)}
				>
					{#if sentiment === 'ALL'}
						전체
					{:else}
						{SENTIMENT_NAMES[sentiment]}
					{/if}
				</button>
			{/each}
		</div>
	</div>

	{#if loading}
		<div class="news-empty">뉴스를 불러오는 중...</div>
	{:else}
		<!-- Featured News -->
		{#if featuredNews()}
			{@const featured = featuredNews()!}
			<div
				class="news-featured"
				onclick={() => openNewsDetail(featured.newsId)}
				onkeydown={(e) => e.key === 'Enter' && openNewsDetail(featured.newsId)}
				role="button"
				tabindex="0"
			>
				<div class="news-featured-label">
					<Star size={14} />
					<span>주요 뉴스</span>
				</div>

				<h2 class="news-featured-headline">{featured.headline}</h2>

				<div class="news-featured-meta">
					<span class="badge {getLevelBadgeVariant(featured.level)}">
						{EVENT_LEVEL_NAMES[featured.level]}
					</span>
					<span
						class="badge badge-outline"
						class:text-success={featured.sentiment === 'POSITIVE'}
						class:text-error={featured.sentiment === 'NEGATIVE'}
						class:text-info={featured.sentiment === 'NEUTRAL'}
					>
						{SENTIMENT_NAMES[featured.sentiment]}
					</span>
					{#if featured.sector}
						<span class="badge badge-secondary">
							{SECTOR_NAMES[featured.sector as Sector] ?? featured.sector}
						</span>
					{/if}
					<span class="news-card-time">{getTimeAgo(featured.publishedAt)}</span>

					<div class="news-impact-bar">
						<span class="news-impact-label">
							<Zap size={12} />
							영향력
						</span>
						<div class="news-impact-track">
							<div
								class="news-impact-fill {getImpactLevel(featured.intensity)}"
								style="width: {getImpactPercent(featured.intensity)}%"
							></div>
						</div>
					</div>
				</div>
			</div>
		{/if}

		<!-- Timeline News -->
		{#if timelineNews().length > 0}
			<div class="news-timeline">
				{#each timelineNews() as news, i}
					<div
						class="news-timeline-item"
						style="animation-delay: {i * 80}ms"
					>
						<div class="news-timeline-dot {getSentimentDotClass(news.sentiment)}"></div>

						<div
							class="news-card-timeline {getSentimentClass(news.sentiment)}"
							onclick={() => openNewsDetail(news.newsId)}
							onkeydown={(e) => e.key === 'Enter' && openNewsDetail(news.newsId)}
							role="button"
							tabindex="0"
						>
							<div class="news-card-badges">
								<span class="badge {getLevelBadgeVariant(news.level)}">
									{EVENT_LEVEL_NAMES[news.level]}
								</span>
								<span
									class="badge badge-outline"
									class:text-success={news.sentiment === 'POSITIVE'}
									class:text-error={news.sentiment === 'NEGATIVE'}
									class:text-info={news.sentiment === 'NEUTRAL'}
								>
									{SENTIMENT_NAMES[news.sentiment]}
								</span>
								{#if news.sector}
									<span class="badge badge-secondary">
										{SECTOR_NAMES[news.sector as Sector] ?? news.sector}
									</span>
								{/if}
							</div>

							<h3 class="news-card-headline">{news.headline}</h3>

							<div class="news-card-bottom">
								<span class="news-card-time">{getTimeAgo(news.publishedAt)}</span>

								<div class="news-impact-bar">
									<span class="news-impact-label">
										<Zap size={12} />
										영향력
									</span>
									<div class="news-impact-track">
										<div
											class="news-impact-fill {getImpactLevel(news.intensity)}"
											style="width: {getImpactPercent(news.intensity)}%"
										></div>
									</div>
								</div>
							</div>
						</div>
					</div>
				{/each}
			</div>
		{/if}

		<!-- Pagination -->
		{#if totalPages > 1}
			<div class="news-pagination" style="display: flex; justify-content: center; gap: var(--spacing-sm); margin-top: var(--spacing-lg);">
				<button
					class="btn btn-sm btn-secondary"
					disabled={currentPage === 0}
					onclick={() => { currentPage--; fetchNews(); }}
				>
					이전
				</button>
				<span class="text-sm text-secondary" style="display: flex; align-items: center;">
					{currentPage + 1} / {totalPages}
				</span>
				<button
					class="btn btn-sm btn-secondary"
					disabled={currentPage >= totalPages - 1}
					onclick={() => { currentPage++; fetchNews(); }}
				>
					다음
				</button>
			</div>
		{/if}

		<!-- Empty State -->
		{#if filteredNews().length === 0}
			<div class="news-empty">
				해당 조건의 뉴스가 없습니다.
			</div>
		{/if}
	{/if}
</div>

<!-- News Detail Modal -->
<Modal bind:isOpen={showModal} title="뉴스 상세">
	{#if selectedNews}
		<div class="news-modal-content">
			<div class="news-card-badges" style="margin-bottom: var(--spacing-md)">
				<span class="badge {getLevelBadgeVariant(selectedNews.level)}">
					{EVENT_LEVEL_NAMES[selectedNews.level]}
				</span>
				<span
					class="badge badge-outline"
					class:text-success={selectedNews.sentiment === 'POSITIVE'}
					class:text-error={selectedNews.sentiment === 'NEGATIVE'}
					class:text-info={selectedNews.sentiment === 'NEUTRAL'}
				>
					{SENTIMENT_NAMES[selectedNews.sentiment]}
				</span>
			</div>

			<h2 class="news-modal-headline">{selectedNews.headline}</h2>

			<div class="news-impact-bar" style="margin-bottom: var(--spacing-md)">
				<span class="news-impact-label">
					<Zap size={14} />
					영향력
				</span>
				<div class="news-impact-track" style="width: 120px">
					<div
						class="news-impact-fill {getImpactLevel(selectedNews.intensity)}"
						style="width: {getImpactPercent(selectedNews.intensity)}%"
					></div>
				</div>
				<span class="news-impact-label">
					{selectedNews.intensity.toFixed(1)} / 2.0
				</span>
			</div>

			<div class="news-modal-tags">
				{#if selectedNews.sector}
					<span class="badge badge-secondary">
						{SECTOR_NAMES[selectedNews.sector as Sector] ?? selectedNews.sector}
					</span>
				{/if}
				<span class="badge badge-outline">
					지속시간: {selectedNews.duration}분
				</span>
				<span class="badge badge-outline">
					{getTimeAgo(selectedNews.publishedAt)}
				</span>
			</div>
		</div>
	{/if}
</Modal>
