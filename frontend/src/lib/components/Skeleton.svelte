<script lang="ts">
	interface Props {
		variant?: 'text' | 'title' | 'avatar' | 'button' | 'image' | 'chart' | 'custom';
		width?: string;
		height?: string;
		size?: 'sm' | 'md' | 'lg' | 'xl';
		length?: 'short' | 'medium' | 'long' | 'full';
		animated?: boolean;
		count?: number;
		class?: string;
	}

	let {
		variant = 'text',
		width,
		height,
		size = 'md',
		length = 'full',
		animated = true,
		count = 1,
		class: className = ''
	}: Props = $props();

	const variantClasses: Record<string, string> = {
		text: 'skeleton-text',
		title: 'skeleton-title',
		avatar: 'skeleton-avatar',
		button: 'skeleton-button',
		image: 'skeleton-image',
		chart: 'skeleton-chart',
		custom: ''
	};

	function getClasses(): string {
		const classes = ['skeleton'];

		if (!animated) {
			classes.push('skeleton-pulse');
		}

		if (variant !== 'custom') {
			classes.push(variantClasses[variant]);
		}

		if (variant === 'avatar') {
			classes.push(size);
		}

		if (variant === 'text') {
			classes.push(length);
		}

		if (variant === 'button' && length === 'full') {
			classes.push('full');
		}

		if (className) {
			classes.push(className);
		}

		return classes.join(' ');
	}

	function getStyle(): string {
		const styles: string[] = [];

		if (width) {
			styles.push(`width: ${width}`);
		}

		if (height) {
			styles.push(`height: ${height}`);
		}

		return styles.join('; ');
	}
</script>

{#each Array(count) as _, i (i)}
	<div class={getClasses()} style={getStyle()} role="status" aria-label="로딩 중..."></div>
{/each}
