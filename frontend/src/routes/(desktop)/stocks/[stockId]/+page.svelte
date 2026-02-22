<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { ArrowLeft, TrendingUp, TrendingDown, Star, Users, Building2, ArrowUpRight, ArrowDownRight } from 'lucide-svelte';
	import { get } from 'svelte/store';
	import { StockChart } from '$lib/components';
	import { getStock, getOrderBook, getCandles } from '$lib/api/stockApi.js';
	import { createOrder, getStockTrades, getStockOrders, type BackendTradeResponse, type BackendOrderResponse } from '$lib/api/tradingApi.js';
	import { authStore } from '$lib/stores/authStore.js';
	import { toastStore } from '$lib/stores/toastStore.js';
	import { getActiveNews } from '$lib/mock/news.js';
	import { SECTOR_NAMES, MARKET_CAP_NAMES, type Stock, type OrderBook as OrderBookType, type Candle } from '$lib/types/stock.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES } from '$lib/types/news.js';

	const stockId = $page.params.stockId!;

	let stock = $state<Stock | null>(null);
	let orderBook = $state<OrderBookType | null>(null);
	let candleData = $state<Candle[]>([]);
	let recentTrades = $state<BackendTradeResponse[]>([]);
	let recentOrders = $state<BackendOrderResponse[]>([]);
	let loading = $state(true);

	let orderSide = $state<'BUY' | 'SELL'>('BUY');
	let orderType = $state<'LIMIT' | 'MARKET'>('LIMIT');
	let orderPrice = $state(0);
	let orderQuantity = $state(1);
	let orderSubmitting = $state(false);
	let selectedInterval = $state<string>('1d');

	const intervals = [
		{ label: '1분', value: '1m' },
		{ label: '5분', value: '5m' },
		{ label: '15분', value: '15m' },
		{ label: '1시간', value: '1h' },
		{ label: '1일', value: '1d' },
		{ label: '1주', value: '1w' }
	];

	const quickQuantities = [10, 50, 100];

	const change = $derived(() => stock ? stock.currentPrice - stock.previousClose : 0);
	const changePercent = $derived(() => stock ? ((stock.currentPrice - stock.previousClose) / stock.previousClose) * 100 : 0);

	// Mock 52-week range
	const week52High = $derived(() => stock ? Math.round(stock.high * 1.3) : 0);
	const week52Low = $derived(() => stock ? Math.round(stock.low * 0.7) : 0);
	const pricePosition = $derived(() => {
		if (!stock) return 50;
		const range = week52High() - week52Low();
		if (range === 0) return 50;
		return ((stock.currentPrice - week52Low()) / range) * 100;
	});

	// Order book max quantity for bars
	const maxObQuantity = $derived(() => {
		if (!orderBook) return 1;
		const allQuantities = [
			...(orderBook.asks ?? []).map(a => a.quantity),
			...(orderBook.bids ?? []).map(b => b.quantity)
		];
		return Math.max(...allQuantities, 1);
	});

	// Related news
	const relatedNews = getActiveNews()
		.filter(n => n.targetStockName || n.targetSector)
		.slice(0, 3);

	// Mock investor trend
	const individualBuyRatio = 62;
	const institutionBuyRatio = 45;

	onMount(async () => {
		try {
			const [stockRes, orderBookRes, candlesRes, tradesRes, ordersRes] = await Promise.all([
				getStock(stockId),
				getOrderBook(stockId),
				getCandles(stockId, { limit: 30 }),
				getStockTrades(stockId).catch(() => null),
				getStockOrders(stockId).catch(() => null)
			]);

			if (stockRes.success && stockRes.data) {
				stock = stockRes.data;
				orderPrice = stockRes.data.currentPrice;
			}
			if (orderBookRes.success && orderBookRes.data) {
				orderBook = orderBookRes.data;
			}
			if (candlesRes.success && candlesRes.data) {
				candleData = candlesRes.data.candles;
			}
			if (tradesRes?.success && tradesRes.data) {
				recentTrades = tradesRes.data.slice(0, 50);
			}
			if (ordersRes?.success && ordersRes.data) {
				recentOrders = ordersRes.data.slice(0, 100);
			}
		} catch (e) {
			console.error('Failed to fetch stock detail:', e);
		} finally {
			loading = false;
		}
	});

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	async function handleOrder() {
		if (!stock) return;

		const auth = get(authStore);
		if (!auth.isAuthenticated || !auth.user) {
			toastStore.error('로그인이 필요합니다.');
			return;
		}

		if (orderQuantity <= 0) {
			toastStore.error('주문 수량은 1주 이상이어야 합니다.');
			return;
		}

		if (orderType === 'LIMIT' && orderPrice <= 0) {
			toastStore.error('지정가 주문에는 가격을 입력해야 합니다.');
			return;
		}

		orderSubmitting = true;
		try {
			const result = await createOrder({
				userId: String(auth.user.userId),
				stockId,
				orderType: orderSide,
				orderKind: orderType,
				price: orderType === 'LIMIT' ? orderPrice : null,
				quantity: orderQuantity
			});

			if (result.success && result.data) {
				const sideLabel = orderSide === 'BUY' ? '매수' : '매도';
				const statusLabel = result.data.status === 'FILLED' ? '체결 완료'
					: result.data.status === 'PARTIALLY_FILLED' ? '부분 체결'
					: result.data.status === 'REJECTED' ? '거부됨'
					: '접수 완료';
				toastStore.success(`${sideLabel} 주문 ${statusLabel}: ${stock.stockName} ${orderQuantity}주`);

				// 호가창 새로고침
				const orderBookRes = await getOrderBook(stockId);
				if (orderBookRes.success && orderBookRes.data) {
					orderBook = orderBookRes.data;
				}
			}
		} catch {
			// api.ts에서 이미 toast 처리됨
		} finally {
			orderSubmitting = false;
		}
	}

	function getTimeAgo(dateStr: string): string {
		const date = new Date(dateStr);
		const now = new Date();
		const diffMs = now.getTime() - date.getTime();
		const diffMins = Math.floor(diffMs / 60000);
		if (diffMins < 1) return '방금 전';
		if (diffMins < 60) return `${diffMins}분 전`;
		if (diffMins < 1440) return `${Math.floor(diffMins / 60)}시간 전`;
		return `${Math.floor(diffMins / 1440)}일 전`;
	}
