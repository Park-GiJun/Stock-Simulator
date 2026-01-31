<script lang="ts">
	interface Props {
		checked?: boolean;
		label?: string;
		disabled?: boolean;
		name?: string;
		id?: string;
		onchange?: (checked: boolean) => void;
	}

	let {
		checked = $bindable(false),
		label = '',
		disabled = false,
		name = '',
		id = '',
		onchange
	}: Props = $props();

	const checkboxId = $derived(id || name || `checkbox-${Math.random().toString(36).slice(2, 9)}`);

	function handleChange(e: Event) {
		const target = e.target as HTMLInputElement;
		checked = target.checked;
		onchange?.(target.checked);
	}
</script>

<label class="checkbox-wrapper" class:disabled>
	<input
		type="checkbox"
		class="checkbox"
		{name}
		id={checkboxId}
		{checked}
		{disabled}
		onchange={handleChange}
	/>
	{#if label}
		<span class="checkbox-label">{label}</span>
	{/if}
</label>
