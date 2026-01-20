<script lang="ts">
	import MobileNav from '$lib/components/mobile/MobileNav.svelte';
	import MobileHeader from '$lib/components/mobile/MobileHeader.svelte';

	let { children } = $props();
</script>

<div class="mobile-layout">
	<MobileHeader />
	<main class="mobile-content p-md">
		{@render children()}
	</main>
	<MobileNav />
</div>
