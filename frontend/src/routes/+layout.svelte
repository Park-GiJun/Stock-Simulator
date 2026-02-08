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

	onMount(async () => {
		// Initialize theme
		themeStore.initialize();

		// Initialize auth (localStorage에서 user 정보 복원)
		authStore.initialize();

		// 현재 경로
		const currentPath = $page.url.pathname;

		// Public route가 아니면 세션 확인
		if (!publicRoutes.includes(currentPath)) {
			// authStore 상태 확인
			let currentAuthState: any;
			const unsubscribe = authStore.subscribe((state) => {
				currentAuthState = state;
			});
			unsubscribe();

			// 이미 authStore에 user 정보가 있으면 세션 검증 스킵
			// (로그인 직후 또는 페이지 새로고침 시 localStorage 복원)
			if (!currentAuthState?.user) {
				try {
					// Session 유효성 확인 (Cookie 자동 전송)
					const response = await getCurrentUser();

					if (response.success && response.data) {
						// Session 유효 -> authStore 업데이트
						authStore.updateUser(response.data);
						console.log('✅ Session valid, user:', response.data);
					} else {
						// Session 무효 -> 로그인 페이지로
						console.warn('❌ Session invalid, redirecting to login');
						authStore.logout();
						goto('/login');
					}
				} catch (error) {
					// Session 만료 또는 오류 -> 로그인 페이지로
					console.error('❌ Session check failed:', error);
					authStore.logout();
					goto('/login');
				}
			} else {
				console.log('✅ User already in authStore, skipping session check');
			}
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
