<script lang="ts">
	import type { OrderBook, OrderBookEntry } from '$lib/types/stock.js';

	interface Props {
		orderBook: OrderBook | null;
		currentPrice?: number;
		previousClose?: number;
		compact?: boolean;
		maxRows?: number;
		onPriceClick?: (price: number) => void;
	}

	let {
		orderBook,
		currentPrice = 0,
		previousClose = 0,
		compact = false,
		maxRows = 10,
		onPriceClick
	}: Props = $props();

	// Calculate max quantity for bar width
	const maxQuantity = $derived(() => {
		if (!orderBook) return 1;
		const allQuantities = [...orderBook.asks, ...orderBook.bids].map((o) => o.quantity);
		return Math.max(...allQuantities, 1);
	});

	// Calculate spread
	const spread = $derived(() => {
		if (!orderBook || orderBook.asks.length === 0 || orderBook.bids.length === 0) return null;
		const lowestAsk = orderBook.asks[orderBook.asks.length - 1]?.price ?? 0;
		const highestBid = orderBook.bids[0]?.price ?? 0;
		return {
			amount: lowestAsk - highestBid,
			percent: ((lowestAsk - highestBid) / lowestAsk) * 100
		};
	});

	// Price direction
	const priceDirection = $derived(() => {
		if (currentPrice > previousClose) return 'up';
		if (currentPrice < previousClose) return 'down';
		return 'unchanged';
	});

	// Price change
	const priceChange = $derived(() => {
		const change = currentPrice - previousClose;
		const percent = previousClose > 0 ? (change / previousClose) * 100 : 0;
		return { change, percent };
	});

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatQuantity(quantity: number): string {
		if (quantity >= 1000000) {
			return (quantity / 1000000).toFixed(1) + 'M';
		}
		if (quantity >= 1000) {
			return (quantity / 1000).toFixed(1) + 'K';
		}
		return quantity.toLocaleString();
	}

	function getBarWidth(quantity: number): string {
		return `${(quantity / maxQuantity()) * 100}%`;
	}

	function handlePriceClick(price: number) {
		onPriceClick?.(price);
	}
</script>

<div class="orderbook" class:compact>
	<div class="orderbook-header">
		<span class="orderbook-title">호가창</span>
		{#if spread()}
			<span class="orderbook-spread">
				스프레드:
				<span class="orderbook-spread-value">
					{formatPrice(spread()!.amount)} ({spread()!.percent.toFixed(2)}%)
				</span>
			</span>
		{/if}
	</div>

	<div class="orderbook-columns">
		<span class="orderbook-col">가격</span>
		<span class="orderbook-col">수량</span>
		<span class="orderbook-col">총액</span>
	</div>

	<div class="orderbook-body">
		<!-- Asks (Sell orders) - displayed in reverse -->
		<div class="orderbook-asks">
			{#if orderBook}
				{#each orderBook.asks.slice(0, maxRows).reverse() as ask (ask.price)}
					<button
						type="button"
						class="orderbook-row ask"
						style="--bar-width: {getBarWidth(ask.quantity)}"
						onclick={() => handlePriceClick(ask.price)}
					>
						<span class="orderbook-cell orderbook-price ask">
							{formatPrice(ask.price)}
						</span>
						<span class="orderbook-cell orderbook-quantity">
							{formatQuantity(ask.quantity)}
						</span>
						<span class="orderbook-cell orderbook-total">
							{formatQuantity(ask.price * ask.quantity)}
						</span>
					</button>
				{/each}
			{/if}
		</div>

		<!-- Current Price -->
		<div class="orderbook-center">
			<span class="orderbook-center-price {priceDirection()}">
				₩{formatPrice(currentPrice)}
			</span>
			<span class="orderbook-center-change" class:text-stock-up={priceChange().change >= 0} class:text-stock-down={priceChange().change < 0}>
				{priceChange().change >= 0 ? '+' : ''}{formatPrice(priceChange().change)}
				({priceChange().percent >= 0 ? '+' : ''}{priceChange().percent.toFixed(2)}%)
			</span>
		</div>

		<!-- Bids (Buy orders) -->
		<div class="orderbook-bids">
			{#if orderBook}
				{#each orderBook.bids.slice(0, maxRows) as bid (bid.price)}
					<button
						type="button"
						class="orderbook-row bid"
						style="--bar-width: {getBarWidth(bid.quantity)}"
						onclick={() => handlePriceClick(bid.price)}
					>
						<span class="orderbook-cell orderbook-price bid">
							{formatPrice(bid.price)}
						</span>
						<span class="orderbook-cell orderbook-quantity">
							{formatQuantity(bid.quantity)}
						</span>
						<span class="orderbook-cell orderbook-total">
							{formatQuantity(bid.price * bid.quantity)}
						</span>
					</button>
				{/each}
			{/if}
		</div>
	</div>
</div>
