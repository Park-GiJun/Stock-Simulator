<script lang="ts">
	interface Props {
		checked?: boolean;
		label?: string;
		disabled?: boolean;
		name?: string;
		onchange?: (checked: boolean) => void;
	}

	let {
		checked = $bindable(false),
		label = '',
		disabled = false,
		name = '',
		onchange
	}: Props = $props();

	function handleClick() {
		if (disabled) return;
		checked = !checked;
		onchange?.(checked);
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Enter' || e.key === ' ') {
			e.preventDefault();
			handleClick();
		}
	}
</script>

<div class="switch-wrapper" class:disabled>
	<button
		type="button"
		role="switch"
		aria-checked={checked}
		class="switch"
		class:active={checked}
		{disabled}
		{name}
		onclick={handleClick}
		onkeydown={handleKeydown}
	>
		<span class="sr-only">{label || 'Toggle'}</span>
	</button>
	{#if label}
		<span class="checkbox-label">{label}</span>
	{/if}
</div>
