<script lang="ts">
	import { Search, Bell, Sun, Moon, Menu, Clock, LogOut } from 'lucide-svelte';
	import { goto } from '$app/navigation';
	import { themeStore, isDarkMode } from '$lib/stores/themeStore.js';
	import { authStore, currentUser, isAuthenticated } from '$lib/stores/authStore.js';
	import { gameTimeStore, formatGameTime, isMarketOpen } from '$lib/stores/gameTimeStore.js';
	import { logout } from '$lib/api/userApi.js';
	import { toastStore } from '$lib/stores/toastStore.js';
	import Button from '$lib/components/Button.svelte';

	interface Props {
		onMenuClick?: () => void;
		showLoginModal?: () => void;
	}

	let { onMenuClick, showLoginModal }: Props = $props();

	function toggleTheme() {
		themeStore.toggle();
	}

	async function handleLogout() {
		try {
			await logout();
			authStore.logout();
			toastStore.success('로그아웃되었습니다');
			goto('/login');
		} catch (error) {
			console.error('Logout error:', error);
			// 실패해도 로컬 상태는 정리
			authStore.logout();
			goto('/login');
		}
	}

	function handleLogin() {
		goto('/login');
	}
</script>

<header class="header">
	<div class="header-left">
		<button class="header-menu-btn" onclick={onMenuClick} type="button" aria-label="메뉴">
			<Menu size={24} />
		</button>
	</div>

	<div class="header-center">
		<div class="header-search">
			<span class="header-search-icon">
				<Search size={18} />
			</span>
			<input
				type="search"
				class="header-search-input"
				placeholder="종목 검색..."
			/>
		</div>
	</div>

	<div class="header-right">
		<!-- Game Time -->
		<div class="header-game-time">
			<Clock size={16} class="header-game-time-icon" />
			<span class="header-game-time-value">{formatGameTime($gameTimeStore.gameTime)}</span>
			<span class="header-market-badge" class:open={$isMarketOpen} class:closed={!$isMarketOpen}>
				{$isMarketOpen ? '장 운영중' : '장 마감'}
			</span>
		</div>

		<!-- Theme Toggle -->
		<button class="header-theme-toggle" onclick={toggleTheme} type="button" aria-label="테마 변경">
			{#if $isDarkMode}
				<Sun size={20} />
			{:else}
				<Moon size={20} />
			{/if}
		</button>

		<!-- Notifications -->
		<button class="header-icon-btn" type="button" aria-label="알림">
			<Bell size={20} />
			<span class="header-icon-badge">3</span>
		</button>

		<!-- User or Login -->
		{#if $isAuthenticated && $currentUser}
			<div class="header-user-group">
				<div class="header-user">
					<div class="header-user-avatar">
						{$currentUser.username.charAt(0)}
					</div>
					<div class="header-user-info">
						<span class="header-user-name">{$currentUser.username}</span>
						<span class="header-user-role">{$currentUser.role || 'USER'}</span>
					</div>
				</div>
				<button 
					class="header-logout-btn" 
					onclick={handleLogout} 
					type="button" 
					aria-label="로그아웃"
					title="로그아웃"
				>
					<LogOut size={18} />
				</button>
			</div>
		{:else}
			<Button type="primary" size="sm" onclick={handleLogin}>
				로그인
			</Button>
		{/if}
	</div>
</header>

<style>
	.header-user-group {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.header-logout-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0.5rem;
		background: transparent;
		border: 1px solid var(--color-border);
		border-radius: 6px;
		color: var(--color-text-secondary);
		cursor: pointer;
		transition: all 0.2s;
	}

	.header-logout-btn:hover {
		background: var(--color-danger);
		border-color: var(--color-danger);
		color: white;
	}

	.header-user-role {
		font-size: 0.7rem;
		color: var(--color-text-tertiary);
		text-transform: uppercase;
	}
</style>
