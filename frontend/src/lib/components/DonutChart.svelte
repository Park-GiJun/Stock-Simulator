<script lang="ts">
	interface Segment {
		label: string;
		value: number;
		color: string;
	}

	interface Props {
		segments: Segment[];
		size?: number;
		thickness?: number;
		showLegend?: boolean;
	}

	let { segments, size = 160, thickness = 24, showLegend = true }: Props = $props();

	const total = $derived(() => segments.reduce((sum, s) => sum + s.value, 0));

	const conicGradient = $derived(() => {
		if (total() === 0) return 'conic-gradient(var(--glass-border) 0deg 360deg)';
		let accumulated = 0;
		const stops: string[] = [];

		segments.forEach((seg) => {
			const startDeg = (accumulated / total()) * 360;
			accumulated += seg.value;
			const endDeg = (accumulated / total()) * 360;
			stops.push(`${seg.color} ${startDeg}deg ${endDeg}deg`);
		});

		return `conic-gradient(${stops.join(', ')})`;
	});

	const formatPercent = (value: number) => {
		if (total() === 0) return '0%';
		return ((value / total()) * 100).toFixed(1) + '%';
	};
</script>

<div class="donut-chart-container">
	<div
		class="donut-chart"
		style="width: {size}px; height: {size}px; background: {conicGradient()};"
	>
		<div
			class="donut-chart-inner"
			style="width: {size - thickness * 2}px; height: {size - thickness * 2}px;"
		>
			<span class="donut-chart-total">{segments.length}</span>
			<span class="donut-chart-label">종목</span>
		</div>
	</div>

	{#if showLegend}
		<div class="donut-chart-legend">
			{#each segments.slice(0, 5) as seg}
				<div class="donut-legend-item">
					<span class="donut-legend-dot" style="background: {seg.color}"></span>
					<span class="donut-legend-label">{seg.label}</span>
					<span class="donut-legend-value">{formatPercent(seg.value)}</span>
				</div>
			{/each}
			{#if segments.length > 5}
				<div class="donut-legend-item">
					<span class="donut-legend-dot" style="background: var(--color-text-disabled)"></span>
					<span class="donut-legend-label">기타 {segments.length - 5}종목</span>
				</div>
			{/if}
		</div>
	{/if}
</div>
