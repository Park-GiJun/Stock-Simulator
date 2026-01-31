<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		content: string;
		position?: 'top' | 'bottom' | 'left' | 'right';
		delay?: number;
		dark?: boolean;
		children: Snippet;
	}

	let {
		content,
		position = 'top',
		delay = 200,
		dark = false,
		children
	}: Props = $props();

	let visible = $state(false);
	let timeoutId: ReturnType<typeof setTimeout>;

	function handleMouseEnter() {
		timeoutId = setTimeout(() => {
			visible = true;
		}, delay);
	}

	function handleMouseLeave() {
		clearTimeout(timeoutId);
		visible = false;
	}

	function handleFocus() {
		visible = true;
	}

	function handleBlur() {
		visible = false;
	}
</script>

<div
	class="tooltip-wrapper"
	onmouseenter={handleMouseEnter}
	onmouseleave={handleMouseLeave}
	onfocusin={handleFocus}
	onfocusout={handleBlur}
>
	{@render children()}
	<div
		class="tooltip {position}"
		class:visible
		class:dark
		role="tooltip"
		aria-hidden={!visible}
	>
		{content}
	</div>
</div>
