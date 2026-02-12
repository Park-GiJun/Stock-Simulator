<script lang="ts">
	import { getMockPortfolio, getMockTransactions } from '$lib/mock/user.js';
	import { currentUser } from '$lib/stores/authStore.js';

	const portfolio = getMockPortfolio();
	const transactions = getMockTransactions();

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="mypage">
	<h1 class="text-2xl font-bold mb-lg gradient-text">마이페이지</h1>

	<!-- Asset Summary -->
	<div class="stat-cards-grid mb-lg">
		<div class="card card-hover animate-slide-up" style="animation-delay: 0ms">
			<div class="card-body">
				<div class="card-stat">
					<div class="card-stat-icon">
						<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
					</div>
					<div class="card-stat-label">총 자산</div>
					<div class="card-stat-value">₩{formatPrice(portfolio.totalAssetValue)}</div>
				</div>
			</div>
		</div>
		<div class="card card-hover animate-slide-up" style="animation-delay: 50ms">
			<div class="card-body">
				<div class="card-stat">
					<div class="card-stat-icon">
						<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="6" width="20" height="12" rx="2"/><path d="M22 10H2"/></svg>
					</div>
					<div class="card-stat-label">보유 현금</div>
					<div class="card-stat-value">₩{formatPrice(portfolio.cashBalance)}</div>
				</div>
			</div>
		</div>
		<div class="card card-hover animate-slide-up" style="animation-delay: 100ms">
			<div class="card-body">
				<div class="card-stat">
					<div class="card-stat-icon">
						<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>
					</div>
					<div class="card-stat-label">주식 평가금</div>
					<div class="card-stat-value">₩{formatPrice(portfolio.totalStockValue)}</div>
				</div>
			</div>
		</div>
		<div class="card card-hover animate-slide-up" style="animation-delay: 150ms">
			<div class="card-body">
				<div class="card-stat">
					<div class="card-stat-icon">
						<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
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
	</div>

	<div class="grid grid-cols-2 gap-lg">
		<!-- Holdings -->
		<div class="card animate-slide-up" style="animation-delay: 200ms">
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
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>

		<!-- Recent Transactions -->
		<div class="card animate-slide-up" style="animation-delay: 250ms">
			<div class="card-header">
				<h3 class="card-title">최근 거래 내역</h3>
			</div>
			<div class="card-body p-0">
				{#each transactions as tx}
					<div class="transaction-item">
						<div>
							<div class="font-medium">{tx.stockName}</div>
							<div class="text-xs text-secondary">
								{new Date(tx.timestamp).toLocaleDateString('ko-KR')}
							</div>
						</div>
						<div class="text-right">
							<div class="badge" class:badge-error={tx.side === 'SELL'} class:badge-success={tx.side === 'BUY'}>
								{tx.side === 'BUY' ? '매수' : '매도'}
							</div>
							<div class="text-sm mt-xs">{tx.quantity}주 × ₩{formatPrice(tx.price)}</div>
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>
</div>
