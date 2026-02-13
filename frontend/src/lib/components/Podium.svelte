<script lang="ts">
	import { Trophy } from 'lucide-svelte';

	interface PodiumEntry {
		rank: number;
		username: string;
		value: string;
		subValue?: string;
	}

	interface Props {
		entries: PodiumEntry[];
	}

	let { entries }: Props = $props();

	const first = $derived(() => entries.find((e) => e.rank === 1));
	const second = $derived(() => entries.find((e) => e.rank === 2));
	const third = $derived(() => entries.find((e) => e.rank === 3));

	function getAvatarColor(rank: number): string {
		switch (rank) {
			case 1:
				return 'linear-gradient(135deg, #FFD700, #FFA500)';
			case 2:
				return 'linear-gradient(135deg, #C0C0C0, #808080)';
			case 3:
				return 'linear-gradient(135deg, #CD7F32, #8B4513)';
			default:
				return 'linear-gradient(135deg, #8b5cf6, #7c3aed)';
		}
	}

	function getMedalGlow(rank: number): string {
		switch (rank) {
			case 1:
				return '0 0 20px rgba(255, 215, 0, 0.4)';
			case 2:
				return '0 0 15px rgba(192, 192, 192, 0.3)';
			case 3:
				return '0 0 15px rgba(205, 127, 50, 0.3)';
			default:
				return 'none';
		}
	}
</script>

<div class="podium">
	<!-- 2nd Place -->
	{#if second()}
		<div class="podium-entry podium-second animate-slide-up" style="animation-delay: 0.1s">
			<div class="podium-avatar" style="background: {getAvatarColor(2)}; box-shadow: {getMedalGlow(2)}">
				{second()!.username.charAt(0)}
			</div>
			<div class="podium-medal silver">2</div>
			<div class="podium-name">{second()!.username}</div>
			<div class="podium-value">{second()!.value}</div>
			{#if second()!.subValue}
				<div class="podium-sub">{second()!.subValue}</div>
			{/if}
			<div class="podium-pillar podium-pillar-silver"></div>
		</div>
	{/if}

	<!-- 1st Place -->
	{#if first()}
		<div class="podium-entry podium-first animate-slide-up" style="animation-delay: 0s">
			<div class="podium-crown">
				<Trophy size={20} />
			</div>
			<div class="podium-avatar podium-avatar-first" style="background: {getAvatarColor(1)}; box-shadow: {getMedalGlow(1)}">
				{first()!.username.charAt(0)}
			</div>
			<div class="podium-medal gold">1</div>
			<div class="podium-name">{first()!.username}</div>
			<div class="podium-value podium-value-first">{first()!.value}</div>
			{#if first()!.subValue}
				<div class="podium-sub">{first()!.subValue}</div>
			{/if}
			<div class="podium-pillar podium-pillar-gold"></div>
		</div>
	{/if}

	<!-- 3rd Place -->
	{#if third()}
		<div class="podium-entry podium-third animate-slide-up" style="animation-delay: 0.2s">
			<div class="podium-avatar" style="background: {getAvatarColor(3)}; box-shadow: {getMedalGlow(3)}">
				{third()!.username.charAt(0)}
			</div>
			<div class="podium-medal bronze">3</div>
			<div class="podium-name">{third()!.username}</div>
			<div class="podium-value">{third()!.value}</div>
			{#if third()!.subValue}
				<div class="podium-sub">{third()!.subValue}</div>
			{/if}
			<div class="podium-pillar podium-pillar-bronze"></div>
		</div>
	{/if}
</div>
