<script lang="ts">
	interface Props {
		name: string;
		value: string | number;
		group?: string | number;
		label?: string;
		disabled?: boolean;
		id?: string;
		onchange?: (value: string | number) => void;
	}

	let {
		name,
		value,
		group = $bindable(''),
		label = '',
		disabled = false,
		id = '',
		onchange
	}: Props = $props();

	const radioId = $derived(id || `radio-${name}-${value}`);

	function handleChange(e: Event) {
		const target = e.target as HTMLInputElement;
		if (target.checked) {
			group = value;
			onchange?.(value);
		}
	}
</script>

<label class="radio-wrapper" class:disabled>
	<input
		type="radio"
		class="radio"
		{name}
		id={radioId}
		{value}
		checked={group === value}
		{disabled}
		onchange={handleChange}
	/>
	{#if label}
		<span class="radio-label">{label}</span>
	{/if}
</label>
