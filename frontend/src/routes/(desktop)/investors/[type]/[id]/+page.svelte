<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { getEnrichedPortfolio, getStockLookupMap, mapTradesToTransactions } from '$lib/api/portfolioHelper.js';
	import { getTradeHistory } from '$lib/api/tradingApi.js';
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

	// Route params
	let investorType = $derived($page.params.type); // 'npc' or 'institution'
	let investorId = $derived($page.params.id);

	// State
	let loading = $state(true);
	let npcProfile = $state<Npc | null>(null);
	let instProfile = $state<Institution | null>(null);
	let portfolio = $state<Portfolio | null>(null);
	let transactions = $state<Transaction[]>([]);

	let activeFilter = $state<'ALL' | 'BUY' | 'SELL'>('ALL');

	const filteredTransactions = $derived(
		activeFilter === 'ALL'
			? transactions
			: transactions.filter((tx) => tx.side === activeFilter)
	);

	// Derived profile info
	let profileName = $derived(
		investorType === 'institution'
			? instProfile?.institutionName ?? ''
			: npcProfile?.npcName ?? ''
	);

	let profileCapital = $derived(
		investorType === 'institution'
			? instProfile?.capital ?? 0
			: npcProfile?.capital ?? 0
	);

	// Trading investorId format
	let tradingInvestorId = $derived(
		investorType === 'institution'
			? `INST_${investorId}`
			: `NPC_${investorId}`
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

	function getHoldingWeight(holdingValue: number): number {
		if (!portfolio || portfolio.totalStockValue === 0) return 0;
		return (holdingValue / portfolio.totalStockValue) * 100;
	}

	function getStyleBadgeClass(style: InvestmentStyle): string {
		switch (style) {
			case 'AGGRESSIVE': return 'badge-style-aggressive';
			case 'STABLE': return 'badge-style-stable';
			case 'VALUE': return 'badge-style-value';
			case 'RANDOM': return 'badge-style-random';
		}
	}

	function getFreqBadgeClass(freq: TradingFrequency): string {
		switch (freq) {
			case 'HIGH': return 'badge-freq-high';
			case 'MEDIUM': return 'badge-freq-medium';
			case 'LOW': return 'badge-freq-low';
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
</script>

<div class="investor-detail-page">
	<!-- Back Button & Header -->
	<div class="flex items-center gap-md mb-lg">
		<button class="btn btn-ghost btn-sm" onclick={() => goto('/investors')}>
			<ArrowLeft size={18} />
			<span>목록으로</span>
		</button>
	</div>

	{#if loading}
		<div class="investors-loading">
			<div class="investors-loading-spinner"></div>
			<div class="text-secondary text-sm">로딩 중...</div>
		</div>
	{:else if !npcProfile && !instProfile}
		<div class="text-center p-lg">
			<p class="text-secondary">투자자 정보를 찾을 수 없습니다.</p>
		</div>
	{:else}
		<!-- Profile Card -->
		<div class="investor-detail-profile">
			<div class="investor-detail-avatar" class:npc={investorType === 'npc'}>
				{#if investorType === 'institution'}
					<Building2 size={28} />
				{:else}
					<Users size={28} />
				{/if}
			</div>
			<div class="investor-detail-profile-info">
				<div class="investor-detail-name">{profileName}</div>
				<div class="investor-detail-meta">
					{#if investorType === 'institution' && instProfile}
						<span class="badge badge-inst-type">
							{INSTITUTION_TYPE_NAMES[instProfile.institutionType]}
						</span>
						<span class="badge {getStyleBadgeClass(instProfile.investmentStyle)}">
							{INVESTMENT_STYLE_NAMES[instProfile.investmentStyle]}
						</span>
						<span class="badge {getFreqBadgeClass(instProfile.tradingFrequency)}">
							{TRADING_FREQUENCY_NAMES[instProfile.tradingFrequency]}
						</span>
					{:else if npcProfile}
						<span class="badge {getStyleBadgeClass(npcProfile.investmentStyle)}">
							{INVESTMENT_STYLE_NAMES[npcProfile.investmentStyle]}
						</span>
						<span class="badge {getFreqBadgeClass(npcProfile.tradingFrequency)}">
							{TRADING_FREQUENCY_NAMES[npcProfile.tradingFrequency]}
						</span>
					{/if}
				</div>
			</div>
			<div class="investor-detail-profile-stats">
				<div class="investor-detail-profile-stat">
					<div class="investor-detail-profile-stat-value capital-text">{formatCapital(profileCapital)}</div>
					<div class="investor-detail-profile-stat-label">초기 자본금</div>
				</div>
				{#if investorType === 'institution' && instProfile}
					<div class="investor-detail-profile-stat">
						<div class="investor-detail-profile-stat-value capital-text">{formatCapital(instProfile.dailyIncome)}</div>
						<div class="investor-detail-profile-stat-label">일일 수입</div>
					</div>
					<div class="investor-detail-profile-stat">
						<div class="risk-bar">
							<div class="risk-bar-track" style="max-width: 60px;">
								<div
									class="risk-bar-fill {getRiskLevel(instProfile.riskTolerance)}"
									style="width: {instProfile.riskTolerance * 100}%"
								></div>
							</div>
							<span class="risk-bar-label">{(instProfile.riskTolerance * 100).toFixed(0)}%</span>
						</div>
						<div class="investor-detail-profile-stat-label">위험허용도</div>
					</div>
				{:else if npcProfile}
					<div class="investor-detail-profile-stat">
						<div class="investor-detail-profile-stat-value capital-text">{formatCapital(npcProfile.weeklyIncome)}</div>
						<div class="investor-detail-profile-stat-label">주간 수입</div>
					</div>
					<div class="investor-detail-profile-stat">
						<div class="risk-bar">
							<div class="risk-bar-track" style="max-width: 60px;">
								<div
									class="risk-bar-fill {getRiskLevel(npcProfile.riskTolerance)}"
									style="width: {npcProfile.riskTolerance * 100}%"
								></div>
							</div>
							<span class="risk-bar-label">{(npcProfile.riskTolerance * 100).toFixed(0)}%</span>
						</div>
						<div class="investor-detail-profile-stat-label">위험허용도</div>
					</div>
				{/if}
			</div>
		</div>

		<!-- Preferred Sectors -->
		{#if (investorType === 'institution' && instProfile) || npcProfile}
			<div class="investor-detail-sectors mb-lg">
				<span class="text-sm text-secondary">선호 섹터:</span>
				<div class="sector-tags">
					{#each (investorType === 'institution' ? instProfile?.preferredSectors : npcProfile?.preferredSectors) ?? [] as sector}
						<span class="sector-tag">{formatSectorName(sector)}</span>
					{/each}
				</div>
			</div>
		{/if}

		<!-- Asset Summary Cards -->
		<div class="stat-cards-grid mb-lg">
			<div class="card card-hover animate-slide-up" style="animation-delay: 0ms">
				<div class="card-stat">
					<div class="card-stat-icon">
						<Wallet size={16} />
					</div>
					<div class="card-stat-label">총 자산</div>
					<div class="card-stat-value">{formatCapital(portfolio?.totalAssetValue ?? 0)}</div>
				</div>
			</div>
			<div class="card card-hover animate-slide-up" style="animation-delay: 50ms">
				<div class="card-stat">
					<div class="card-stat-icon">
						<CreditCard size={16} />
					</div>
					<div class="card-stat-label">보유 현금</div>
					<div class="card-stat-value">{formatCapital(portfolio?.cashBalance ?? 0)}</div>
				</div>
			</div>
			<div class="card card-hover animate-slide-up" style="animation-delay: 100ms">
				<div class="card-stat">
					<div class="card-stat-icon">
						<TrendingUp size={16} />
					</div>
					<div class="card-stat-label">주식 평가금</div>
					<div class="card-stat-value">{formatCapital(portfolio?.totalStockValue ?? 0)}</div>
				</div>
			</div>
			<div class="card card-hover animate-slide-up" style="animation-delay: 150ms">
				<div class="card-stat">
					<div class="card-stat-icon">
						<Activity size={16} />
					</div>
					<div class="card-stat-label">총 손익</div>
					{#if portfolio}
						<div class="card-stat-value" class:text-stock-up={portfolio.totalProfitLoss >= 0} class:text-stock-down={portfolio.totalProfitLoss < 0}>
							{portfolio.totalProfitLoss >= 0 ? '+' : ''}{formatCapital(portfolio.totalProfitLoss)}
						</div>
						<div class="card-stat-change" class:up={portfolio.totalProfitLossPercent >= 0} class:down={portfolio.totalProfitLossPercent < 0}>
							{formatPercent(portfolio.totalProfitLossPercent)}
						</div>
					{:else}
						<div class="card-stat-value">0</div>
					{/if}
				</div>
			</div>
		</div>

		<div class="grid grid-cols-2 gap-lg">
			<!-- Holdings Table -->
			<div class="card animate-slide-up" style="animation-delay: 200ms">
				<div class="card-header">
					<h3 class="card-title">보유 종목</h3>
				</div>
				{#if portfolio && portfolio.holdings.length > 0}
					<div class="table-wrapper border-none" style="border: none; box-shadow: none; border-radius: 0; background: transparent; backdrop-filter: none;">
						<table class="table table-portfolio">
							<thead>
								<tr>
									<th>종목</th>
									<th class="text-right">보유수량</th>
									<th class="text-right">매입가</th>
									<th class="text-right">현재가</th>
									<th class="text-right">손익</th>
									<th>비중</th>
								</tr>
							</thead>
							<tbody>
								{#each portfolio.holdings as holding}
									<tr>
										<td>
											<a href="/stocks/{holding.stockId}" class="holding-name font-medium text-link">
												{holding.stockName}
											</a>
										</td>
										<td class="text-right">{holding.quantity}주</td>
										<td class="text-right">{formatPrice(holding.averagePrice)}원</td>
										<td class="text-right">{formatPrice(holding.currentPrice)}원</td>
										<td class="text-right">
											<div class="profit-loss" class:positive={holding.profitLoss >= 0} class:negative={holding.profitLoss < 0}>
												{holding.profitLoss >= 0 ? '+' : ''}{formatPrice(holding.profitLoss)}원
												<span class="text-xs">({formatPercent(holding.profitLossPercent)})</span>
											</div>
										</td>
										<td>
											<div class="holding-weight-bar">
												<div class="holding-weight-track">
													<div
														class="holding-weight-fill"
														style="width: {getHoldingWeight(holding.totalValue)}%"
													></div>
												</div>
												<span class="holding-weight-value">
													{getHoldingWeight(holding.totalValue).toFixed(1)}%
												</span>
											</div>
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					</div>
				{:else}
					<div class="text-center text-secondary p-lg">보유 종목이 없습니다</div>
				{/if}
			</div>

			<!-- Recent Transactions -->
			<div class="card animate-slide-up" style="animation-delay: 250ms">
				<div class="card-header">
					<h3 class="card-title">최근 거래 내역</h3>
				</div>
				<div class="transaction-filter-bar">
					<button
						class="btn btn-sm"
						class:btn-primary={activeFilter === 'ALL'}
						class:btn-ghost={activeFilter !== 'ALL'}
						onclick={() => (activeFilter = 'ALL')}
					>
						전체
					</button>
					<button
						class="btn btn-sm"
						class:btn-primary={activeFilter === 'BUY'}
						class:btn-ghost={activeFilter !== 'BUY'}
						onclick={() => (activeFilter = 'BUY')}
					>
						매수
					</button>
					<button
						class="btn btn-sm"
						class:btn-primary={activeFilter === 'SELL'}
						class:btn-ghost={activeFilter !== 'SELL'}
						onclick={() => (activeFilter = 'SELL')}
					>
						매도
					</button>
				</div>
				<div class="card-body p-0">
					{#each filteredTransactions as tx}
						<div class="transaction-item">
							<div class="transaction-icon" class:buy={tx.side === 'BUY'} class:sell={tx.side === 'SELL'}>
								{#if tx.side === 'BUY'}
									<ArrowDownRight size={18} />
								{:else}
									<ArrowUpRight size={18} />
								{/if}
							</div>
							<div style="flex: 1;">
								<div class="font-medium">{tx.stockName}</div>
								<div class="text-xs text-secondary">
									{new Date(tx.timestamp).toLocaleDateString('ko-KR')}
								</div>
							</div>
							<div class="text-right">
								<div class="badge" class:badge-success={tx.side === 'BUY'} class:badge-error={tx.side === 'SELL'}>
									{tx.side === 'BUY' ? '매수' : '매도'}
								</div>
								<div class="text-sm mt-xs">{tx.quantity}주 x {formatPrice(tx.price)}원</div>
							</div>
						</div>
					{/each}
					{#if filteredTransactions.length === 0}
						<div class="transaction-item" style="justify-content: center;">
							<span class="text-secondary text-sm">거래 내역이 없습니다</span>
						</div>
					{/if}
				</div>
			</div>
		</div>
	{/if}
</div>
