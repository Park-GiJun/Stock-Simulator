<script lang="ts">
	import { Card } from '$lib/components';
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
	<h1 class="text-2xl font-bold mb-lg">마이페이지</h1>

	<!-- Asset Summary -->
	<div class="grid grid-cols-4 gap-md mb-lg">
		<Card>
			<div class="card-stat">
				<div class="card-stat-label">총 자산</div>
				<div class="card-stat-value">₩{formatPrice(portfolio.totalAssetValue)}</div>
			</div>
		</Card>
		<Card>
			<div class="card-stat">
				<div class="card-stat-label">보유 현금</div>
				<div class="card-stat-value">₩{formatPrice(portfolio.cashBalance)}</div>
			</div>
		</Card>
		<Card>
			<div class="card-stat">
				<div class="card-stat-label">주식 평가금</div>
				<div class="card-stat-value">₩{formatPrice(portfolio.totalStockValue)}</div>
			</div>
		</Card>
		<Card>
			<div class="card-stat">
				<div class="card-stat-label">총 손익</div>
				<div class="card-stat-value" class:text-stock-up={portfolio.totalProfitLoss >= 0} class:text-stock-down={portfolio.totalProfitLoss < 0}>
					{portfolio.totalProfitLoss >= 0 ? '+' : ''}₩{formatPrice(portfolio.totalProfitLoss)}
				</div>
				<div class="card-stat-change" class:up={portfolio.totalProfitLossPercent >= 0} class:down={portfolio.totalProfitLossPercent < 0}>
					{formatPercent(portfolio.totalProfitLossPercent)}
				</div>
			</div>
		</Card>
	</div>

	<div class="grid grid-cols-2 gap-lg">
		<!-- Holdings -->
		<Card>
			{#snippet header()}
				<h3 class="card-title">보유 종목</h3>
			{/snippet}

			<div class="table-wrapper border-none">
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
		</Card>

		<!-- Recent Transactions -->
		<Card>
			{#snippet header()}
				<h3 class="card-title">최근 거래 내역</h3>
			{/snippet}

			<div class="stack-sm">
				{#each transactions as tx}
					<div class="flex justify-between items-center p-sm border-b">
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
							<div class="text-sm">{tx.quantity}주 × ₩{formatPrice(tx.price)}</div>
						</div>
					</div>
				{/each}
			</div>
		</Card>
	</div>
</div>
