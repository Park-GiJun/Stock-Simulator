<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Component } from 'svelte';

	interface Tab {
		id: string;
		label: string;
		icon?: Component;
		badge?: string | number;
		disabled?: boolean;
	}

	interface Props {
		tabs: Tab[];
		activeTab?: string;
		variant?: 'line' | 'pills' | 'segment';
		size?: 'sm' | 'md' | 'lg';
		fullWidth?: boolean;
		vertical?: boolean;
		onChange?: (tabId: string) => void;
		children?: Snippet<[string]>;
	}

	let {
		tabs,
		activeTab = $bindable(tabs[0]?.id ?? ''),
		variant = 'line',
		size = 'md',
		fullWidth = false,
		vertical = false,
		onChange,
		children
	}: Props = $props();

	function handleTabClick(tabId: string) {
		if (tabs.find(t => t.id === tabId)?.disabled) return;
		activeTab = tabId;
		onChange?.(tabId);
	}

	function handleKeyDown(e: KeyboardEvent, index: number) {
		const enabledTabs = tabs.filter(t => !t.disabled);
		const currentEnabledIndex = enabledTabs.findIndex(t => t.id === tabs[index].id);

		let newIndex = -1;

		if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
			e.preventDefault();
			newIndex = (currentEnabledIndex + 1) % enabledTabs.length;
		} else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
			e.preventDefault();
			newIndex = (currentEnabledIndex - 1 + enabledTabs.length) % enabledTabs.length;
		} else if (e.key === 'Home') {
			e.preventDefault();
			newIndex = 0;
		} else if (e.key === 'End') {
			e.preventDefault();
			newIndex = enabledTabs.length - 1;
		}

		if (newIndex >= 0) {
			const targetTab = enabledTabs[newIndex];
			activeTab = targetTab.id;
			onChange?.(targetTab.id);
			// Focus the new tab button
			const tabButtons = document.querySelectorAll('.tabs-tab:not(:disabled)');
			(tabButtons[newIndex] as HTMLElement)?.focus();
		}
	}
</script>

<div
	class="tabs variant-{variant} size-{size}"
	class:full-width={fullWidth}
	class:vertical
>
	<div class="tabs-list" role="tablist" aria-orientation={vertical ? 'vertical' : 'horizontal'}>
		{#each tabs as tab, index (tab.id)}
			<button
				type="button"
				class="tabs-tab"
				class:active={activeTab === tab.id}
				role="tab"
				id="tab-{tab.id}"
				aria-selected={activeTab === tab.id}
				aria-controls="panel-{tab.id}"
				tabindex={activeTab === tab.id ? 0 : -1}
				disabled={tab.disabled}
				onclick={() => handleTabClick(tab.id)}
				onkeydown={(e) => handleKeyDown(e, index)}
			>
				{#if tab.icon}
					{@const Icon = tab.icon}
					<span class="tabs-tab-icon">
						<Icon size={16} />
					</span>
				{/if}
				<span>{tab.label}</span>
				{#if tab.badge !== undefined}
					<span class="tabs-tab-badge">{tab.badge}</span>
				{/if}
			</button>
		{/each}
	</div>

	{#each tabs as tab (tab.id)}
		<div
			class="tabs-panel"
			role="tabpanel"
			id="panel-{tab.id}"
			aria-labelledby="tab-{tab.id}"
			hidden={activeTab !== tab.id}
		>
			{#if activeTab === tab.id && children}
				{@render children(tab.id)}
			{/if}
		</div>
	{/each}
</div>
