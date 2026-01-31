<script lang="ts">
	interface Props {
		name?: string;
		placeholder?: string;
		value?: string;
		disabled?: boolean;
		readonly?: boolean;
		required?: boolean;
		error?: string;
		label?: string;
		helper?: string;
		rows?: number;
		maxlength?: number;
		onchange?: (value: string) => void;
		oninput?: (value: string) => void;
	}

	let {
		name = '',
		placeholder = '',
		value = $bindable(''),
		disabled = false,
		readonly = false,
		required = false,
		error = '',
		label = '',
		helper = '',
		rows = 4,
		maxlength,
		onchange,
		oninput
	}: Props = $props();

	function handleInput(e: Event) {
		const target = e.target as HTMLTextAreaElement;
		value = target.value;
		oninput?.(target.value);
	}

	function handleChange(e: Event) {
		const target = e.target as HTMLTextAreaElement;
		onchange?.(target.value);
	}

	const textareaClass = $derived(() => {
		const base = 'textarea';
		const errorClass = error ? 'input-error' : '';
		return [base, errorClass].filter(Boolean).join(' ');
	});
</script>

<div class="input-wrapper">
	{#if label}
		<label class="input-label" class:required for={name}>
			{label}
		</label>
	{/if}

	<textarea
		{name}
		id={name}
		class={textareaClass()}
		{placeholder}
		{disabled}
		{readonly}
		{required}
		{rows}
		{maxlength}
		oninput={handleInput}
		onchange={handleChange}
	>{value}</textarea>

	{#if error}
		<span class="input-error-text">{error}</span>
	{:else if helper}
		<span class="input-helper">{helper}</span>
	{/if}
</div>
