<script lang="ts">
	import { Card } from '$lib/components';
	import { getNewsList } from '$lib/mock/news.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES } from '$lib/types/news.js';

	const news = getNewsList();

	function getTimeAgo(dateStr: string): string {
		const date = new Date(dateStr);
		const now = new Date();
		const diffMs = now.getTime() - date.getTime();
		const diffMins = Math.floor(diffMs / 60000);

		if (diffMins < 1) return '방금 전';
		if (diffMins < 60) return `${diffMins}분 전`;
		return `${Math.floor(diffMins / 60)}시간 전`;
	}
</script>

<div class="m-news-page">
	<h1 class="text-lg font-bold mb-md">뉴스</h1>

	<div class="stack-sm">
		{#each news as item}
			<Card hover>
				<div class="flex gap-sm mb-sm flex-wrap">
					<span class="badge badge-{item.level === 'SOCIETY' ? 'error' : item.level === 'INDUSTRY' ? 'info' : 'success'}">
						{EVENT_LEVEL_NAMES[item.level]}
					</span>
					<span
						class="badge badge-outline"
						class:text-success={item.sentiment === 'POSITIVE'}
						class:text-error={item.sentiment === 'NEGATIVE'}
					>
						{SENTIMENT_NAMES[item.sentiment]}
					</span>
				</div>
				<h3 class="font-medium line-clamp-2 mb-xs">{item.headline}</h3>
				<div class="text-xs text-secondary">{getTimeAgo(item.createdAt)}</div>
			</Card>
		{/each}
	</div>
</div>
