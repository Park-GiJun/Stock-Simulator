<script lang="ts">
	import type { Candle } from '$lib/types/stock.js';

	interface Props {
		data: Candle[];
		width?: number;
		height?: number;
		compact?: boolean;
		showGrid?: boolean;
		showTooltip?: boolean;
	}

	let {
		data,
		width = 400,
		height = 250,
		compact = false,
		showGrid = true,
		showTooltip = true
	}: Props = $props();

	// Chart padding - derived to be reactive to compact prop
	const padding = $derived(compact ? { top: 5, right: 5, bottom: 5, left: 5 } : { top: 20, right: 20, bottom: 30, left: 50 });

	// Calculate chart dimensions
	const chartWidth = $derived(width - padding.left - padding.right);
	const chartHeight = $derived((compact ? 120 : height) - padding.top - padding.bottom);

	// Calculate min/max values
	const priceRange = $derived(() => {
		if (!data || data.length === 0) return { min: 0, max: 100 };
		const prices = data.flatMap(d => [d.high, d.low]);
		const min = Math.min(...prices);
		const max = Math.max(...prices);
		const buffer = (max - min) * 0.1;
		return { min: min - buffer, max: max + buffer };
	});

	// Calculate if price went up or down
	const priceDirection = $derived(() => {
		if (!data || data.length < 2) return 'neutral';
		const first = data[0].close;
		const last = data[data.length - 1].close;
		return last >= first ? 'up' : 'down';
	});

	// Scale functions
	const xScale = $derived((index: number) => {
		const step = chartWidth / Math.max(data.length - 1, 1);
		return padding.left + index * step;
	});

	const yScale = $derived((price: number) => {
		const { min, max } = priceRange();
		const range = max - min || 1;
		return padding.top + chartHeight - ((price - min) / range) * chartHeight;
	});

	// Generate line path
	const linePath = $derived(() => {
		if (!data || data.length === 0) return '';
		return data
			.map((d, i) => {
				const x = xScale(i);
				const y = yScale(d.close);
				return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
			})
			.join(' ');
	});

	// Generate area path (for gradient fill)
	const areaPath = $derived(() => {
		if (!data || data.length === 0) return '';
		const baseline = padding.top + chartHeight;
		const linePoints = data
			.map((d, i) => {
				const x = xScale(i);
				const y = yScale(d.close);
				return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
			})
			.join(' ');
		
		const lastX = xScale(data.length - 1);
		const firstX = xScale(0);
		
		return `${linePoints} L ${lastX} ${baseline} L ${firstX} ${baseline} Z`;
	});

	// Grid lines
	const gridLines = $derived(() => {
		if (!showGrid || compact) return [];
		const { min, max } = priceRange();
		const step = (max - min) / 4;
		return [0, 1, 2, 3, 4].map(i => ({
			y: yScale(min + step * i),
			label: Math.round(min + step * i).toLocaleString()
		}));
	});

	// Tooltip state
	let tooltipVisible = $state(false);
	let tooltipX = $state(0);
	let tooltipY = $state(0);
	let tooltipData = $state<Candle | null>(null);

	function handleMouseMove(e: MouseEvent) {
		if (!showTooltip || compact || !data.length) return;

		const svg = e.currentTarget as SVGSVGElement;
		const rect = svg.getBoundingClientRect();
		const mouseX = e.clientX - rect.left;

		// Find nearest data point
		const step = chartWidth / Math.max(data.length - 1, 1);
		const index = Math.round((mouseX - padding.left) / step);
		const clampedIndex = Math.max(0, Math.min(data.length - 1, index));

		tooltipData = data[clampedIndex];
		tooltipX = xScale(clampedIndex);
		tooltipY = yScale(tooltipData.close);
		tooltipVisible = true;
	}

	function handleMouseLeave() {
		tooltipVisible = false;
	}
</script>

<div class="stock-chart-wrapper" class:compact>
	<svg
		class="chart-svg"
		viewBox="0 0 {width} {compact ? 120 : height}"
		preserveAspectRatio="none"
		role="img"
		aria-label="Stock price chart"
		onmousemove={handleMouseMove}
		onmouseleave={handleMouseLeave}
	>
		<!-- Grid lines -->
		{#if showGrid && !compact}
			{#each gridLines() as line}
				<line
					class="chart-grid-line"
					x1={padding.left}
					x2={width - padding.right}
					y1={line.y}
					y2={line.y}
				/>
				<text
					class="chart-axis-label"
					x={padding.left - 8}
					y={line.y + 4}
					text-anchor="end"
				>
					{line.label}
				</text>
			{/each}
		{/if}

		<!-- Area fill -->
		<path
			class="chart-area {priceDirection()}"
			d={areaPath()}
		/>

		<!-- Price line -->
		<path
			class="chart-line {priceDirection()}"
			d={linePath()}
		/>

		<!-- Tooltip indicator -->
		{#if tooltipVisible && tooltipData}
			<circle
				cx={tooltipX}
				cy={tooltipY}
				r="4"
				fill="var(--color-bg-primary)"
				stroke={priceDirection() === 'up' ? 'var(--color-stock-up)' : 'var(--color-stock-down)'}
				stroke-width="2"
			/>
		{/if}
	</svg>

	<!-- Tooltip -->
	{#if tooltipVisible && tooltipData && showTooltip && !compact}
		<div
			class="chart-tooltip"
			style="left: {tooltipX + 10}px; top: {tooltipY - 10}px;"
		>
			<div class="chart-tooltip-price">₩{tooltipData.close.toLocaleString()}</div>
			<div class="chart-tooltip-time">{new Date(tooltipData.timestamp).toLocaleDateString()}</div>
		</div>
	{/if}
</div>
