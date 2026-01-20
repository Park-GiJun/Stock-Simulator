<script lang="ts">
	import { ChevronLeft, Search, Bell, User } from 'lucide-svelte';
	import { goto } from '$app/navigation';
	import { gameTimeStore, formatGameTime, isMarketOpen } from '$lib/stores/gameTimeStore.js';
	import { isAuthenticated, currentUser } from '$lib/stores/authStore.js';

	interface Props {
		title?: string;
		showBack?: boolean;
		showSearch?: boolean;
		showUser?: boolean;
		showGameTime?: boolean;
	}

	let {
		title = '',
		showBack = false,
		showSearch = true,
		showUser = true,
		showGameTime = true
	}: Props = $props();

	function goBack() {
		history.back();
	}
</script>

<header class="mobile-header">
	<div class="mobile-header-left">
		{#if showBack}
			<button class="mobile-header-back" onclick={goBack} type="button" aria-label="뒤로가기">
				<ChevronLeft size={24} />
			</button>
		{/if}

		{#if title}
			<h1 class="mobile-header-title">{title}</h1>
		{:else}
			<div class="mobile-header-logo">
				<div class="mobile-header-logo-icon">
					<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<polyline points="22,7 13.5,15.5 8.5,10.5 2,17"></polyline>
						<polyline points="16,7 22,7 22,13"></polyline>
					</svg>
				</div>
				<span class="mobile-header-logo-text">StockSim</span>
			</div>
		{/if}
	</div>

	<div class="mobile-header-right">
		{#if showSearch}
			<button class="mobile-header-btn" type="button" aria-label="검색">
				<Search size={20} />
			</button>
		{/if}
		<button class="mobile-header-btn" type="button" aria-label="알림">
			<Bell size={20} />
		</button>
		{#if showUser && $isAuthenticated && $currentUser}
			<button class="mobile-header-btn" type="button" aria-label="프로필">
				<User size={20} />
			</button>
		{/if}
	</div>
</header>

{#if showGameTime}
	<div class="mobile-game-time-bar">
		<div class="mobile-game-time">
			<span class="mobile-game-time-icon">🕐</span>
			<span class="mobile-game-time-value">{formatGameTime($gameTimeStore.gameTime)}</span>
		</div>
		<div class="mobile-market-status" class:open={$isMarketOpen} class:closed={!$isMarketOpen}>
			<span class="mobile-market-status-dot"></span>
			{$isMarketOpen ? '장 운영중' : '장 마감'}
		</div>
	</div>
{/if}
