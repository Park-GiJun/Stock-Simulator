<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { ArrowLeft, TrendingUp, TrendingDown } from 'lucide-svelte';
	import { Button, Card, StockChart } from '$lib/components';
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
	<div class="text-center p-2xl text-secondary">로딩 중...</div>
{:else if stock}
	<div class="stock-detail">
		<!-- Header -->
		<div class="flex items-center gap-md mb-lg">
			<a href="/stocks" class="btn btn-ghost btn-icon-only">
				<ArrowLeft size={20} />
			</a>
			<div>
				<h1 class="text-2xl font-bold">{stock.stockName}</h1>
				<div class="flex items-center gap-md text-secondary">
					<span>{stockId}</span>
					<span class="badge badge-sector-{stock.sector.toLowerCase()}">{SECTOR_NAMES[stock.sector]}</span>
					<span>{MARKET_CAP_NAMES[stock.marketCapGrade]}</span>
				</div>
			</div>
		</div>

		<div class="layout-main-aside">
			<!-- Main Content -->
			<div>
				<!-- Price Card -->
				<Card>
					<div class="p-lg">
						<div class="flex items-end gap-md mb-md">
							<span class="text-3xl font-bold">₩{formatPrice(stock.currentPrice)}</span>
							<div
								class="flex items-center gap-xs text-xl"
								class:text-stock-up={change() >= 0}
								class:text-stock-down={change() < 0}
							>
								{#if change() >= 0}
									<TrendingUp size={20} />
								{:else}
									<TrendingDown size={20} />
								{/if}
								{formatPercent(changePercent())}
							</div>
						</div>

						<div class="grid grid-cols-4 gap-md text-sm">
							<div>
								<div class="text-secondary">시가</div>
								<div class="font-medium">₩{formatPrice(stock.open)}</div>
							</div>
							<div>
								<div class="text-secondary">고가</div>
								<div class="font-medium text-stock-up">₩{formatPrice(stock.high)}</div>
							</div>
							<div>
								<div class="text-secondary">저가</div>
								<div class="font-medium text-stock-down">₩{formatPrice(stock.low)}</div>
							</div>
							<div>
								<div class="text-secondary">거래량</div>
								<div class="font-medium">{stock.volume.toLocaleString()}</div>
							</div>
						</div>
					</div>
				</Card>

				<!-- Order Book + Chart Grid -->
			<div class="orderbook-chart-layout">
				<!-- Order Book -->
				<Card>
					{#snippet header()}
						<h3 class="card-title">호가창</h3>
					{/snippet}

					<div class="table-wrapper border-none">
						{#if orderBook}
							<table class="table table-orderbook">
								<thead>
									<tr>
										<th class="text-right">매도잔량</th>
										<th class="text-center">가격</th>
										<th class="text-left">매수잔량</th>
									</tr>
								</thead>
								<tbody>
									{#each (orderBook.asks ?? []).slice().reverse() as ask}
										<tr class="ask-row">
											<td class="quantity-col">{ask.quantity.toLocaleString()}</td>
											<td class="price-col ask">₩{formatPrice(ask.price)}</td>
											<td></td>
										</tr>
									{/each}
									<tr>
										<td colspan="3" class="text-center p-sm bg-tertiary font-semibold">
											현재가 ₩{formatPrice(stock.currentPrice)}
										</td>
									</tr>
									{#each orderBook.bids ?? [] as bid}
										<tr class="bid-row">
											<td></td>
											<td class="price-col bid">₩{formatPrice(bid.price)}</td>
											<td class="quantity-col">{bid.quantity.toLocaleString()}</td>
										</tr>
									{/each}
								</tbody>
							</table>
						{:else}
							<div class="text-center p-md text-secondary">호가 데이터 없음</div>
						{/if}
					</div>
				</Card>

				<!-- Price Chart -->
				<Card>
					{#snippet header()}
						<h3 class="card-title">주가 차트 (30일)</h3>
					{/snippet}

					<div class="p-md">
						{#if candleData.length > 0}
							<StockChart data={candleData} width={400} height={300} />
						{:else}
							<div class="text-center p-md text-secondary">차트 데이터 없음</div>
						{/if}
					</div>
				</Card>
			</div>
		</div>

			<!-- Order Panel -->
			<aside>
				<Card>
					{#snippet header()}
						<h3 class="card-title">주문</h3>
					{/snippet}

					<div class="p-md">
						<!-- Buy/Sell Toggle -->
						<div class="btn-group w-full mb-lg">
							<button
								class="btn flex-1"
								class:btn-buy={orderSide === 'BUY'}
								class:btn-secondary={orderSide !== 'BUY'}
								onclick={() => orderSide = 'BUY'}
							>
								매수
							</button>
							<button
								class="btn flex-1"
								class:btn-sell={orderSide === 'SELL'}
								class:btn-secondary={orderSide !== 'SELL'}
								onclick={() => orderSide = 'SELL'}
							>
								매도
							</button>
						</div>

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
							<div class="text-xl font-bold">
								₩{formatPrice(stock.currentPrice * orderQuantity)}
							</div>
						</div>

						<Button
							type={orderSide === 'BUY' ? 'buy' : 'sell'}
							fullWidth
							size="lg"
							onclick={handleOrder}
						>
							{orderSide === 'BUY' ? '매수' : '매도'} 주문
						</Button>
					</div>
				</Card>

				<!-- Stock Info -->
				<Card>
					{#snippet header()}
						<h3 class="card-title">종목 정보</h3>
					{/snippet}

					<div class="stack-sm p-sm">
						<div class="flex justify-between">
							<span class="text-secondary">PER</span>
							<span class="font-medium">{stock.per.toFixed(1)}</span>
						</div>
						<div class="flex justify-between">
							<span class="text-secondary">배당률</span>
							<span class="font-medium">{(stock.dividendRate * 100).toFixed(1)}%</span>
						</div>
						<div class="flex justify-between">
							<span class="text-secondary">변동성</span>
							<span class="font-medium">{stock.volatility.toFixed(1)}</span>
						</div>
						<div class="flex justify-between">
							<span class="text-secondary">성장률</span>
							<span class="font-medium">{(stock.growthRate * 100).toFixed(1)}%</span>
						</div>
					</div>
				</Card>
			</aside>
		</div>
	</div>
{:else}
	<div class="text-center p-2xl">
		<h2 class="text-xl font-semibold mb-md">종목을 찾을 수 없습니다</h2>
		<a href="/stocks" class="btn btn-primary">목록으로 돌아가기</a>
	</div>
{/if}
