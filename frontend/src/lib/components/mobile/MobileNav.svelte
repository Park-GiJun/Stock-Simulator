<script lang="ts">
	import { page } from '$app/stores';
	import { MOBILE_MENU } from '$lib/config/staticMenu.js';
	import {
		LayoutDashboard,
		TrendingUp,
		Newspaper,
		User,
		Trophy
	} from 'lucide-svelte';

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
			return currentPath === '/m' || currentPath === '/m/';
		}
		// Convert desktop path to mobile comparison
		const mobilePath = `/m${path}`;
		return currentPath.startsWith(mobilePath);
	}

	function getMobilePath(path: string): string {
		return path === '/' ? '/m' : `/m${path}`;
	}
</script>

<nav class="mobile-nav">
	{#each MOBILE_MENU as item}
		{@const IconComponent = getIcon(item.icon)}
		<a
			href={getMobilePath(item.path)}
			class="mobile-nav-item"
			class:active={isActive(item.path)}
		>
			<span class="mobile-nav-icon">
				<IconComponent size={24} />
			</span>
			<span class="mobile-nav-label">{item.name}</span>
		</a>
	{/each}
</nav>
