<script lang="ts">
	import Sidebar from '$lib/components/desktop/Sidebar.svelte';
	import Header from '$lib/components/desktop/Header.svelte';

	let { children } = $props();
	let sidebarCollapsed = $state(false);
	let showLogin = $state(false);

	function handleMenuClick() {
		sidebarCollapsed = !sidebarCollapsed;
	}

	function openLoginModal() {
		showLogin = true;
	}
</script>

<div class="layout-with-sidebar">
	<Sidebar bind:collapsed={sidebarCollapsed} />

	<div
		class="layout-content"
		style:margin-left={sidebarCollapsed ? 'var(--sidebar-collapsed-width)' : 'var(--sidebar-width)'}
	>
		<Header onMenuClick={handleMenuClick} showLoginModal={openLoginModal} />
		<main class="page-content animate-fade-in">
			{@render children()}
		</main>
	</div>
</div>
