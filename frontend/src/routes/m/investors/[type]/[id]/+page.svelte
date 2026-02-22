<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { getEnrichedPortfolio, getStockLookupMap, mapTradesToTransactions } from '$lib/api/portfolioHelper.js';
	import { getTradeHistory } from '$lib/api/tradingApi.js';
	import { getInstitutions, getNpcs } from '$lib/api/investorApi.js';
	import { Card } from '$lib/components';
	import {
		INSTITUTION_TYPE_NAMES,
		INVESTMENT_STYLE_NAMES,
		TRADING_FREQUENCY_NAMES,
		type Institution,
		type Npc,
		type InvestmentStyle
	} from '$lib/types/investor.js';
	import { SECTOR_NAMES, type Sector } from '$lib/types/stock.js';
	import type { Portfolio, Transaction } from '$lib/types/user.js';
	import {
		ArrowLeft,
		Building2,
		Users,
		Wallet,
		CreditCard,
		TrendingUp,
		Activity,
		ArrowUpRight,
		ArrowDownRight
	} from 'lucide-svelte';

	let investorType = $derived($page.params.type);
	let investorId = $derived($page.params.id);

	let loading = $state(true);
	let npcProfile = $state<Npc | null>(null);
	let instProfile = $state<Institution | null>(null);
	let portfolio = $state<Portfolio | null>(null);
	let transactions = $state<Transaction[]>([]);

	let profileName = $derived(
		investorType === 'institution'
			? instProfile?.institutionName ?? ''
			: npcProfile?.npcName ?? ''
	);

	let tradingInvestorId = $derived(
		investorType === 'institution' ? `INST_${investorId}` : `NPC_${investorId}`
	);

	let tradingInvestorType = $derived(
		investorType === 'institution' ? 'INSTITUTION' : 'NPC'
	);

	async function fetchProfile() {
		try {
			if (investorType === 'institution') {
				const res = await getInstitutions({ page: 0, size: 500 });
				if (res.success && res.data) {
					instProfile = res.data.institutions.find(
						(i) => String(i.institutionId) === investorId
					) ?? null;
				}
			} else {
				const res = await getNpcs({ page: 0, size: 500 });
				if (res.success && res.data) {
					npcProfile = res.data.npcs.find(
						(n) => String(n.npcId) === investorId
					) ?? null;
				}
			}
		} catch (e) {
			console.error('Failed to fetch profile:', e);
		}
	}

	async function fetchPortfolioData() {
		try {
			const [enrichedPortfolio, tradesRes, stockMap] = await Promise.all([
				getEnrichedPortfolio(tradingInvestorId, tradingInvestorType),
				getTradeHistory(tradingInvestorId, tradingInvestorType).catch(() => null),
				getStockLookupMap()
			]);

			if (enrichedPortfolio) {
				portfolio = enrichedPortfolio;
			}

			if (tradesRes?.success && tradesRes.data) {
				transactions = mapTradesToTransactions(tradesRes.data, tradingInvestorId, stockMap);
			}
		} catch (e) {
			console.error('Failed to fetch portfolio data:', e);
		}
	}

	onMount(async () => {
		await fetchProfile();
		await fetchPortfolioData();
		loading = false;
	});

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatCapital(amount: number): string {
		if (amount >= 1_000_000_000_000) return (amount / 1_000_000_000_000).toFixed(1) + '\uC870';
		if (amount >= 100_000_000) return (amount / 100_000_000).toFixed(1) + '\uC5B5';
		if (amount >= 10_000) return (amount / 10_000).toFixed(0) + '\uB9CC';
		return amount.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	function getStyleBadgeClass(style: InvestmentStyle): string {
		switch (style) {
			case 'AGGRESSIVE': return 'badge-style-aggressive';
			case 'STABLE': return 'badge-style-stable';
			case 'VALUE': return 'badge-style-value';
			case 'RANDOM': return 'badge-style-random';
		}
	}

	function formatSectorName(sector: string): string {
		return SECTOR_NAMES[sector as Sector] ?? sector;
	}
</script>

<div class="m-investors-page">
	<!-- Back Header -->
	<div class="flex items-center gap-sm mb-md">
		<button class="btn btn-ghost btn-sm" onclick={() => goto('/m/investors')}>
			<ArrowLeft size={18} />
		</button>
		<h1 class="text-lg font-bold">{profileName || '투자자 상세'}</h1>
	</div>

	{#if loading}
		<div class="text-center p-xl text-secondary">로딩 중...</div>
	{:else if !npcProfile && !instProfile}
		<div class="text-center p-lg text-secondary">투자자 정보를 찾을 수 없습니다.</div>
	{:else}
		<!-- Profile Card -->
		<Card>
			<div class="flex items-center gap-md mb-md">
				<div class="investor-avatar" class:npc={investorType === 'npc'} style="width: 48px; height: 48px; font-size: 18px;">
					{profileName.charAt(0)}
				</div>
				<div style="flex: 1;">
					<div class="font-bold text-lg">{profileName}</div>
					<div class="flex gap-xs flex-wrap mt-xs">
						{#if investorType === 'institution' && instProfile}
							<span class="badge badge-inst-type">{INSTITUTION_TYPE_NAMES[instProfile.institutionType]}</span>
							<span class="badge {getStyleBadgeClass(instProfile.investmentStyle)}">{INVESTMENT_STYLE_NAMES[instProfile.investmentStyle]}</span>
						{:else if npcProfile}
							<span class="badge {getStyleBadgeClass(npcProfile.investmentStyle)}">{INVESTMENT_STYLE_NAMES[npcProfile.investmentStyle]}</span>
						{/if}
					</div>
				</div>
			</div>
			<div class="m-investor-card-meta">
				<span class="m-investor-card-label">자본금</span>
				<span class="font-semibold capital-text">{formatCapital(investorType === 'institution' ? instProfile?.capital ?? 0 : npcProfile?.capital ?? 0)}</span>
				<span class="m-investor-card-label">{investorType === 'institution' ? '일일수입' : '주간수입'}</span>
				<span class="capital-text">{formatCapital(investorType === 'institution' ? instProfile?.dailyIncome ?? 0 : npcProfile?.weeklyIncome ?? 0)}</span>
				<span class="m-investor-card-label">거래빈도</span>
				<span>{TRADING_FREQUENCY_NAMES[(investorType === 'institution' ? instProfile?.tradingFrequency : npcProfile?.tradingFrequency) ?? 'MEDIUM']}</span>
				<span class="m-investor-card-label">위험허용도</span>
				<span>{(((investorType === 'institution' ? instProfile?.riskTolerance : npcProfile?.riskTolerance) ?? 0) * 100).toFixed(0)}%</span>
			</div>
			{#if ((investorType === 'institution' ? instProfile?.preferredSectors : npcProfile?.preferredSectors) ?? []).length > 0}
				<div class="m-investor-card-sectors">
					<div class="sector-tags">
						{#each (investorType === 'institution' ? instProfile?.preferredSectors : npcProfile?.preferredSectors) ?? [] as sector}
							<span class="sector-tag">{formatSectorName(sector)}</span>
						{/each}
					</div>
				</div>
			{/if}
		</Card>

		<!-- Asset Summary -->
		<div class="stack-sm mt-md mb-md">
			<div class="grid grid-cols-2 gap-sm">
				<Card>
					<div class="text-xs text-secondary mb-xs">보유 현금</div>
					<div class="font-bold capital-text">{formatCapital(portfolio?.cashBalance ?? 0)}</div>
				</Card>
				<Card>
					<div class="text-xs text-secondary mb-xs">주식 평가금</div>
					<div class="font-bold capital-text">{formatCapital(portfolio?.totalStockValue ?? 0)}</div>
				</Card>
				<Card>
					<div class="text-xs text-secondary mb-xs">총 자산</div>
					<div class="font-bold capital-text">{formatCapital(portfolio?.totalAssetValue ?? 0)}</div>
				</Card>
				<Card>
					<div class="text-xs text-secondary mb-xs">총 손익</div>
					{#if portfolio}
						<div class="font-bold" class:text-stock-up={portfolio.totalProfitLoss >= 0} class:text-stock-down={portfolio.totalProfitLoss < 0}>
							{portfolio.totalProfitLoss >= 0 ? '+' : ''}{formatCapital(portfolio.totalProfitLoss)}
						</div>
					{:else}
						<div class="font-bold">0</div>
					{/if}
				</Card>
			</div>
		</div>

		<!-- Holdings -->
		<Card>
			<h3 class="font-bold mb-sm">보유 종목</h3>
			{#if portfolio && portfolio.holdings.length > 0}
				{#each portfolio.holdings as holding}
					<a href="/m/stocks/{holding.stockId}" class="transaction-item" style="text-decoration: none; color: inherit;">
						<div style="flex: 1;">
							<div class="font-medium">{holding.stockName}</div>
							<div class="text-xs text-secondary">{holding.quantity}주 | 매입 {formatPrice(holding.averagePrice)}원</div>
						</div>
						<div class="text-right">
							<div class="font-medium">{formatPrice(holding.currentPrice)}원</div>
							<div class="text-xs" class:text-stock-up={holding.profitLoss >= 0} class:text-stock-down={holding.profitLoss < 0}>
								{formatPercent(holding.profitLossPercent)}
							</div>
						</div>
					</a>
				{/each}
			{:else}
				<div class="text-center text-secondary p-md text-sm">보유 종목이 없습니다</div>
			{/if}
		</Card>

		<!-- Recent Transactions -->
		<Card>
			<h3 class="font-bold mb-sm">최근 거래</h3>
			{#each transactions.slice(0, 20) as tx}
				<div class="transaction-item">
					<div class="transaction-icon" class:buy={tx.side === 'BUY'} class:sell={tx.side === 'SELL'}>
						{#if tx.side === 'BUY'}
							<ArrowDownRight size={16} />
						{:else}
							<ArrowUpRight size={16} />
						{/if}
					</div>
					<div style="flex: 1;">
						<div class="font-medium text-sm">{tx.stockName}</div>
						<div class="text-xs text-secondary">{new Date(tx.timestamp).toLocaleDateString('ko-KR')}</div>
					</div>
					<div class="text-right">
						<div class="badge text-xs" class:badge-success={tx.side === 'BUY'} class:badge-error={tx.side === 'SELL'}>
							{tx.side === 'BUY' ? '매수' : '매도'}
						</div>
						<div class="text-xs mt-xs">{tx.quantity}주</div>
					</div>
				</div>
			{/each}
			{#if transactions.length === 0}
				<div class="text-center text-secondary p-md text-sm">거래 내역이 없습니다</div>
			{/if}
		</Card>
	{/if}
</div>
