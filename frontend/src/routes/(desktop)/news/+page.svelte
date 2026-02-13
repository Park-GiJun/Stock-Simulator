<script lang="ts">
	import { getNewsList, getNewsById, MOCK_NEWS } from '$lib/mock/news.js';
	import {
		EVENT_LEVEL_NAMES,
		SENTIMENT_NAMES,
		type EventLevel,
		type Sentiment
	} from '$lib/types/news.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';
	import { Modal } from '$lib/components';
	import { Zap, Star } from 'lucide-svelte';
	import type { NewsEvent, NewsListItem } from '$lib/types/news.js';

	const allNews = getNewsList();

	let selectedLevel = $state<EventLevel | 'ALL'>('ALL');
	let selectedSentiment = $state<Sentiment | 'ALL'>('ALL');
	let showModal = $state(false);
	let selectedNews = $state<NewsEvent | null>(null);

	const filteredNews = $derived(() => {
		let result = allNews;
		if (selectedLevel !== 'ALL') {
			result = result.filter((n) => n.level === selectedLevel);
		}
		if (selectedSentiment !== 'ALL') {
			result = result.filter((n) => n.sentiment === selectedSentiment);
		}
		return result;
	});

	const featuredNews = $derived(() => {
		const list = filteredNews();
		return list.length > 0 ? list[0] : null;
	});

	const timelineNews = $derived(() => {
		const list = filteredNews();
		return list.length > 1 ? list.slice(1) : [];
	});

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

	function openNewsDetail(eventId: string) {
		const news = getNewsById(eventId);
		if (news) {
			selectedNews = news;
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

	function getIntensityFromId(eventId: string): number {
		const event = MOCK_NEWS.find((n) => n.eventId === eventId);
		return event?.intensity ?? 0;
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
					onclick={() => (selectedLevel = level)}
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
					onclick={() => (selectedSentiment = sentiment)}
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

	<!-- Featured News -->
	{#if featuredNews()}
		{@const featured = featuredNews()!}
		{@const featuredIntensity = getIntensityFromId(featured.eventId)}
		<div
			class="news-featured"
			onclick={() => openNewsDetail(featured.eventId)}
			onkeydown={(e) => e.key === 'Enter' && openNewsDetail(featured.eventId)}
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
				{#if featured.targetSector}
					<span class="badge badge-secondary">
						{SECTOR_NAMES[featured.targetSector]}
					</span>
				{/if}
				{#if featured.targetStockName}
					<span class="badge badge-secondary">
						{featured.targetStockName}
					</span>
				{/if}
				<span class="news-card-time">{getTimeAgo(featured.createdAt)}</span>

				<div class="news-impact-bar">
					<span class="news-impact-label">
						<Zap size={12} />
						영향력
					</span>
					<div class="news-impact-track">
						<div
							class="news-impact-fill {getImpactLevel(featuredIntensity)}"
							style="width: {getImpactPercent(featuredIntensity)}%"
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
						onclick={() => openNewsDetail(news.eventId)}
						onkeydown={(e) => e.key === 'Enter' && openNewsDetail(news.eventId)}
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
							{#if news.targetSector}
								<span class="badge badge-secondary">
									{SECTOR_NAMES[news.targetSector]}
								</span>
							{/if}
							{#if news.targetStockName}
								<span class="badge badge-secondary">
									{news.targetStockName}
								</span>
							{/if}
						</div>

						<h3 class="news-card-headline">{news.headline}</h3>

						<div class="news-card-bottom">
							<span class="news-card-time">{getTimeAgo(news.createdAt)}</span>

							<div class="news-impact-bar">
								<span class="news-impact-label">
									<Zap size={12} />
									영향력
								</span>
								<div class="news-impact-track">
									<div
										class="news-impact-fill {getImpactLevel(getIntensityFromId(news.eventId))}"
										style="width: {getImpactPercent(getIntensityFromId(news.eventId))}%"
									></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}

	<!-- Empty State -->
	{#if filteredNews().length === 0}
		<div class="news-empty">
			해당 조건의 뉴스가 없습니다.
		</div>
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
				{#if selectedNews.isActive}
					<span class="badge badge-success">진행중</span>
				{:else}
					<span class="badge badge-secondary">종료</span>
				{/if}
			</div>

			<h2 class="news-modal-headline">{selectedNews.headline}</h2>

			<p class="news-modal-body">{selectedNews.content}</p>

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
				{#if selectedNews.targetSector}
					<span class="badge badge-secondary">
						{SECTOR_NAMES[selectedNews.targetSector]}
					</span>
				{/if}
				{#if selectedNews.targetStockName}
					<span class="badge badge-secondary">
						{selectedNews.targetStockName}
					</span>
				{/if}
				<span class="badge badge-outline">
					지속시간: {selectedNews.duration}분
				</span>
				<span class="badge badge-outline">
					{getTimeAgo(selectedNews.createdAt)}
				</span>
			</div>
		</div>
	{/if}
</Modal>
