<script lang="ts">
	import { getNewsList } from '$lib/mock/news.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES, type EventLevel, type Sentiment } from '$lib/types/news.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';

	const allNews = getNewsList();

	let selectedLevel = $state<EventLevel | 'ALL'>('ALL');

	const filteredNews = $derived(() => {
		if (selectedLevel === 'ALL') return allNews;
		return allNews.filter(n => n.level === selectedLevel);
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
			case 'POSITIVE': return 'sentiment-positive';
			case 'NEGATIVE': return 'sentiment-negative';
			default: return 'sentiment-neutral';
		}
	}

	const levels: (EventLevel | 'ALL')[] = ['ALL', 'SOCIETY', 'INDUSTRY', 'COMPANY'];
</script>

<div class="news-page">
	<h1 class="text-2xl font-bold mb-lg gradient-text">뉴스</h1>

	<!-- Filters -->
	<div class="flex gap-sm mb-lg flex-wrap">
		{#each levels as level}
			<button
				class="btn btn-sm"
				class:btn-primary={selectedLevel === level}
				class:btn-secondary={selectedLevel !== level}
				onclick={() => selectedLevel = level}
			>
				{level === 'ALL' ? '전체' : EVENT_LEVEL_NAMES[level]}
			</button>
		{/each}
	</div>

	<!-- News List -->
	<div class="news-grid">
		{#each filteredNews() as news, i}
			<div class="card news-card {getSentimentClass(news.sentiment)} card-hover animate-slide-up" style="animation-delay: {i * 50}ms">
				<div class="card-body">
					<div class="flex gap-sm mb-sm flex-wrap">
						<span class="badge badge-{news.level === 'SOCIETY' ? 'error' : news.level === 'INDUSTRY' ? 'info' : 'success'}">
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
					<h3 class="card-news-headline">{news.headline}</h3>
					<div class="card-news-meta">
						<span>{getTimeAgo(news.createdAt)}</span>
					</div>
				</div>
			</div>
		{/each}
	</div>

	{#if filteredNews().length === 0}
		<div class="card glass-subtle text-center p-2xl text-secondary animate-fade-in">
			해당 조건의 뉴스가 없습니다.
		</div>
	{/if}
</div>
