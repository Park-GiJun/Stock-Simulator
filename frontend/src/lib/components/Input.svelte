<script lang="ts">
	interface Props {
		type?: 'text' | 'password' | 'email' | 'number' | 'search';
		name?: string;
		placeholder?: string;
		value?: string | number;
		disabled?: boolean;
		readonly?: boolean;
		required?: boolean;
		error?: string;
		label?: string;
		helper?: string;
		size?: 'sm' | 'md' | 'lg';
		onchange?: (value: string) => void;
		oninput?: (value: string) => void;
	}

	let {
		type = 'text',
		name = '',
		placeholder = '',
		value = $bindable(''),
		disabled = false,
		readonly = false,
		required = false,
		error = '',
		label = '',
		helper = '',
		size = 'md',
		onchange,
		oninput
	}: Props = $props();

	function handleInput(e: Event) {
		const target = e.target as HTMLInputElement;
		value = target.value;
		oninput?.(target.value);
	}

	function handleChange(e: Event) {
		const target = e.target as HTMLInputElement;
		onchange?.(target.value);
	}

	const inputClass = $derived(() => {
		const base = 'input';
		const sizeClass = size !== 'md' ? `input-${size}` : '';
		const errorClass = error ? 'input-error' : '';
		return [base, sizeClass, errorClass].filter(Boolean).join(' ');
	});
</script>

<div class="input-wrapper">
	{#if label}
		<label class="input-label" class:required for={name}>
			{label}
		</label>
	{/if}

	<input
		{type}
		{name}
		id={name}
		class={inputClass()}
		{placeholder}
		{value}
		{disabled}
		{readonly}
		{required}
		oninput={handleInput}
		onchange={handleChange}
	/>

	{#if error}
		<span class="input-error-text">{error}</span>
	{:else if helper}
		<span class="input-helper">{helper}</span>
	{/if}
</div>
