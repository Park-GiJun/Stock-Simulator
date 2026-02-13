<script lang="ts">
	interface Props {
		data: number[];
		width?: number;
		height?: number;
		color?: string;
		fillOpacity?: number;
	}

	let { data, width = 80, height = 24, color = '#8b5cf6', fillOpacity = 0.15 }: Props = $props();

	const points = $derived(() => {
		if (!data || data.length < 2) return '';
		const min = Math.min(...data);
		const max = Math.max(...data);
		const range = max - min || 1;
		const stepX = width / (data.length - 1);

		return data
			.map((val, i) => {
				const x = i * stepX;
				const y = height - ((val - min) / range) * (height - 4) - 2;
				return `${x},${y}`;
			})
			.join(' ');
	});

	const fillPath = $derived(() => {
		if (!data || data.length < 2) return '';
		const min = Math.min(...data);
		const max = Math.max(...data);
		const range = max - min || 1;
		const stepX = width / (data.length - 1);

		const linePoints = data.map((val, i) => {
			const x = i * stepX;
			const y = height - ((val - min) / range) * (height - 4) - 2;
			return `${x},${y}`;
		});

		return `M0,${height} L${linePoints.join(' L')} L${width},${height} Z`;
	});

	const trend = $derived(() => {
		if (!data || data.length < 2) return 'neutral';
		return data[data.length - 1] >= data[0] ? 'up' : 'down';
	});

	const resolvedColor = $derived(() => {
		if (color === 'auto') {
			return trend() === 'up' ? 'var(--color-stock-up)' : 'var(--color-stock-down)';
		}
		return color;
	});
</script>

<svg class="mini-sparkline" {width} {height} viewBox="0 0 {width} {height}">
	{#if data && data.length >= 2}
		<path d={fillPath()} fill={resolvedColor()} opacity={fillOpacity} />
		<polyline
			points={points()}
			fill="none"
			stroke={resolvedColor()}
			stroke-width="1.5"
			stroke-linecap="round"
			stroke-linejoin="round"
		/>
		<circle
			cx={width}
			cy={(() => {
				const min = Math.min(...data);
				const max = Math.max(...data);
				const range = max - min || 1;
				return height - ((data[data.length - 1] - min) / range) * (height - 4) - 2;
			})()}
			r="2"
			fill={resolvedColor()}
		/>
	{/if}
</svg>
