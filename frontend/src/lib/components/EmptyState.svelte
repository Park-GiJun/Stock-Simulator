<script lang="ts">
	import { Inbox, Search, FileX, AlertCircle } from 'lucide-svelte';
	import type { Snippet } from 'svelte';

	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	type IconComponent = any;

	interface Props {
		type?: 'empty' | 'no-results' | 'error' | 'custom';
		title?: string;
		description?: string;
		icon?: IconComponent;
		action?: Snippet;
	}

	let {
		type = 'empty',
		title,
		description,
		icon,
		action
	}: Props = $props();

	const defaultContent: Record<string, { icon: IconComponent; title: string; description: string }> = {
		empty: {
			icon: Inbox,
			title: '데이터가 없습니다',
			description: '표시할 항목이 없습니다.'
		},
		'no-results': {
			icon: Search,
			title: '검색 결과가 없습니다',
			description: '다른 검색어로 다시 시도해 보세요.'
		},
		error: {
			icon: AlertCircle,
			title: '오류가 발생했습니다',
			description: '잠시 후 다시 시도해 주세요.'
		},
		custom: {
			icon: FileX,
			title: '',
			description: ''
		}
	};

	const content = $derived(defaultContent[type]);
	const IconComponent = $derived(icon ?? content.icon);
	const displayTitle = $derived(title ?? content.title);
	const displayDescription = $derived(description ?? content.description);
</script>

<div class="empty-state">
	<div class="empty-state-icon">
		<IconComponent size={48} />
	</div>
	{#if displayTitle}
		<h3 class="empty-state-title">{displayTitle}</h3>
	{/if}
	{#if displayDescription}
		<p class="empty-state-description">{displayDescription}</p>
	{/if}
	{#if action}
		<div class="empty-state-action">
			{@render action()}
		</div>
	{/if}
</div>

<style>
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		text-align: center;
		padding: var(--spacing-2xl);
		min-height: 200px;
	}

	.empty-state-icon {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 80px;
		height: 80px;
		margin-bottom: var(--spacing-md);
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-full);
		color: var(--color-text-disabled);
	}

	.empty-state-title {
		font-size: var(--font-size-lg);
		font-weight: 600;
		color: var(--color-text-primary);
		margin-bottom: var(--spacing-xs);
	}

	.empty-state-description {
		font-size: var(--font-size-sm);
		color: var(--color-text-secondary);
		max-width: 300px;
		margin-bottom: var(--spacing-md);
	}

	.empty-state-action {
		margin-top: var(--spacing-sm);
	}
</style>
