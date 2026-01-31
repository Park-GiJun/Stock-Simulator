<script lang="ts">
	import { Sun, Moon, Monitor } from 'lucide-svelte';
	import { theme, toggleTheme, setTheme } from '$lib/stores/themeStore.js';

	interface Props {
		showLabel?: boolean;
		showSystem?: boolean;
	}

	let {
		showLabel = false,
		showSystem = false
	}: Props = $props();

	const themes = [
		{ id: 'light', label: '라이트', icon: Sun },
		{ id: 'dark', label: '다크', icon: Moon },
		{ id: 'system', label: '시스템', icon: Monitor }
	] as const;
</script>

{#if showSystem}
	<div class="theme-toggle-group">
		{#each themes as t (t.id)}
			{@const Icon = t.icon}
			<button
				type="button"
				class="theme-toggle-btn"
				class:active={$theme === t.id}
				onclick={() => setTheme(t.id)}
				aria-label="{t.label} 테마"
			>
				<Icon size={16} />
				{#if showLabel}
					<span>{t.label}</span>
				{/if}
			</button>
		{/each}
	</div>
{:else}
	<button
		type="button"
		class="theme-toggle"
		onclick={toggleTheme}
		aria-label="테마 전환"
	>
		{#if $theme === 'dark'}
			<Sun size={20} />
		{:else}
			<Moon size={20} />
		{/if}
		{#if showLabel}
			<span>{$theme === 'dark' ? '라이트 모드' : '다크 모드'}</span>
		{/if}
	</button>
{/if}

<style>
	.theme-toggle {
		display: flex;
		align-items: center;
		gap: var(--spacing-sm);
		padding: var(--spacing-sm);
		border: none;
		border-radius: var(--radius-md);
		background: var(--color-bg-tertiary);
		color: var(--color-text-secondary);
		cursor: pointer;
		transition: all var(--transition-fast);
	}

	.theme-toggle:hover {
		background: var(--color-bg-hover);
		color: var(--color-text-primary);
	}

	.theme-toggle-group {
		display: flex;
		gap: 2px;
		padding: 2px;
		background: var(--color-bg-tertiary);
		border-radius: var(--radius-md);
	}

	.theme-toggle-btn {
		display: flex;
		align-items: center;
		gap: var(--spacing-xs);
		padding: var(--spacing-xs) var(--spacing-sm);
		border: none;
		border-radius: var(--radius-sm);
		background: transparent;
		color: var(--color-text-secondary);
		font-size: var(--font-size-sm);
		cursor: pointer;
		transition: all var(--transition-fast);
	}

	.theme-toggle-btn:hover {
		color: var(--color-text-primary);
	}

	.theme-toggle-btn.active {
		background: var(--color-bg-primary);
		color: var(--color-text-primary);
		box-shadow: var(--shadow-sm);
	}
</style>
