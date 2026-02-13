<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { ArrowLeft, TrendingUp, TrendingDown, Star, Users, Building2 } from 'lucide-svelte';
	import { StockChart } from '$lib/components';
	import { getStock, getOrderBook, getCandles } from '$lib/api/stockApi.js';
	import { getActiveNews } from '$lib/mock/news.js';
	import { SECTOR_NAMES, MARKET_CAP_NAMES, type Stock, type OrderBook as OrderBookType, type Candle } from '$lib/types/stock.js';
	import { EVENT_LEVEL_NAMES, SENTIMENT_NAMES } from '$lib/types/news.js';

	const stockId = $page.params.stockId!;

	let stock = $state<Stock | null>(null);
	let orderBook = $state<OrderBookType | null>(null);
	let candleData = $state<Candle[]>([]);
	let loading = $state(true);

	let orderSide = $state<'BUY' | 'SELL'>('BUY');
	let orderType = $state<'LIMIT' | 'MARKET'>('LIMIT');
	let orderQuantity = $state(1);
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
			const [stockRes, orderBookRes, candlesRes] = await Promise.all([
				getStock(stockId),
				getOrderBook(stockId),
				getCandles(stockId, { limit: 30 })
			]);

			if (stockRes.success && stockRes.data) {
				stock = stockRes.data;
			}
			if (orderBookRes.success && orderBookRes.data) {
				orderBook = orderBookRes.data;
			}
			if (candlesRes.success && candlesRes.data) {
				candleData = candlesRes.data.candles;
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

	function handleOrder() {
		const typeLabel = orderType === 'LIMIT' ? '지정가' : '시장가';
		const sideLabel = orderSide === 'BUY' ? '매수' : '매도';
		alert(`${typeLabel} ${sideLabel} 주문: ${stock?.stockName} ${orderQuantity}주`);
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

				<!-- Investor Trend + Related News -->
				<div class="stock-detail-content-grid">
					<!-- Investor Trend -->
					<div class="stock-detail-section animate-entry-3">
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
					<div class="stock-detail-section animate-entry-4">
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
							<label class="input-label">주문 유형</label>
							<div class="stock-detail-order-type-toggle">
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

						<!-- Quantity -->
						<div class="form-group">
							<label class="input-label">주문 수량</label>
							<div class="input-addon-wrapper">
								<input
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
							<label class="input-label">예상 금액</label>
							<div class="stock-detail-estimated-amount">
								₩{formatPrice(stock.currentPrice * orderQuantity)}
							</div>
						</div>

						<!-- Action Button -->
						{#if orderSide === 'BUY'}
							<button class="stock-detail-btn-buy" onclick={handleOrder}>
								매수 주문
							</button>
						{:else}
							<button class="stock-detail-btn-sell" onclick={handleOrder}>
								매도 주문
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
