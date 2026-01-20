<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		variant?: 'default' | 'flat' | 'elevated' | 'outlined';
		hover?: boolean;
		clickable?: boolean;
		onclick?: () => void;
		header?: Snippet;
		children: Snippet;
		footer?: Snippet;
	}

	let {
		variant = 'default',
		hover = false,
		clickable = false,
		onclick,
		header,
		children,
		footer
	}: Props = $props();

	const classes = $derived(() => {
		const base = 'card';
		const variantClass = variant !== 'default' ? `card-${variant}` : '';
		const hoverClass = hover ? 'card-hover' : '';
		const clickableClass = clickable ? 'card-clickable' : '';
		return [base, variantClass, hoverClass, clickableClass].filter(Boolean).join(' ');
	});
</script>

{#if clickable}
	<button class={classes()} onclick={onclick} type="button">
		{#if header}
			<div class="card-header">
				{@render header()}
			</div>
		{/if}
		<div class="card-body">
			{@render children()}
		</div>
		{#if footer}
			<div class="card-footer">
				{@render footer()}
			</div>
		{/if}
	</button>
{:else}
	<div class={classes()}>
		{#if header}
			<div class="card-header">
				{@render header()}
			</div>
		{/if}
		<div class="card-body">
			{@render children()}
		</div>
		{#if footer}
			<div class="card-footer">
				{@render footer()}
			</div>
		{/if}
	</div>
{/if}