</script>

{#if loading}
	<div class="stock-detail-loading">
		<div class="stocks-loading-spinner"></div>
		<div class="text-secondary text-sm">로딩 중...</div>
	</div>
{:else if stock}
	<div class="stock-detail">
		<!-- Header -->
		<div class="stock-detail-header">
			<a href="/stocks" class="btn btn-ghost btn-icon-only btn-rounded">
				<ArrowLeft size={20} />
			</a>
			<div>
				<h1>{stock.stockName}</h1>
				<div class="stock-detail-meta">
					<span>{stockId}</span>
					<span class="badge badge-sector-{stock.sector.toLowerCase()}">{SECTOR_NAMES[stock.sector]}</span>
					<span>{MARKET_CAP_NAMES[stock.marketCapGrade]}</span>
				</div>
			</div>
		</div>

		<!-- Price Panel -->
		<div class="stock-detail-price-panel">
			<div class="stock-detail-price-main">
				<span
					class="stock-detail-current-price"
					class:up={change() >= 0}
					class:down={change() < 0}
				>
					₩{formatPrice(stock.currentPrice)}
				</span>
				<div
					class="stock-detail-change"
					class:up={change() >= 0}
					class:down={change() < 0}
				>
					{#if change() >= 0}
						<TrendingUp size={20} />
					{:else}
						<TrendingDown size={20} />
					{/if}
					{formatPercent(changePercent())}
				</div>
			</div>

			<div class="stock-detail-stats">
				<div class="stock-detail-stat">
					<span class="stock-detail-stat-label">시가</span>
					<span class="stock-detail-stat-value">₩{formatPrice(stock.open)}</span>
				</div>
				<div class="stock-detail-stat">
					<span class="stock-detail-stat-label">고가</span>
					<span class="stock-detail-stat-value text-stock-up">₩{formatPrice(stock.high)}</span>
				</div>
				<div class="stock-detail-stat">
					<span class="stock-detail-stat-label">저가</span>
					<span class="stock-detail-stat-value text-stock-down">₩{formatPrice(stock.low)}</span>
				</div>
				<div class="stock-detail-stat">
					<span class="stock-detail-stat-label">거래량</span>
					<span class="stock-detail-stat-value">{stock.volume.toLocaleString()}</span>
				</div>
			</div>

			<!-- 52-Week Range Bar -->
			<div class="stock-detail-range">
				<div class="stock-detail-range-labels">
					<span class="text-xs text-secondary">52주 최저 ₩{formatPrice(week52Low())}</span>
					<span class="text-xs text-secondary">52주 최고 ₩{formatPrice(week52High())}</span>
				</div>
				<div class="stock-detail-range-bar">
					<div class="stock-detail-range-fill" style="width: {pricePosition()}%"></div>
					<div class="stock-detail-range-marker" style="left: {pricePosition()}%"></div>
				</div>
			</div>
		</div>

		<div class="layout-main-aside">
			<!-- Main Content -->
			<div class="flex flex-col gap-lg">
				<!-- Order Book + Chart Grid -->
				<div class="stock-detail-content-grid">
					<!-- Order Book -->
					<div class="stock-detail-section animate-entry-1">
						<div class="stock-detail-section-header">
							<h3 class="stock-detail-section-title">호가창</h3>
						</div>

						{#if orderBook}
							<div class="stock-detail-orderbook">
								<div class="stock-detail-orderbook-header">
									<span>매도잔량</span>
									<span>가격</span>
									<span>매수잔량</span>
								</div>
								<div class="stock-detail-orderbook-body">
									{#each (orderBook.asks ?? []).slice().reverse() as ask}
										<div class="stock-detail-ob-row ask-row">
											<div class="stock-detail-ob-cell quantity left">
												<div class="stock-detail-ob-bar ask" style="width: {(ask.quantity / maxObQuantity()) * 100}%"></div>
												<span>{ask.quantity.toLocaleString()}</span>
											</div>
											<div class="stock-detail-ob-cell price ask">₩{formatPrice(ask.price)}</div>
											<div class="stock-detail-ob-cell quantity right"></div>
										</div>
									{/each}
									<div class="stock-detail-ob-center">
										현재가 ₩{formatPrice(stock.currentPrice)}
									</div>
									{#each orderBook.bids ?? [] as bid}
										<div class="stock-detail-ob-row bid-row">
											<div class="stock-detail-ob-cell quantity left"></div>
											<div class="stock-detail-ob-cell price bid">₩{formatPrice(bid.price)}</div>
											<div class="stock-detail-ob-cell quantity right">
												<div class="stock-detail-ob-bar bid" style="width: {(bid.quantity / maxObQuantity()) * 100}%"></div>
												<span>{bid.quantity.toLocaleString()}</span>
											</div>
										</div>
									{/each}
								</div>
								<div class="stock-detail-ob-totals">
									<span class="text-stock-down">총 매도 {(orderBook.asks ?? []).reduce((s, a) => s + a.quantity, 0).toLocaleString()}</span>
									<span class="text-stock-up">총 매수 {(orderBook.bids ?? []).reduce((s, b) => s + b.quantity, 0).toLocaleString()}</span>
								</div>
							</div>
						{:else}
							<div class="stock-detail-empty">호가 데이터 없음</div>
						{/if}
					</div>

					<!-- Price Chart -->
					<div class="stock-detail-section animate-entry-2">
						<div class="stock-detail-section-header">
							<h3 class="stock-detail-section-title">주가 차트</h3>
							<div class="stock-detail-interval-tabs">
								{#each intervals as interval}
									<button
										class="stock-detail-interval-tab"
										class:active={selectedInterval === interval.value}
										onclick={() => selectedInterval = interval.value}
									>
										{interval.label}
									</button>
								{/each}
							</div>
						</div>
						<div class="stock-detail-chart-container">
							{#if candleData.length > 0}
								<StockChart data={candleData} width={500} height={350} />
							{:else}
								<div class="stock-detail-empty">차트 데이터 없음</div>
							{/if}
						</div>
					</div>
				</div>

				<!-- Recent Trades -->
				<div class="stock-detail-section animate-entry-3">
					<div class="stock-detail-section-header">
						<h3 class="stock-detail-section-title">체결 내역</h3>
						<span class="text-xs text-secondary">{recentTrades.length}건</span>
					</div>
					{#if recentTrades.length > 0}
						<div class="table-wrapper border-none" style="border: none; box-shadow: none; border-radius: 0; background: transparent; backdrop-filter: none; max-height: 400px; overflow-y: auto;">
							<table class="table">
								<thead>
									<tr>
										<th>시각</th>
										<th class="text-right">체결가</th>
										<th class="text-right">수량</th>
										<th class="text-right">체결금액</th>
										<th>매수자</th>
										<th>매도자</th>
									</tr>
								</thead>
								<tbody>
									{#each recentTrades as trade}
										<tr>
											<td class="text-xs text-secondary">
												{new Date(trade.tradedAt).toLocaleString('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })}
											</td>
											<td class="text-right font-medium">₩{formatPrice(trade.price)}</td>
											<td class="text-right">{trade.quantity.toLocaleString()}</td>
											<td class="text-right">{formatPrice(trade.tradeAmount)}원</td>
											<td>
												<span class="badge badge-success text-xs">{trade.buyerType === 'USER' ? '개인' : trade.buyerType === 'NPC' ? 'NPC' : trade.buyerType === 'INSTITUTION' ? '기관' : 'MM'}</span>
											</td>
											<td>
												<span class="badge badge-error text-xs">{trade.sellerType === 'USER' ? '개인' : trade.sellerType === 'NPC' ? 'NPC' : trade.sellerType === 'INSTITUTION' ? '기관' : 'MM'}</span>
											</td>
										</tr>
									{/each}
								</tbody>
							</table>
						</div>
					{:else}
						<div class="stock-detail-empty">체결 내역이 없습니다</div>
					{/if}
				</div>

				<!-- Order History (REJECTED 포함) -->
				<div class="stock-detail-section animate-entry-4">
					<div class="stock-detail-section-header">
						<h3 class="stock-detail-section-title">주문 내역</h3>
						<span class="text-xs text-secondary">{recentOrders.length}건</span>
					</div>
					{#if recentOrders.length > 0}
						<div class="table-wrapper border-none" style="border: none; box-shadow: none; border-radius: 0; background: transparent; backdrop-filter: none; max-height: 400px; overflow-y: auto;">
							<table class="table">
								<thead>
									<tr>
										<th>시각</th>
										<th>구분</th>
										<th>유형</th>
										<th class="text-right">가격</th>
										<th class="text-right">수량</th>
										<th class="text-right">체결</th>
										<th>상태</th>
										<th>투자자</th>
									</tr>
								</thead>
								<tbody>
									{#each recentOrders as order}
										<tr>
											<td class="text-xs text-secondary">
												{new Date(order.createdAt).toLocaleString('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })}
											</td>
											<td>
												<span class="badge text-xs" class:badge-success={order.orderType === 'BUY'} class:badge-error={order.orderType === 'SELL'}>
													{order.orderType === 'BUY' ? '매수' : '매도'}
												</span>
											</td>
											<td class="text-xs">{order.orderKind === 'MARKET' ? '시장가' : '지정가'}</td>
											<td class="text-right font-medium">
												{order.price ? `₩${formatPrice(order.price)}` : '-'}
											</td>
											<td class="text-right">{order.quantity.toLocaleString()}</td>
											<td class="text-right">{order.filledQuantity.toLocaleString()}</td>
											<td>
												{#if order.status === 'FILLED'}
													<span class="badge badge-success text-xs">체결</span>
												{:else if order.status === 'PARTIALLY_FILLED'}
													<span class="badge badge-info text-xs">부분체결</span>
												{:else if order.status === 'REJECTED'}
													<span class="badge badge-error text-xs">거부</span>
												{:else if order.status === 'CANCELLED'}
													<span class="badge text-xs" style="background: var(--color-text-tertiary); color: var(--color-bg-primary);">취소</span>
												{:else}
													<span class="badge text-xs">대기</span>
												{/if}
											</td>
											<td class="text-xs">
												{order.investorType === 'USER' ? '개인' : order.investorType === 'NPC' ? 'NPC' : order.investorType === 'INSTITUTION' ? '기관' : 'MM'}
											</td>
										</tr>
									{/each}
								</tbody>
							</table>
						</div>
					{:else}
						<div class="stock-detail-empty">주문 내역이 없습니다</div>
					{/if}
				</div>

				<!-- Investor Trend + Related News -->
				<div class="stock-detail-content-grid">
					<!-- Investor Trend -->
					<div class="stock-detail-section animate-entry-5">
						<div class="stock-detail-section-header">
							<h3 class="stock-detail-section-title">투자자 동향</h3>
						</div>
						<div class="stock-detail-investor-trend">
							<div class="investor-trend-row">
								<div class="investor-trend-label">
									<Users size={16} />
									<span>개인 투자자</span>
								</div>
								<div class="investor-trend-bar-wrapper">
									<div class="investor-trend-bar">
										<div class="investor-trend-buy" style="width: {individualBuyRatio}%"></div>
									</div>
									<div class="investor-trend-values">
										<span class="text-stock-up text-xs font-medium">매수 {individualBuyRatio}%</span>
										<span class="text-stock-down text-xs font-medium">매도 {100 - individualBuyRatio}%</span>
									</div>
								</div>
							</div>
							<div class="investor-trend-row">
								<div class="investor-trend-label">
									<Building2 size={16} />
									<span>기관 투자자</span>
								</div>
								<div class="investor-trend-bar-wrapper">
									<div class="investor-trend-bar">
										<div class="investor-trend-buy" style="width: {institutionBuyRatio}%"></div>
									</div>
									<div class="investor-trend-values">
										<span class="text-stock-up text-xs font-medium">매수 {institutionBuyRatio}%</span>
										<span class="text-stock-down text-xs font-medium">매도 {100 - institutionBuyRatio}%</span>
									</div>
								</div>
							</div>
						</div>
					</div>

					<!-- Related News -->
					<div class="stock-detail-section animate-entry-5">
						<div class="stock-detail-section-header">
							<h3 class="stock-detail-section-title">관련 뉴스</h3>
						</div>
						<div class="stock-detail-related-news">
							{#each relatedNews as news}
								<div class="stock-detail-news-item">
									<div class="flex items-center gap-sm mb-xs">
										<span class="news-sentiment-dot" class:positive={news.sentiment === 'POSITIVE'} class:negative={news.sentiment === 'NEGATIVE'} class:neutral={news.sentiment === 'NEUTRAL'}></span>
										<span class="badge badge-{news.level === 'SOCIETY' ? 'error' : news.level === 'INDUSTRY' ? 'info' : 'success'} text-xs">
											{EVENT_LEVEL_NAMES[news.level]}
										</span>
									</div>
									<div class="font-medium text-sm line-clamp-1">{news.headline}</div>
									<div class="text-xs text-secondary mt-xs">{getTimeAgo(news.createdAt)}</div>
								</div>
							{:else}
								<div class="stock-detail-empty">관련 뉴스 없음</div>
							{/each}
						</div>
					</div>
				</div>
			</div>

			<!-- Order Panel (Aside) -->
			<aside>
				<!-- Trade Panel -->
				<div class="stock-detail-trade-panel animate-entry-3">
					<div class="stock-detail-section-header">
						<h3 class="stock-detail-section-title">주문</h3>
					</div>

					<!-- Buy/Sell Tab -->
					<div class="stock-detail-trade-tabs">
						<button
							class="stock-detail-trade-tab"
							class:active-buy={orderSide === 'BUY'}
							onclick={() => orderSide = 'BUY'}
						>
							매수
						</button>
						<button
							class="stock-detail-trade-tab"
							class:active-sell={orderSide === 'SELL'}
							onclick={() => orderSide = 'SELL'}
						>
							매도
						</button>
					</div>

					<div class="stock-detail-trade-body">
						<!-- Order Type -->
						<div class="form-group">
							<span class="input-label" id="order-type-label">주문 유형</span>
							<div class="stock-detail-order-type-toggle" role="radiogroup" aria-labelledby="order-type-label">
								<button
									class="stock-detail-order-type-btn"
									class:active={orderType === 'LIMIT'}
									onclick={() => orderType = 'LIMIT'}
								>
									지정가
								</button>
								<button
									class="stock-detail-order-type-btn"
									class:active={orderType === 'MARKET'}
									onclick={() => orderType = 'MARKET'}
								>
									시장가
								</button>
							</div>
						</div>

						<!-- Price (LIMIT only) -->
						{#if orderType === 'LIMIT'}
							<div class="form-group">
								<label class="input-label" for="order-price">주문 가격</label>
								<div class="input-addon-wrapper">
									<input
										id="order-price"
										type="number"
										class="input input-number"
										min="1"
										bind:value={orderPrice}
									/>
									<span class="input-addon">원</span>
								</div>
							</div>
						{/if}

						<!-- Quantity -->
						<div class="form-group">
							<label class="input-label" for="order-quantity">주문 수량</label>
							<div class="input-addon-wrapper">
								<input
									id="order-quantity"
									type="number"
									class="input input-number"
									min="1"
									bind:value={orderQuantity}
								/>
								<span class="input-addon">주</span>
							</div>
							<div class="stock-detail-quick-qty">
								{#each quickQuantities as qty}
									<button
										class="stock-detail-quick-qty-btn"
										onclick={() => orderQuantity = qty}
									>
										{qty}주
									</button>
								{/each}
							</div>
						</div>

						<!-- Estimated Amount -->
						<div class="form-group">
							<span class="input-label" id="estimated-amount-label">예상 금액</span>
							<div class="stock-detail-estimated-amount" aria-labelledby="estimated-amount-label">
								{#if orderType === 'MARKET'}
									≈ ₩{formatPrice(stock.currentPrice * orderQuantity)}
								{:else}
									₩{formatPrice(orderPrice * orderQuantity)}
								{/if}
							</div>
						</div>

						<!-- Action Button -->
						{#if orderSide === 'BUY'}
							<button class="stock-detail-btn-buy" onclick={handleOrder} disabled={orderSubmitting}>
								{orderSubmitting ? '주문 처리 중...' : '매수 주문'}
							</button>
						{:else}
							<button class="stock-detail-btn-sell" onclick={handleOrder} disabled={orderSubmitting}>
								{orderSubmitting ? '주문 처리 중...' : '매도 주문'}
							</button>
						{/if}
					</div>
				</div>

				<!-- Stock Info Panel -->
				<div class="stock-detail-section animate-entry-4">
					<div class="stock-detail-section-header">
						<h3 class="stock-detail-section-title">종목 정보</h3>
					</div>
					<div>
						<div class="stock-detail-info-row">
							<span class="stock-detail-info-label">PER</span>
							<span class="stock-detail-info-value">{stock.per.toFixed(1)}</span>
						</div>
						<div class="stock-detail-info-row">
							<span class="stock-detail-info-label">배당률</span>
							<span class="stock-detail-info-value">{(stock.dividendRate * 100).toFixed(1)}%</span>
						</div>
						<div class="stock-detail-info-row">
							<span class="stock-detail-info-label">변동성</span>
							<span class="stock-detail-info-value">{stock.volatility.toFixed(1)}</span>
						</div>
						<div class="stock-detail-info-row">
							<span class="stock-detail-info-label">성장률</span>
							<span class="stock-detail-info-value">{(stock.growthRate * 100).toFixed(1)}%</span>
						</div>
					</div>
				</div>
			</aside>
		</div>
	</div>
{:else}
	<div class="stock-detail-not-found">
		<h2>종목을 찾을 수 없습니다</h2>
		<a href="/stocks" class="btn btn-primary">목록으로 돌아가기</a>
	</div>
{/if}
