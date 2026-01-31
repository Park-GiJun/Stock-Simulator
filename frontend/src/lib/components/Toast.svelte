<script lang="ts">
	import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-svelte';
	import { toastStore, toastPosition, type ToastType } from '$lib/stores/toastStore.js';

	const iconMap: Record<ToastType, typeof CheckCircle> = {
		success: CheckCircle,
		error: AlertCircle,
		warning: AlertTriangle,
		info: Info
	};

	let exitingIds = $state<Set<string>>(new Set());

	function handleDismiss(id: string) {
		exitingIds.add(id);
		exitingIds = new Set(exitingIds);

		setTimeout(() => {
			toastStore.removeToast(id);
			exitingIds.delete(id);
			exitingIds = new Set(exitingIds);
		}, 200);
	}
</script>

<div class="toast-container {$toastPosition}">
	{#each $toastStore as toast (toast.id)}
		{@const IconComponent = iconMap[toast.type]}
		<div class="toast {toast.type}" class:exiting={exitingIds.has(toast.id)} role="alert">
			<div class="toast-icon">
				<IconComponent size={20} />
			</div>
			<div class="toast-content">
				{#if toast.title}
					<div class="toast-title">{toast.title}</div>
				{/if}
				<div class="toast-message">{toast.message}</div>
			</div>
			{#if toast.dismissible}
				<button
					type="button"
					class="toast-close"
					onclick={() => handleDismiss(toast.id)}
					aria-label="닫기"
				>
					<X size={16} />
				</button>
			{/if}
		</div>
	{/each}
</div>
