<script lang="ts">
	import { onMount } from 'svelte';
	import { Building2, Users } from 'lucide-svelte';
	import { Card } from '$lib/components';
	import { getInstitutions, getNpcs } from '$lib/api/investorApi.js';
	import {
		INSTITUTION_TYPE_NAMES,
		INVESTMENT_STYLE_NAMES,
		TRADING_FREQUENCY_NAMES,
		type Institution,
		type Npc,
		type InvestmentStyle
	} from '$lib/types/investor.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';

	// State
	type TabType = 'institutions' | 'npcs';
	let activeTab = $state<TabType>('institutions');
	let loading = $state(true);

	let institutions = $state<Institution[]>([]);
	let instTotal = $state(0);
	let instPage = $state(0);

	let npcs = $state<Npc[]>([]);
	let npcTotal = $state(0);
	let npcPage = $state(0);

	let instHasMore = $derived(institutions.length < instTotal);
	let npcHasMore = $derived(npcs.length < npcTotal);

	async function fetchInstitutions(loadMore = false) {
		if (!loadMore) loading = true;
		try {
			const page = loadMore ? instPage + 1 : 0;
			const res = await getInstitutions({ page, size: 20 });
			if (res.success && res.data) {
				if (loadMore) {
					institutions = [...institutions, ...res.data.institutions];
				} else {
					institutions = res.data.institutions;
				}
				instTotal = res.data.total;
				instPage = page;
			}
		} catch (e) {
			console.error('Failed to fetch institutions:', e);
		} finally {
			loading = false;
		}
	}

	async function fetchNpcs(loadMore = false) {
		if (!loadMore) loading = true;
		try {
			const page = loadMore ? npcPage + 1 : 0;
			const res = await getNpcs({ page, size: 20 });
			if (res.success && res.data) {
				if (loadMore) {
					npcs = [...npcs, ...res.data.npcs];
				} else {
					npcs = res.data.npcs;
				}
				npcTotal = res.data.total;
				npcPage = page;
			}
		} catch (e) {
			console.error('Failed to fetch NPCs:', e);
		} finally {
			loading = false;
		}
	}

	function switchTab(tab: TabType) {
		activeTab = tab;
		if (tab === 'institutions' && institutions.length === 0) {
			fetchInstitutions();
		} else if (tab === 'npcs' && npcs.length === 0) {
			fetchNpcs();
		}
	}

	function formatCapital(amount: number): string {
		if (amount >= 1_000_000_000_000) return (amount / 1_000_000_000_000).toFixed(1) + '조';
		if (amount >= 100_000_000) return (amount / 100_000_000).toFixed(1) + '억';
		if (amount >= 10_000) return (amount / 10_000).toFixed(0) + '만';
		return amount.toLocaleString();
	}

	function getStyleBadgeClass(style: InvestmentStyle): string {
		switch (style) {
			case 'AGGRESSIVE':
				return 'badge-style-aggressive';
			case 'STABLE':
				return 'badge-style-stable';
			case 'VALUE':
				return 'badge-style-value';
			case 'RANDOM':
				return 'badge-style-random';
		}
	}

	function formatSectorName(sector: string): string {
		return SECTOR_NAMES[sector as Sector] ?? sector;
	}

	onMount(() => {
		fetchInstitutions();
	});
</script>

<div class="m-investors-page">
	<!-- Tab Bar -->
	<div class="investors-tab-bar">
		<button
			class="investors-tab-btn"
			class:active={activeTab === 'institutions'}
			onclick={() => switchTab('institutions')}
		>
			<Building2 size={16} />
			기관투자자
		</button>
		<button
			class="investors-tab-btn"
			class:active={activeTab === 'npcs'}
			onclick={() => switchTab('npcs')}
		>
			<Users size={16} />
			개인 (NPC)
		</button>
	</div>

	{#if loading}
		<div class="text-center p-xl text-secondary">로딩 중...</div>
	{:else if activeTab === 'institutions'}
		<div class="stack-sm">
			{#each institutions as inst}
				<Card>
					<a href="/m/investors/institution/{inst.institutionId}" class="m-investor-card-header" style="text-decoration: none; color: inherit; cursor: pointer;">
						<div class="m-investor-card-name">{inst.institutionName}</div>
						<span class="badge badge-inst-type">
							{INSTITUTION_TYPE_NAMES[inst.institutionType]}
						</span>
					</a>
					<div class="m-investor-card-meta">
						<span class="m-investor-card-label">투자성향</span>
						<span class="badge {getStyleBadgeClass(inst.investmentStyle)}">
							{INVESTMENT_STYLE_NAMES[inst.investmentStyle]}
						</span>
						<span class="m-investor-card-label">자본금</span>
						<span class="font-semibold capital-text">{formatCapital(inst.capital)}</span>
						<span class="m-investor-card-label">일일수입</span>
						<span class="capital-text">{formatCapital(inst.dailyIncome)}</span>
						<span class="m-investor-card-label">거래빈도</span>
						<span>{TRADING_FREQUENCY_NAMES[inst.tradingFrequency]}</span>
					</div>
					{#if inst.preferredSectors.length > 0}
						<div class="m-investor-card-sectors">
							<div class="sector-tags">
								{#each inst.preferredSectors as sector}
									<span class="sector-tag">{formatSectorName(sector)}</span>
								{/each}
							</div>
						</div>
					{/if}
				</Card>
			{/each}
		</div>

		{#if instHasMore}
			<div class="text-center p-md">
				<button class="btn btn-sm btn-secondary" onclick={() => fetchInstitutions(true)}>
					더 보기
				</button>
			</div>
		{/if}

		{#if institutions.length === 0}
			<div class="text-center p-xl text-secondary">기관투자자가 없습니다</div>
		{/if}
	{:else}
		<div class="stack-sm">
			{#each npcs as npc}
				<Card>
					<a href="/m/investors/npc/{npc.npcId}" class="m-investor-card-header" style="text-decoration: none; color: inherit; cursor: pointer;">
						<div class="m-investor-card-name">{npc.npcName}</div>
						<span class="badge {getStyleBadgeClass(npc.investmentStyle)}">
							{INVESTMENT_STYLE_NAMES[npc.investmentStyle]}
						</span>
					</a>
					<div class="m-investor-card-meta">
						<span class="m-investor-card-label">자본금</span>
						<span class="font-semibold capital-text">{formatCapital(npc.capital)}</span>
						<span class="m-investor-card-label">주간수입</span>
						<span class="capital-text">{formatCapital(npc.weeklyIncome)}</span>
						<span class="m-investor-card-label">위험허용도</span>
						<span>{(npc.riskTolerance * 100).toFixed(0)}%</span>
						<span class="m-investor-card-label">거래빈도</span>
						<span>{TRADING_FREQUENCY_NAMES[npc.tradingFrequency]}</span>
					</div>
					{#if npc.preferredSectors.length > 0}
						<div class="m-investor-card-sectors">
							<div class="sector-tags">
								{#each npc.preferredSectors as sector}
									<span class="sector-tag">{formatSectorName(sector)}</span>
								{/each}
							</div>
						</div>
					{/if}
				</Card>
			{/each}
		</div>

		{#if npcHasMore}
			<div class="text-center p-md">
				<button class="btn btn-sm btn-secondary" onclick={() => fetchNpcs(true)}>
					더 보기
				</button>
			</div>
		{/if}

		{#if npcs.length === 0}
			<div class="text-center p-xl text-secondary">개인투자자(NPC)가 없습니다</div>
		{/if}
	{/if}
</div>
