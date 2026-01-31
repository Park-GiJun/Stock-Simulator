<script lang="ts">
	import { Loader2 } from 'lucide-svelte';

	interface Props {
		loading?: boolean;
		hasMore?: boolean;
		threshold?: number;
		onLoadMore: () => void;
	}

	let {
		loading = false,
		hasMore = true,
		threshold = 200,
		onLoadMore
	}: Props = $props();

	let sentinelRef = $state<HTMLDivElement | null>(null);

	$effect(() => {
		if (!sentinelRef) return;

		const observer = new IntersectionObserver(
			(entries) => {
				const [entry] = entries;
				if (entry.isIntersecting && hasMore && !loading) {
					onLoadMore();
				}
			},
			{
				rootMargin: `${threshold}px`
			}
		);

		observer.observe(sentinelRef);

		return () => {
			observer.disconnect();
		};
	});
</script>

<div class="infinite-scroll-sentinel" bind:this={sentinelRef}>
	{#if loading}
		<div class="infinite-scroll-loading">
			<Loader2 size={24} class="animate-spin" />
			<span>불러오는 중...</span>
		</div>
	{:else if !hasMore}
		<div class="infinite-scroll-end">
			<span>모든 항목을 불러왔습니다.</span>
		</div>
	{/if}
</div>

<style>
	.infinite-scroll-sentinel {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: var(--spacing-lg);
		min-height: 60px;
	}

	.infinite-scroll-loading {
		display: flex;
		align-items: center;
		gap: var(--spacing-sm);
		color: var(--color-text-secondary);
		font-size: var(--font-size-sm);
	}

	.infinite-scroll-end {
		color: var(--color-text-disabled);
		font-size: var(--font-size-sm);
	}

	:global(.animate-spin) {
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		from { transform: rotate(0deg); }
		to { transform: rotate(360deg); }
	}
</style>
