<script lang="ts">
	import '../app.css';
	import { onMount } from 'svelte';
	import { themeStore } from '$lib/stores/themeStore.js';
	import { gameTimeStore } from '$lib/stores/gameTimeStore.js';
	import { authStore } from '$lib/stores/authStore.js';
	import { ErrorModal } from '$lib/components';
	import { MOCK_USER } from '$lib/mock/user.js';

	let { children } = $props();

	onMount(() => {
		// Initialize theme
		themeStore.initialize();

		// Initialize auth (mock user for demo)
		authStore.login(MOCK_USER, 'mock-token');

		// Start game time
		gameTimeStore.start();

		return () => {
			gameTimeStore.stop();
		};
	});
</script>

<svelte:head>
	<title>StockSim - 모의 주식 게임</title>
	<meta name="description" content="AI 기반 이벤트와 수급 시스템을 갖춘 모의 주식 시뮬레이션 게임" />
</svelte:head>

<!-- Global Error Modal -->
<ErrorModal />

{@render children()}
