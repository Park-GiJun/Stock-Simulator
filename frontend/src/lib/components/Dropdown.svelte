<script lang="ts">
	import { Check, ChevronRight } from 'lucide-svelte';
	import type { Snippet, Component } from 'svelte';

	interface DropdownItem {
		id: string;
		label: string;
		icon?: Component;
		disabled?: boolean;
		danger?: boolean;
		selected?: boolean;
		divider?: boolean;
		onClick?: () => void;
	}

	interface Props {
		items: DropdownItem[];
		position?: 'bottom-start' | 'bottom-end' | 'top-start' | 'top-end';
		closeOnSelect?: boolean;
		trigger: Snippet;
	}

	let {
		items,
		position = 'bottom-start',
		closeOnSelect = true,
		trigger
	}: Props = $props();

	let isOpen = $state(false);
	let triggerRef = $state<HTMLDivElement | null>(null);

	function toggle() {
		isOpen = !isOpen;
	}

	function close() {
		isOpen = false;
	}

	function handleItemClick(item: DropdownItem) {
		if (item.disabled) return;
		item.onClick?.();
		if (closeOnSelect) {
			close();
		}
	}

	function handleKeyDown(e: KeyboardEvent) {
		if (e.key === 'Escape') {
			close();
		}
	}

	function handleClickOutside(e: MouseEvent) {
		if (triggerRef && !triggerRef.contains(e.target as Node)) {
			close();
		}
	}

	$effect(() => {
		if (isOpen) {
			document.addEventListener('click', handleClickOutside);
			document.addEventListener('keydown', handleKeyDown);
		}

		return () => {
			document.removeEventListener('click', handleClickOutside);
			document.removeEventListener('keydown', handleKeyDown);
		};
	});
</script>

<div class="dropdown" bind:this={triggerRef}>
	<div
		class="dropdown-trigger"
		onclick={toggle}
		onkeydown={(e) => e.key === 'Enter' && toggle()}
		role="button"
		tabindex="0"
		aria-haspopup="menu"
		aria-expanded={isOpen}
	>
		{@render trigger()}
	</div>

	<div class="dropdown-menu {position}" class:open={isOpen} role="menu">
		{#each items as item (item.id)}
			{#if item.divider}
				<div class="dropdown-divider" role="separator"></div>
			{:else}
				<button
					type="button"
					class="dropdown-item"
					class:danger={item.danger}
					class:selected={item.selected}
					disabled={item.disabled}
					onclick={() => handleItemClick(item)}
					role="menuitem"
				>
					{#if item.icon}
						{@const Icon = item.icon}
						<span class="dropdown-item-icon">
							<Icon size={16} />
						</span>
					{:else if item.selected !== undefined}
						<span class="dropdown-item-check">
							<Check size={16} />
						</span>
					{/if}
					<span>{item.label}</span>
				</button>
			{/if}
		{/each}
	</div>
</div>
