<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		type?: 'primary' | 'secondary' | 'accent' | 'danger' | 'success' | 'ghost' | 'outline' | 'buy' | 'sell';
		size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
		disabled?: boolean;
		loading?: boolean;
		fullWidth?: boolean;
		iconOnly?: boolean;
		rounded?: boolean;
		href?: string;
		onclick?: (e: MouseEvent) => void;
		children: Snippet;
	}

	let {
		type = 'primary',
		size = 'md',
		disabled = false,
		loading = false,
		fullWidth = false,
		iconOnly = false,
		rounded = false,
		href,
		onclick,
		children
	}: Props = $props();

	const classes = $derived(() => {
		const base = 'btn';
		const typeClass = `btn-${type}`;
		const sizeClass = `btn-${size}`;
		const loadingClass = loading ? 'btn-loading' : '';
		const fullWidthClass = fullWidth ? 'btn-full' : '';
		const iconOnlyClass = iconOnly ? 'btn-icon-only' : '';
		const roundedClass = rounded ? 'btn-rounded' : '';

		return [base, typeClass, sizeClass, loadingClass, fullWidthClass, iconOnlyClass, roundedClass]
			.filter(Boolean)
			.join(' ');
	});
</script>

{#if href && !disabled}
	<a {href} class={classes()} role="button">
		{@render children()}
	</a>
{:else}
	<button
		class={classes()}
		disabled={disabled || loading}
		onclick={onclick}
		type="button"
	>
		{@render children()}
	</button>
{/if}
