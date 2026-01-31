<script lang="ts">
	interface Props {
		value?: number;
		max?: number;
		size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
		color?: 'primary' | 'success' | 'warning' | 'error' | 'info' | 'stock-up' | 'stock-down' | 'gradient';
		striped?: boolean;
		animated?: boolean;
		indeterminate?: boolean;
		showLabel?: boolean;
		label?: string;
		circular?: boolean;
		circularSize?: number;
		strokeWidth?: number;
	}

	let {
		value = 0,
		max = 100,
		size = 'md',
		color = 'primary',
		striped = false,
		animated = false,
		indeterminate = false,
		showLabel = false,
		label = '',
		circular = false,
		circularSize = 80,
		strokeWidth = 8
	}: Props = $props();

	const percent = $derived(Math.min(100, Math.max(0, (value / max) * 100)));

	// Circular progress calculations
	const radius = $derived((circularSize - strokeWidth) / 2);
	const circumference = $derived(2 * Math.PI * radius);
	const offset = $derived(circumference - (percent / 100) * circumference);
</script>

{#if circular}
	<div class="progress-circular" style="width: {circularSize}px; height: {circularSize}px;">
		<svg class="progress-circular-svg" width={circularSize} height={circularSize}>
			<circle
				class="progress-circular-track"
				cx={circularSize / 2}
				cy={circularSize / 2}
				r={radius}
				stroke-width={strokeWidth}
			/>
			<circle
				class="progress-circular-bar"
				cx={circularSize / 2}
				cy={circularSize / 2}
				r={radius}
				stroke-width={strokeWidth}
				stroke-dasharray={circumference}
				stroke-dashoffset={offset}
				style="stroke: var(--color-{color === 'gradient' ? 'primary' : color})"
			/>
		</svg>
		{#if showLabel}
			<span class="progress-circular-label">{Math.round(percent)}%</span>
		{/if}
	</div>
{:else}
	<div class="progress-labeled">
		{#if showLabel || label}
			<div class="progress-label">
				<span class="progress-label-text">{label}</span>
				<span class="progress-label-value">{Math.round(percent)}%</span>
			</div>
		{/if}
		<div
			class="progress progress-{size} progress-{color}"
			class:progress-striped={striped}
			class:progress-animated={animated}
			class:progress-indeterminate={indeterminate}
			role="progressbar"
			aria-valuenow={value}
			aria-valuemin={0}
			aria-valuemax={max}
		>
			<div
				class="progress-bar"
				style="width: {indeterminate ? 30 : percent}%"
			></div>
		</div>
	</div>
{/if}
