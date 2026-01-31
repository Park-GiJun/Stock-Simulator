<script lang="ts">
	interface Option {
		value: string | number;
		label: string;
		disabled?: boolean;
	}

	interface Props {
		options: Option[];
		value?: string | number;
		placeholder?: string;
		disabled?: boolean;
		required?: boolean;
		error?: string;
		label?: string;
		helper?: string;
		name?: string;
		size?: 'sm' | 'md' | 'lg';
		onchange?: (value: string) => void;
	}

	let {
		options,
		value = $bindable(''),
		placeholder = '선택하세요',
		disabled = false,
		required = false,
		error = '',
		label = '',
		helper = '',
		name = '',
		size = 'md',
		onchange
	}: Props = $props();

	function handleChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		value = target.value;
		onchange?.(target.value);
	}

	const selectClass = $derived(() => {
		const base = 'select';
		const sizeClass = size !== 'md' ? `select-${size}` : '';
		const errorClass = error ? 'select-error' : '';
		return [base, sizeClass, errorClass].filter(Boolean).join(' ');
	});
</script>

<div class="input-wrapper">
	{#if label}
		<label class="input-label" class:required for={name}>
			{label}
		</label>
	{/if}

	<div class="select-wrapper">
		<select
			{name}
			id={name}
			class={selectClass()}
			{disabled}
			{required}
			onchange={handleChange}
		>
			{#if placeholder}
				<option value="" disabled selected={!value}>{placeholder}</option>
			{/if}
			{#each options as option}
				<option
					value={option.value}
					disabled={option.disabled}
					selected={value === option.value}
				>
					{option.label}
				</option>
			{/each}
		</select>
	</div>

	{#if error}
		<span class="input-error-text">{error}</span>
	{:else if helper}
		<span class="input-helper">{helper}</span>
	{/if}
</div>
