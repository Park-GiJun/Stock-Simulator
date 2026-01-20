<script lang="ts">
	interface Props {
		size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
		color?: 'primary' | 'accent' | 'white';
	}

	let { size = 'md', color = 'primary' }: Props = $props();

	const classes = $derived(() => {
		return `spinner spinner-${size} spinner-${color}`;
	});
</script>

<span class={classes()} role="status" aria-label="로딩 중"></span>
