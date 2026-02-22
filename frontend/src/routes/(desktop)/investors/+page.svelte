<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import {
		Building2,
		Users,
		Wallet,
		ChevronLeft,
		ChevronRight,
		Search
	} from 'lucide-svelte';
	import { getInstitutions, getNpcs } from '$lib/api/investorApi.js';
	import {
		INSTITUTION_TYPE_NAMES,
		INVESTMENT_STYLE_NAMES,
		TRADING_FREQUENCY_NAMES,
		type Institution,
		type Npc,
		type InvestmentStyle,
		type TradingFrequency
	} from '$lib/types/investor.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';

	// State
	type TabType = 'institutions' | 'npcs';
	let activeTab = $state<TabType>('institutions');
	let loading = $state(true);

	// Institution state
	let institutions = $state<Institution[]>([]);
	let instTotal = $state(0);
	let instPage = $state(0);
	let instPageSize = $state(20);

	// NPC state
	let npcs = $state<Npc[]>([]);
	let npcTotal = $state(0);
	let npcPage = $state(0);
	let npcPageSize = $state(20);

	// Derived
	let instTotalPages = $derived(Math.max(1, Math.ceil(instTotal / instPageSize)));
	let npcTotalPages = $derived(Math.max(1, Math.ceil(npcTotal / npcPageSize)));

	let totalCapital = $derived(() => {
		if (activeTab === 'institutions') {
			return institutions.reduce((sum, i) => sum + i.capital, 0);
		}
		return npcs.reduce((sum, n) => sum + n.capital, 0);
	});

	// Fetch functions
	async function fetchInstitutions() {
		loading = true;
		try {
			const res = await getInstitutions({ page: instPage, size: instPageSize });
			if (res.success && res.data) {
				institutions = res.data.institutions;
				instTotal = res.data.total;
			}
		} catch (e) {
			console.error('Failed to fetch institutions:', e);
		} finally {
			loading = false;
		}
	}

	async function fetchNpcs() {
		loading = true;
		try {
			const res = await getNpcs({ page: npcPage, size: npcPageSize });
			if (res.success && res.data) {
				npcs = res.data.npcs;
				npcTotal = res.data.total;
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

	function goToInstPage(page: number) {
		if (page < 0 || page >= instTotalPages) return;
		instPage = page;
		fetchInstitutions();
	}

	function goToNpcPage(page: number) {
		if (page < 0 || page >= npcTotalPages) return;
		npcPage = page;
		fetchNpcs();
	}

	// Formatting helpers
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

	function getFreqBadgeClass(freq: TradingFrequency): string {
		switch (freq) {
			case 'HIGH':
				return 'badge-freq-high';
			case 'MEDIUM':
				return 'badge-freq-medium';
			case 'LOW':
				return 'badge-freq-low';
		}
	}

	function getRiskLevel(tolerance: number): string {
		if (tolerance >= 0.7) return 'risk-high';
		if (tolerance >= 0.4) return 'risk-medium';
		return 'risk-low';
	}

	function formatSectorName(sector: string): string {
		return SECTOR_NAMES[sector as Sector] ?? sector;
	}

	function getVisiblePages(current: number, total: number): (number | '...')[] {
		if (total <= 7) {
			return Array.from({ length: total }, (_, i) => i);
		}
		const pages: (number | '...')[] = [];
		pages.push(0);
		if (current > 3) {
			pages.push('...');
		}
		const start = Math.max(1, current - 1);
		const end = Math.min(total - 2, current + 1);
		for (let i = start; i <= end; i++) {
			pages.push(i);
		}
		if (current < total - 4) {
			pages.push('...');
		}
		pages.push(total - 1);
		return pages;
	}

	onMount(() => {
		fetchInstitutions();
	});
</script>

<div class="investors-page">
	<!-- Page Header -->
	<div class="flex justify-between items-center mb-lg">
		<h1 class="text-2xl font-bold gradient-text">투자자 목록</h1>
	</div>

	<!-- Tab Bar -->
	<div class="investors-tab-bar">
		<button
			class="investors-tab-btn"
			class:active={activeTab === 'institutions'}
			onclick={() => switchTab('institutions')}
		>
			<Building2 size={18} />
			기관투자자
			{#if instTotal > 0}
				<span class="tab-count">{instTotal}</span>
			{/if}
		</button>
		<button
			class="investors-tab-btn"
			class:active={activeTab === 'npcs'}
			onclick={() => switchTab('npcs')}
		>
			<Users size={18} />
			개인투자자 (NPC)
			{#if npcTotal > 0}
				<span class="tab-count">{npcTotal}</span>
			{/if}
		</button>
	</div>

	<!-- Stats Summary -->
	<div class="investors-stats">
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon" style="background: rgba(139, 92, 246, 0.15); color: var(--color-primary-light);">
					{#if activeTab === 'institutions'}
						<Building2 size={16} />
					{:else}
						<Users size={16} />
					{/if}
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">총 투자자 수</span>
					<span class="investors-stat-value">
						{activeTab === 'institutions' ? instTotal : npcTotal}
					</span>
				</div>
			</div>
		</div>
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon" style="background: rgba(16, 185, 129, 0.15); color: var(--color-stock-up);">
					<Wallet size={16} />
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">총 자본금</span>
					<span class="investors-stat-value capital-text">
						{formatCapital(totalCapital())}
					</span>
				</div>
			</div>
		</div>
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon" style="background: rgba(6, 182, 212, 0.15); color: var(--color-accent);">
					<Wallet size={16} />
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">
						{activeTab === 'institutions' ? '평균 일일수입' : '평균 주간수입'}
					</span>
					<span class="investors-stat-value capital-text">
						{#if activeTab === 'institutions'}
							{formatCapital(institutions.length > 0 ? institutions.reduce((s, i) => s + i.dailyIncome, 0) / institutions.length : 0)}
						{:else}
							{formatCapital(npcs.length > 0 ? npcs.reduce((s, n) => s + n.weeklyIncome, 0) / npcs.length : 0)}
						{/if}
					</span>
				</div>
			</div>
		</div>
	</div>

	<!-- Loading State -->
	{#if loading}
		<div class="investors-loading">
			<div class="investors-loading-spinner"></div>
			<div class="text-secondary text-sm">로딩 중...</div>
		</div>
	{:else if activeTab === 'institutions'}
		<!-- Institution Table -->
		{#if institutions.length === 0}
			<div class="table-wrapper">
				<div class="table-empty">
					<div class="table-empty-icon">
						<Search size={48} />
					</div>
					<div class="table-empty-text">기관투자자가 없습니다</div>
				</div>
			</div>
		{:else}
			<div class="table-wrapper">
				<table class="table table-stock">
					<thead>
						<tr>
							<th>이름</th>
							<th>유형</th>
							<th>투자성향</th>
							<th class="text-right">자본금</th>
							<th class="text-right">일일수입</th>
							<th>위험허용도</th>
							<th>선호섹터</th>
							<th>거래빈도</th>
						</tr>
					</thead>
					<tbody>
						{#each institutions as inst}
							<tr class="investor-row-clickable" onclick={() => goto(`/investors/institution/${inst.institutionId}`)}>
								<td>
									<div class="investor-name-cell">
										<div class="investor-avatar">
											{inst.institutionName.charAt(0)}
										</div>
										<span class="font-medium">{inst.institutionName}</span>
									</div>
								</td>
								<td>
									<span class="badge badge-inst-type">
										{INSTITUTION_TYPE_NAMES[inst.institutionType]}
									</span>
								</td>
								<td>
									<span class="badge {getStyleBadgeClass(inst.investmentStyle)}">
										{INVESTMENT_STYLE_NAMES[inst.investmentStyle]}
									</span>
								</td>
								<td class="text-right capital-text">{formatCapital(inst.capital)}</td>
								<td class="text-right capital-text">{formatCapital(inst.dailyIncome)}</td>
								<td>
									<div class="risk-bar">
										<div class="risk-bar-track">
											<div
												class="risk-bar-fill {getRiskLevel(inst.riskTolerance)}"
												style="width: {inst.riskTolerance * 100}%"
											></div>
										</div>
										<span class="risk-bar-label">{(inst.riskTolerance * 100).toFixed(0)}%</span>
									</div>
								</td>
								<td>
									<div class="sector-tags">
										{#each inst.preferredSectors as sector}
											<span class="sector-tag">{formatSectorName(sector)}</span>
										{/each}
									</div>
								</td>
								<td>
									<span class="badge {getFreqBadgeClass(inst.tradingFrequency)}">
										{TRADING_FREQUENCY_NAMES[inst.tradingFrequency]}
									</span>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>

			<!-- Pagination -->
			{#if instTotal > 0}
				<div class="pagination-bar">
					<button
						class="pagination-btn"
						disabled={instPage === 0}
						onclick={() => goToInstPage(instPage - 1)}
					>
						<ChevronLeft size={16} />
					</button>

					{#each getVisiblePages(instPage, instTotalPages) as page}
						{#if page === '...'}
							<span class="pagination-ellipsis">...</span>
						{:else}
							<button
								class="pagination-btn"
								class:active={instPage === page}
								onclick={() => goToInstPage(page)}
							>
								{page + 1}
							</button>
						{/if}
					{/each}

					<button
						class="pagination-btn"
						disabled={instPage >= instTotalPages - 1}
						onclick={() => goToInstPage(instPage + 1)}
					>
						<ChevronRight size={16} />
					</button>

					<span class="pagination-info">
						<strong>{instPage * instPageSize + 1}</strong>-<strong>{Math.min((instPage + 1) * instPageSize, instTotal)}</strong>
						/ {instTotal}건
					</span>
				</div>
			{/if}
		{/if}
	{:else}
		<!-- NPC Table -->
		{#if npcs.length === 0}
			<div class="table-wrapper">
				<div class="table-empty">
					<div class="table-empty-icon">
						<Search size={48} />
					</div>
					<div class="table-empty-text">개인투자자(NPC)가 없습니다</div>
				</div>
			</div>
		{:else}
			<div class="table-wrapper">
				<table class="table table-stock">
					<thead>
						<tr>
							<th>이름</th>
							<th>투자성향</th>
							<th class="text-right">자본금</th>
							<th class="text-right">주간수입</th>
							<th>위험허용도</th>
							<th>선호섹터</th>
							<th>거래빈도</th>
						</tr>
					</thead>
					<tbody>
						{#each npcs as npc}
							<tr class="investor-row-clickable" onclick={() => goto(`/investors/npc/${npc.npcId}`)}>
								<td>
									<div class="investor-name-cell">
										<div class="investor-avatar npc">
											{npc.npcName.charAt(0)}
										</div>
										<span class="font-medium">{npc.npcName}</span>
									</div>
								</td>
								<td>
									<span class="badge {getStyleBadgeClass(npc.investmentStyle)}">
										{INVESTMENT_STYLE_NAMES[npc.investmentStyle]}
									</span>
								</td>
								<td class="text-right capital-text">{formatCapital(npc.capital)}</td>
								<td class="text-right capital-text">{formatCapital(npc.weeklyIncome)}</td>
								<td>
									<div class="risk-bar">
										<div class="risk-bar-track">
											<div
												class="risk-bar-fill {getRiskLevel(npc.riskTolerance)}"
												style="width: {npc.riskTolerance * 100}%"
											></div>
										</div>
										<span class="risk-bar-label">{(npc.riskTolerance * 100).toFixed(0)}%</span>
									</div>
								</td>
								<td>
									<div class="sector-tags">
										{#each npc.preferredSectors as sector}
											<span class="sector-tag">{formatSectorName(sector)}</span>
										{/each}
									</div>
								</td>
								<td>
									<span class="badge {getFreqBadgeClass(npc.tradingFrequency)}">
										{TRADING_FREQUENCY_NAMES[npc.tradingFrequency]}
									</span>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>

			<!-- Pagination -->
			{#if npcTotal > 0}
				<div class="pagination-bar">
					<button
						class="pagination-btn"
						disabled={npcPage === 0}
						onclick={() => goToNpcPage(npcPage - 1)}
					>
						<ChevronLeft size={16} />
					</button>

					{#each getVisiblePages(npcPage, npcTotalPages) as page}
						{#if page === '...'}
							<span class="pagination-ellipsis">...</span>
						{:else}
							<button
								class="pagination-btn"
								class:active={npcPage === page}
								onclick={() => goToNpcPage(page)}
							>
								{page + 1}
							</button>
						{/if}
					{/each}

					<button
						class="pagination-btn"
						disabled={npcPage >= npcTotalPages - 1}
						onclick={() => goToNpcPage(npcPage + 1)}
					>
						<ChevronRight size={16} />
					</button>

					<span class="pagination-info">
						<strong>{npcPage * npcPageSize + 1}</strong>-<strong>{Math.min((npcPage + 1) * npcPageSize, npcTotal)}</strong>
						/ {npcTotal}건
					</span>
				</div>
			{/if}
		{/if}
	{/if}
</div>
