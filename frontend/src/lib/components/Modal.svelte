<script lang="ts">
	import type { Snippet } from 'svelte';
	import { X } from 'lucide-svelte';

	interface Props {
		isOpen: boolean;
		title?: string;
		size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
		closable?: boolean;
		onclose?: () => void;
		children: Snippet;
		footer?: Snippet;
	}

	let {
		isOpen = $bindable(false),
		title = '',
		size = 'md',
		closable = true,
		onclose,
		children,
		footer
	}: Props = $props();

	function handleClose() {
		isOpen = false;
		onclose?.();
	}

	function handleBackdropClick(e: MouseEvent) {
		if (e.target === e.currentTarget && closable) {
			handleClose();
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Escape' && closable) {
			handleClose();
		}
	}
</script>

<svelte:window onkeydown={handleKeydown} />

{#if isOpen}
	<div
		class="modal-backdrop"
		onclick={handleBackdropClick}
		onkeydown={handleKeydown}
		role="dialog"
		aria-modal="true"
		tabindex="-1"
	>
		<div class="modal modal-{size}">
			{#if title || closable}
				<div class="modal-header">
					{#if title}
						<h2 class="modal-title">{title}</h2>
					{:else}
						<div></div>
					{/if}
					{#if closable}
						<button class="modal-close" onclick={handleClose} type="button" aria-label="닫기">
							<X size={20} />
						</button>
					{/if}
				</div>
			{/if}

			<div class="modal-body">
				{@render children()}
			</div>

			{#if footer}
				<div class="modal-footer">
					{@render footer()}
				</div>
			{/if}
		</div>
	</div>
{/if}
