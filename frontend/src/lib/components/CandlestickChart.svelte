<script lang="ts">
	import type { Candle } from '$lib/types/stock.js';

	interface Props {
		data: Candle[];
		width?: number;
		height?: number;
		showVolume?: boolean;
		showGrid?: boolean;
		showTooltip?: boolean;
	}

	let {
		data,
		width = 800,
		height = 400,
		showVolume = true,
		showGrid = true,
		showTooltip = true
	}: Props = $props();

	// Layout
	const padding = { top: 20, right: 60, bottom: 30, left: 10 };
	const volumeHeight = $derived(showVolume ? 60 : 0);
	const chartHeight = $derived(height - padding.top - padding.bottom - volumeHeight);
	const chartWidth = $derived(width - padding.left - padding.right);

	// Calculate price range
	const priceRange = $derived(() => {
		if (!data || data.length === 0) return { min: 0, max: 100 };
		const highs = data.map(d => d.high);
		const lows = data.map(d => d.low);
		const min = Math.min(...lows);
		const max = Math.max(...highs);
		const buffer = (max - min) * 0.05;
		return { min: min - buffer, max: max + buffer };
	});

	// Calculate volume range
	const volumeMax = $derived(() => {
		if (!data || data.length === 0) return 1;
		return Math.max(...data.map(d => d.volume));
	});

	// Candle width
	const candleWidth = $derived(() => {
		if (!data || data.length === 0) return 10;
		const totalWidth = chartWidth - data.length * 2;
		return Math.max(2, Math.min(20, totalWidth / data.length));
	});

	// Scale functions
	const xScale = $derived((index: number) => {
		const step = chartWidth / data.length;
		return padding.left + index * step + step / 2;
	});

	const yScale = $derived((price: number) => {
		const { min, max } = priceRange();
		const range = max - min || 1;
		return padding.top + chartHeight - ((price - min) / range) * chartHeight;
	});

	const volumeScale = $derived((volume: number) => {
		const max = volumeMax();
		return (volume / max) * volumeHeight * 0.8;
	});

	// Grid lines
	const priceGridLines = $derived(() => {
		if (!showGrid) return [];
		const { min, max } = priceRange();
		const step = (max - min) / 5;
		return [0, 1, 2, 3, 4, 5].map(i => ({
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
		if (!showTooltip || !data.length) return;

		const svg = e.currentTarget as SVGSVGElement;
		const rect = svg.getBoundingClientRect();
		const mouseX = e.clientX - rect.left;

		const step = chartWidth / data.length;
		const index = Math.floor((mouseX - padding.left) / step);
		const clampedIndex = Math.max(0, Math.min(data.length - 1, index));

		tooltipData = data[clampedIndex];
		tooltipX = xScale(clampedIndex);
		tooltipY = e.clientY - rect.top;
		tooltipVisible = true;
	}

	function handleMouseLeave() {
		tooltipVisible = false;
	}

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatVolume(volume: number): string {
		if (volume >= 1000000) return (volume / 1000000).toFixed(1) + 'M';
		if (volume >= 1000) return (volume / 1000).toFixed(0) + 'K';
		return volume.toString();
	}
</script>

<div class="candlestick-chart-wrapper">
	<svg
		class="candlestick-chart"
		viewBox="0 0 {width} {height}"
		preserveAspectRatio="xMidYMid meet"
		role="img"
		aria-label="Candlestick chart"
		onmousemove={handleMouseMove}
		onmouseleave={handleMouseLeave}
	>
		<!-- Grid lines -->
		{#if showGrid}
			{#each priceGridLines() as line}
				<line
					class="chart-grid-line"
					x1={padding.left}
					x2={width - padding.right}
					y1={line.y}
					y2={line.y}
					stroke="var(--color-border)"
					stroke-dasharray="2,2"
					opacity="0.5"
				/>
				<text
					x={width - padding.right + 8}
					y={line.y + 4}
					fill="var(--color-text-secondary)"
					font-size="10"
				>
					{line.label}
				</text>
			{/each}
		{/if}

		<!-- Volume bars -->
		{#if showVolume}
			{#each data as candle, i (candle.timestamp)}
				{@const isUp = candle.close >= candle.open}
				<rect
					x={xScale(i) - candleWidth() / 2}
					y={height - padding.bottom - volumeScale(candle.volume)}
					width={candleWidth()}
					height={volumeScale(candle.volume)}
					fill={isUp ? 'var(--color-stock-up)' : 'var(--color-stock-down)'}
					opacity="0.3"
				/>
			{/each}
		{/if}

		<!-- Candlesticks -->
		{#each data as candle, i (candle.timestamp)}
			{@const isUp = candle.close >= candle.open}
			{@const x = xScale(i)}
			{@const bodyTop = yScale(Math.max(candle.open, candle.close))}
			{@const bodyBottom = yScale(Math.min(candle.open, candle.close))}
			{@const bodyHeight = Math.max(1, bodyBottom - bodyTop)}

			<!-- Wick (high-low line) -->
			<line
				x1={x}
				x2={x}
				y1={yScale(candle.high)}
				y2={yScale(candle.low)}
				stroke={isUp ? 'var(--color-stock-up)' : 'var(--color-stock-down)'}
				stroke-width="1"
			/>

			<!-- Body -->
			<rect
				x={x - candleWidth() / 2}
				y={bodyTop}
				width={candleWidth()}
				height={bodyHeight}
				fill={isUp ? 'var(--color-stock-up)' : 'var(--color-stock-down)'}
				stroke={isUp ? 'var(--color-stock-up)' : 'var(--color-stock-down)'}
				stroke-width="1"
				rx="1"
			/>
		{/each}

		<!-- Crosshair -->
		{#if tooltipVisible && tooltipData}
			<line
				x1={tooltipX}
				x2={tooltipX}
				y1={padding.top}
				y2={height - padding.bottom}
				stroke="var(--color-text-disabled)"
				stroke-dasharray="4,4"
			/>
			<line
				x1={padding.left}
				x2={width - padding.right}
				y1={tooltipY}
				y2={tooltipY}
				stroke="var(--color-text-disabled)"
				stroke-dasharray="4,4"
			/>
		{/if}
	</svg>

	<!-- Tooltip -->
	{#if tooltipVisible && tooltipData && showTooltip}
		<div
			class="candlestick-tooltip"
			style="left: {Math.min(tooltipX + 15, width - 150)}px; top: {Math.min(tooltipY, height - 120)}px;"
		>
			<div class="candlestick-tooltip-date">
				{new Date(tooltipData.timestamp).toLocaleDateString('ko-KR')}
			</div>
			<div class="candlestick-tooltip-row">
				<span class="label">시가</span>
				<span class="value">₩{formatPrice(tooltipData.open)}</span>
			</div>
			<div class="candlestick-tooltip-row">
				<span class="label">고가</span>
				<span class="value up">₩{formatPrice(tooltipData.high)}</span>
			</div>
			<div class="candlestick-tooltip-row">
				<span class="label">저가</span>
				<span class="value down">₩{formatPrice(tooltipData.low)}</span>
			</div>
			<div class="candlestick-tooltip-row">
				<span class="label">종가</span>
				<span class="value" class:up={tooltipData.close >= tooltipData.open} class:down={tooltipData.close < tooltipData.open}>
					₩{formatPrice(tooltipData.close)}
				</span>
			</div>
			<div class="candlestick-tooltip-row">
				<span class="label">거래량</span>
				<span class="value">{formatVolume(tooltipData.volume)}</span>
			</div>
		</div>
	{/if}
</div>

<style>
	.candlestick-chart-wrapper {
		position: relative;
		width: 100%;
	}

	.candlestick-chart {
		width: 100%;
		height: auto;
		display: block;
	}

	.candlestick-tooltip {
		position: absolute;
		background: var(--color-bg-primary);
		border: 1px solid var(--color-border);
		border-radius: var(--radius-md);
		padding: var(--spacing-sm);
		box-shadow: var(--shadow-lg);
		pointer-events: none;
		z-index: 10;
		min-width: 130px;
	}

	.candlestick-tooltip-date {
		font-size: var(--font-size-xs);
		color: var(--color-text-secondary);
		margin-bottom: var(--spacing-xs);
		padding-bottom: var(--spacing-xs);
		border-bottom: 1px solid var(--color-border);
	}

	.candlestick-tooltip-row {
		display: flex;
		justify-content: space-between;
		font-size: var(--font-size-xs);
		padding: 2px 0;
	}

	.candlestick-tooltip-row .label {
		color: var(--color-text-secondary);
	}

	.candlestick-tooltip-row .value {
		font-family: var(--font-mono);
		font-weight: 500;
		color: var(--color-text-primary);
	}

	.candlestick-tooltip-row .value.up {
		color: var(--color-stock-up);
	}

	.candlestick-tooltip-row .value.down {
		color: var(--color-stock-down);
	}
</style>
