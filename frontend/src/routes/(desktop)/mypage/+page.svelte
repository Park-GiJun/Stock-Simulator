<script lang="ts">
	import { getMockPortfolio, getMockTransactions, getMockUser } from '$lib/mock/user.js';
	import { currentUser } from '$lib/stores/authStore.js';
	import { DonutChart, MiniSparkline } from '$lib/components';
	import {
		DollarSign,
		CreditCard,
		TrendingUp,
		Activity,
		ArrowUpRight,
		ArrowDownRight
	} from 'lucide-svelte';

	const user = getMockUser();
	const portfolio = getMockPortfolio();
	const transactions = getMockTransactions();

	let activeFilter = $state<'ALL' | 'BUY' | 'SELL'>('ALL');

	const filteredTransactions = $derived(
		activeFilter === 'ALL'
			? transactions
			: transactions.filter((tx) => tx.side === activeFilter)
	);

	const donutColors = ['#8b5cf6', '#06b6d4', '#f59e0b', '#10b981', '#f43f5e'];
	const donutSegments = $derived(
		portfolio.holdings.map((h, i) => ({
			label: h.stockName,
			value: h.totalValue,
			color: donutColors[i % donutColors.length]
		}))
	);

	const performanceData = [2.1, 1.8, 3.5, 2.9, 4.2, 3.8, 5.1];
	const latestReturn = performanceData[performanceData.length - 1];

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	function getHoldingWeight(holdingValue: number): number {
		if (portfolio.totalStockValue === 0) return 0;
		return (holdingValue / portfolio.totalStockValue) * 100;
	}

	function formatDate(dateStr: string): string {
		return new Date(dateStr).toLocaleDateString('ko-KR', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}
</script>

<div class="mypage">
	<h1 class="text-2xl font-bold mb-lg gradient-text">마이페이지</h1>

	<!-- Profile Card -->
	<div class="mypage-profile">
		<div class="mypage-avatar-large">
			{(user.username ?? '?').charAt(0)}
		</div>
		<div class="mypage-profile-info">
			<div class="mypage-profile-name">{user.username}</div>
			<div class="mypage-profile-meta">
				<span>{user.email}</span>
				<span>가입일: {formatDate('2024-01-01')}</span>
			</div>
		</div>
		<div class="mypage-profile-stat">
			<div class="mypage-profile-stat-value">{portfolio.holdings.length}</div>
			<div class="mypage-profile-stat-label">보유 종목</div>
		</div>
		<div class="mypage-profile-stat">
			<div class="mypage-profile-stat-value">{transactions.length}</div>
			<div class="mypage-profile-stat-label">총 거래</div>
		</div>
		<div class="mypage-profile-stat">
			<div class="mypage-profile-stat-value" class:text-stock-up={portfolio.totalProfitLossPercent >= 0} class:text-stock-down={portfolio.totalProfitLossPercent < 0}>
				{formatPercent(portfolio.totalProfitLossPercent)}
			</div>
			<div class="mypage-profile-stat-label">총 수익률</div>
		</div>
	</div>

	<!-- Asset Summary Cards -->
	<div class="stat-cards-grid mb-lg">
		<div class="card card-hover animate-slide-up" style="animation-delay: 0ms">
			<div class="card-stat">
				<div class="card-stat-icon">
					<DollarSign size={16} />
				</div>
				<div class="card-stat-label">총 자산</div>
				<div class="card-stat-value">₩{formatPrice(portfolio.totalAssetValue)}</div>
			</div>
		</div>
		<div class="card card-hover animate-slide-up" style="animation-delay: 50ms">
			<div class="card-stat">
				<div class="card-stat-icon">
					<CreditCard size={16} />
				</div>
				<div class="card-stat-label">보유 현금</div>
				<div class="card-stat-value">₩{formatPrice(portfolio.cashBalance)}</div>
			</div>
		</div>
		<div class="card card-hover animate-slide-up" style="animation-delay: 100ms">
			<div class="card-stat">
				<div class="card-stat-icon">
					<TrendingUp size={16} />
				</div>
				<div class="card-stat-label">주식 평가금</div>
				<div class="card-stat-value">₩{formatPrice(portfolio.totalStockValue)}</div>
			</div>
		</div>
		<div class="card card-hover animate-slide-up" style="animation-delay: 150ms">
			<div class="card-stat">
				<div class="card-stat-icon">
					<Activity size={16} />
				</div>
				<div class="card-stat-label">총 손익</div>
				<div class="card-stat-value" class:text-stock-up={portfolio.totalProfitLoss >= 0} class:text-stock-down={portfolio.totalProfitLoss < 0}>
					{portfolio.totalProfitLoss >= 0 ? '+' : ''}₩{formatPrice(portfolio.totalProfitLoss)}
				</div>
				<div class="card-stat-change" class:up={portfolio.totalProfitLossPercent >= 0} class:down={portfolio.totalProfitLossPercent < 0}>
					{formatPercent(portfolio.totalProfitLossPercent)}
				</div>
			</div>
		</div>
	</div>

	<!-- Portfolio Donut Chart & Performance Mini Chart -->
	<div class="grid grid-cols-2 gap-lg mb-lg">
		<div class="card animate-slide-up" style="animation-delay: 200ms">
			<div class="card-header">
				<h3 class="card-title">포트폴리오 구성</h3>
			</div>
			<div class="card-body">
				<DonutChart segments={donutSegments} size={140} thickness={20} />
			</div>
		</div>

		<div class="card animate-slide-up" style="animation-delay: 250ms">
			<div class="card-header">
				<h3 class="card-title">7일 수익률 추이</h3>
			</div>
			<div class="card-body">
				<div class="performance-chart-container">
					<div class="performance-chart-header">
						<div>
							<div class="performance-chart-value" class:text-stock-up={latestReturn >= 0} class:text-stock-down={latestReturn < 0}>
								{latestReturn >= 0 ? '+' : ''}{latestReturn.toFixed(1)}%
							</div>
							<div class="text-sm text-secondary">최근 7일 수익률</div>
						</div>
						{#if latestReturn >= 0}
							<ArrowUpRight size={24} class="text-stock-up" />
						{:else}
							<ArrowDownRight size={24} class="text-stock-down" />
						{/if}
					</div>
					<MiniSparkline data={performanceData} width={200} height={40} color="auto" />
				</div>
			</div>
		</div>
	</div>

	<div class="grid grid-cols-2 gap-lg">
		<!-- Holdings Table -->
		<div class="card animate-slide-up" style="animation-delay: 300ms">
			<div class="card-header">
				<h3 class="card-title">보유 종목</h3>
			</div>
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
								<td class="text-right">₩{formatPrice(holding.averagePrice)}</td>
								<td class="text-right">₩{formatPrice(holding.currentPrice)}</td>
								<td class="text-right">
									<div class="profit-loss" class:positive={holding.profitLoss >= 0} class:negative={holding.profitLoss < 0}>
										{holding.profitLoss >= 0 ? '+' : ''}₩{formatPrice(holding.profitLoss)}
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
		</div>

		<!-- Recent Transactions -->
		<div class="card animate-slide-up" style="animation-delay: 350ms">
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
							<div class="text-sm mt-xs">{tx.quantity}주 × ₩{formatPrice(tx.price)}</div>
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
</div>
