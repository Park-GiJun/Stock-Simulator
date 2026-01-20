<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { STATIC_MENU } from '$lib/config/staticMenu.js';
	import {
		LayoutDashboard,
		TrendingUp,
		Newspaper,
		User,
		Trophy,
		Clock,
		ChevronLeft,
		ChevronRight
	} from 'lucide-svelte';

	import { gameTimeStore, formatGameTime, isMarketOpen } from '$lib/stores/gameTimeStore.js';
	import { authStore, currentUser } from '$lib/stores/authStore.js';

	interface Props {
		collapsed?: boolean;
	}

	let { collapsed = $bindable(false) }: Props = $props();

	const iconMap: Record<string, typeof LayoutDashboard> = {
		LayoutDashboard,
		TrendingUp,
		Newspaper,
		User,
		Trophy
	};

	function getIcon(iconName: string) {
		return iconMap[iconName] ?? LayoutDashboard;
	}

	function isActive(path: string): boolean {
		const currentPath = $page.url.pathname;
		if (path === '/') {
			return currentPath === '/';
		}
		return currentPath.startsWith(path);
	}

	function toggleCollapse() {
		collapsed = !collapsed;
	}
</script>

<aside class="sidebar" class:collapsed>
	<!-- Logo -->
	<div class="sidebar-logo">
		<div class="sidebar-logo-icon">
			<TrendingUp size={20} color="#1F2937" />
		</div>
		<span class="sidebar-logo-text">StockSim</span>
	</div>

	<!-- Game Time -->
	<div class="sidebar-game-time">
		<span class="sidebar-game-time-label">게임 시간</span>
		<span class="sidebar-game-time-value">{formatGameTime($gameTimeStore.gameTime)}</span>
		<div class="sidebar-market-status" class:open={$isMarketOpen} class:closed={!$isMarketOpen}>
			<span class="sidebar-market-status-dot"></span>
			{$isMarketOpen ? '장 운영중' : '장 마감'}
		</div>
	</div>

	<!-- Navigation -->
	<nav class="sidebar-nav">
		{#each STATIC_MENU as item}
			{@const IconComponent = getIcon(item.icon)}
			<a
				href={item.path}
				class="sidebar-item"
				class:active={isActive(item.path)}
			>
				<span class="sidebar-item-icon">
					<IconComponent size={20} />
				</span>
				<span class="sidebar-item-text">{item.name}</span>
			</a>
		{/each}
	</nav>

	<!-- User -->
	{#if $currentUser}
		<div class="sidebar-footer">
			<div class="sidebar-user">
				<div class="sidebar-user-avatar">
					{$currentUser.username.charAt(0)}
				</div>
				<div class="sidebar-user-info">
					<span class="sidebar-user-name">{$currentUser.username}</span>
					<span class="sidebar-user-capital">
						₩{$currentUser.capital.toLocaleString()}
					</span>
				</div>
			</div>
		</div>
	{/if}

	<!-- Toggle Button -->
	<button class="sidebar-toggle" onclick={toggleCollapse} type="button" aria-label="사이드바 토글">
		{#if collapsed}
			<ChevronRight size={14} />
		{:else}
			<ChevronLeft size={14} />
		{/if}
	</button>
</aside>
