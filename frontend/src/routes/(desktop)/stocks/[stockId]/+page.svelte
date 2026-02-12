<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { ArrowLeft, TrendingUp, TrendingDown } from 'lucide-svelte';
	import { StockChart } from '$lib/components';
	import { getStock, getOrderBook, getCandles } from '$lib/api/stockApi.js';
	import { SECTOR_NAMES, MARKET_CAP_NAMES, type Stock, type OrderBook as OrderBookType, type Candle } from '$lib/types/stock.js';

	const stockId = $page.params.stockId!;

	let stock = $state<Stock | null>(null);
	let orderBook = $state<OrderBookType | null>(null);
	let candleData = $state<Candle[]>([]);
	let loading = $state(true);

	let orderSide = $state<'BUY' | 'SELL'>('BUY');
	let orderQuantity = $state(1);

	const change = $derived(() => stock ? stock.currentPrice - stock.previousClose : 0);
	const changePercent = $derived(() => stock ? ((stock.currentPrice - stock.previousClose) / stock.previousClose) * 100 : 0);

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
		alert(`${orderSide === 'BUY' ? '매수' : '매도'} 주문: ${stock?.stockName} ${orderQuantity}주`);
	}
</script>

{#if loading}
	<div class="stock-detail-loading">로딩 중...</div>
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
											<div class="stock-detail-ob-cell quantity left">{ask.quantity.toLocaleString()}</div>
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
											<div class="stock-detail-ob-cell quantity right">{bid.quantity.toLocaleString()}</div>
										</div>
									{/each}
								</div>
							</div>
						{:else}
							<div class="stock-detail-empty">호가 데이터 없음</div>
						{/if}
					</div>

					<!-- Price Chart -->
					<div class="stock-detail-section animate-entry-2">
						<div class="stock-detail-section-header">
							<h3 class="stock-detail-section-title">주가 차트 (30일)</h3>
						</div>
						<div class="stock-detail-chart-container">
							{#if candleData.length > 0}
								<StockChart data={candleData} width={400} height={300} />
							{:else}
								<div class="stock-detail-empty">차트 데이터 없음</div>
							{/if}
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

					<!-- Buy/Sell Tab Navigation - Glass Pill Style -->
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
						<!-- Order Form -->
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
						</div>

						<div class="form-group">
							<label class="input-label">예상 금액</label>
							<div class="stock-detail-estimated-amount">
								₩{formatPrice(stock.currentPrice * orderQuantity)}
							</div>
						</div>

						<!-- Glow Action Button -->
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
