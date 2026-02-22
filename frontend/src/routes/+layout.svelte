<script lang="ts">
	import '../app.css';
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { themeStore } from '$lib/stores/themeStore.js';
	import { gameTimeStore } from '$lib/stores/gameTimeStore.js';
	import { authStore } from '$lib/stores/authStore.js';
	import { getCurrentUser } from '$lib/api/userApi.js';
	import { ErrorModal } from '$lib/components';

	let { children } = $props();

	// Public routes (인증 불필요)
	const publicRoutes = ['/login', '/signup', '/clear-storage.html'];

	onMount(() => {
		// Initialize theme
		themeStore.initialize();

		// Initialize auth (localStorage에서 user 정보 복원)
		authStore.initialize();

		// 현재 경로
		const currentPath = $page.url.pathname;

		// Public route가 아니면 세션 확인
		if (!publicRoutes.includes(currentPath)) {
			// 항상 서버 세션 검증 (localStorage만 믿지 않음)
			getCurrentUser()
				.then((response) => {
					if (response.success && response.data) {
						authStore.updateUser(response.data);
					} else {
						console.warn('❌ Session invalid, redirecting to login');
						authStore.logout();
						goto('/login');
					}
				})
				.catch(() => {
					authStore.logout();
					goto('/login');
				});
		}

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
