<script lang="ts">
	import { X } from 'lucide-svelte';
	import type { Component } from 'svelte';

	interface Props {
		variant?: 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info' | 'stock-up' | 'stock-down';
		size?: 'sm' | 'md' | 'lg';
		outline?: boolean;
		pill?: boolean;
		dot?: boolean;
		pulse?: boolean;
		count?: number;
		icon?: Component;
		removable?: boolean;
		onRemove?: () => void;
		children?: import('svelte').Snippet;
	}

	let {
		variant = 'secondary',
		size = 'md',
		outline = false,
		pill = false,
		dot = false,
		pulse = false,
		count,
		icon,
		removable = false,
		onRemove,
		children
	}: Props = $props();

	function getClasses(): string {
		const classes = ['badge', `badge-${variant}`];

		if (size !== 'md') classes.push(`badge-${size}`);
		if (outline) classes.push('badge-outline');
		if (pill) classes.push('badge-pill');
		if (dot) classes.push('badge-dot');
		if (pulse) classes.push('badge-pulse');
		if (count !== undefined) classes.push('badge-count');

		return classes.join(' ');
	}
</script>

<span class={getClasses()}>
	{#if icon && !dot}
		{@const Icon = icon}
		<span class="badge-icon">
			<Icon size={12} />
		</span>
	{/if}

	{#if count !== undefined}
		{count > 99 ? '99+' : count}
	{:else if !dot && children}
		{@render children()}
	{/if}

	{#if removable && !dot}
		<button
			type="button"
			class="badge-remove"
			onclick={onRemove}
			aria-label="제거"
		>
			<X size={12} />
		</button>
	{/if}
</span>
