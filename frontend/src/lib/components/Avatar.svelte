<script lang="ts">
	import { User } from 'lucide-svelte';

	interface Props {
		src?: string | null;
		alt?: string;
		name?: string;
		size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
		square?: boolean;
		bordered?: boolean;
		status?: 'online' | 'offline' | 'busy' | 'away';
		color?: 'default' | 'primary' | 'accent' | 'success' | 'warning' | 'error';
		loading?: boolean;
	}

	let {
		src = null,
		alt = '',
		name = '',
		size = 'md',
		square = false,
		bordered = false,
		status,
		color = 'default',
		loading = false
	}: Props = $props();

	let imageError = $state(false);

	function getInitials(name: string): string {
		if (!name) return '';
		const parts = name.trim().split(/\s+/);
		if (parts.length === 1) {
			return parts[0].substring(0, 2);
		}
		return parts[0][0] + parts[parts.length - 1][0];
	}

	function handleImageError() {
		imageError = true;
	}

	const showImage = $derived(src && !imageError && !loading);
	const showInitials = $derived(!showImage && name && !loading);
	const showIcon = $derived(!showImage && !showInitials && !loading);

	const iconSizes: Record<string, number> = {
		xs: 12,
		sm: 14,
		md: 18,
		lg: 22,
		xl: 28,
		'2xl': 40
	};
</script>

<div
	class="avatar avatar-{size}"
	class:avatar-square={square}
	class:avatar-bordered={bordered}
	class:avatar-loading={loading}
	class:avatar-primary={color === 'primary'}
	class:avatar-accent={color === 'accent'}
	class:avatar-success={color === 'success'}
	class:avatar-warning={color === 'warning'}
	class:avatar-error={color === 'error'}
	role="img"
	aria-label={alt || name || '사용자 아바타'}
>
	{#if showImage}
		<img
			class="avatar-image"
			src={src}
			alt={alt || name}
			onerror={handleImageError}
		/>
	{:else if showInitials}
		<span class="avatar-initials">{getInitials(name)}</span>
	{:else if showIcon}
		<span class="avatar-icon">
			<User size={iconSizes[size]} />
		</span>
	{/if}

	{#if status}
		<span class="avatar-status {status}"></span>
	{/if}
</div>
