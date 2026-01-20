<script lang="ts">
	import { AlertCircle } from 'lucide-svelte';
	import { errorModal, closeErrorModal } from '$lib/stores/errorStore.js';
	import Modal from './Modal.svelte';
	import Button from './Button.svelte';
</script>

<Modal
	bind:isOpen={$errorModal.isOpen}
	title=""
	size="sm"
	closable={true}
	onclose={closeErrorModal}
>
	<div class="modal-error-icon">
		<AlertCircle size={32} />
	</div>

	<h3 class="text-lg font-semibold text-center mb-sm">{$errorModal.title}</h3>
	<p class="text-secondary text-center">{$errorModal.message}</p>

	{#if $errorModal.errorCode}
		<div class="text-center mt-md">
			<span class="modal-error-code">{$errorModal.errorCode}</span>
		</div>
	{/if}

	{#snippet footer()}
		<Button type="primary" fullWidth onclick={closeErrorModal}>
			확인
		</Button>
	{/snippet}
</Modal>
