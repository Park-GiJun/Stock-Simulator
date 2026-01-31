<script lang="ts">
	import { Minus, Plus, TrendingUp, TrendingDown } from 'lucide-svelte';
	import type { Stock } from '$lib/types/stock.js';
	import { toastStore } from '$lib/stores/toastStore.js';

	interface Props {
		stock: Stock | null;
		availableCash?: number;
		holdingQuantity?: number;
		compact?: boolean;
		onSubmit?: (order: OrderData) => void;
	}

	interface OrderData {
		stockId: string;
		side: 'BUY' | 'SELL';
		orderType: 'LIMIT' | 'MARKET';
		price: number;
		quantity: number;
	}

	let {
		stock,
		availableCash = 0,
		holdingQuantity = 0,
		compact = false,
		onSubmit
	}: Props = $props();

	// State
	let side = $state<'BUY' | 'SELL'>('BUY');
	let orderType = $state<'LIMIT' | 'MARKET'>('LIMIT');
	let price = $state(0);
	let quantity = $state(0);
	let error = $state<string | null>(null);

	// Initialize price when stock changes
	$effect(() => {
		if (stock) {
			price = stock.currentPrice;
		}
	});

	// Calculations
	const totalAmount = $derived(price * quantity);
	const commission = $derived(Math.floor(totalAmount * 0.00015)); // 0.015% 수수료
	const finalAmount = $derived(side === 'BUY' ? totalAmount + commission : totalAmount - commission);

	// Max quantity calculation
	const maxBuyQuantity = $derived(() => {
		if (!stock || price <= 0) return 0;
		return Math.floor(availableCash / price);
	});

	const maxSellQuantity = $derived(() => {
		return holdingQuantity;
	});

	// Price direction
	const priceDirection = $derived(() => {
		if (!stock) return 'unchanged';
		if (stock.currentPrice > stock.previousClose) return 'up';
		if (stock.currentPrice < stock.previousClose) return 'down';
		return 'unchanged';
	});

	const priceChange = $derived(() => {
		if (!stock) return { change: 0, percent: 0 };
		const change = stock.currentPrice - stock.previousClose;
		const percent = stock.previousClose > 0 ? (change / stock.previousClose) * 100 : 0;
		return { change, percent };
	});

	// Functions
	function adjustPrice(delta: number) {
		const tickSize = getTickSize(price);
		price = Math.max(0, price + delta * tickSize);
	}

	function getTickSize(currentPrice: number): number {
		if (currentPrice < 1000) return 1;
		if (currentPrice < 5000) return 5;
		if (currentPrice < 10000) return 10;
		if (currentPrice < 50000) return 50;
		if (currentPrice < 100000) return 100;
		if (currentPrice < 500000) return 500;
		return 1000;
	}

	function setQuantityPercent(percent: number) {
		const maxQty = side === 'BUY' ? maxBuyQuantity() : maxSellQuantity();
		quantity = Math.floor(maxQty * (percent / 100));
	}

	function formatNumber(num: number): string {
		return num.toLocaleString();
	}

	function handleSubmit() {
		if (!stock) return;

		error = null;

		// Validation
		if (quantity <= 0) {
			error = '수량을 입력해주세요.';
			return;
		}

		if (orderType === 'LIMIT' && price <= 0) {
			error = '가격을 입력해주세요.';
			return;
		}

		if (side === 'BUY' && finalAmount > availableCash) {
			error = '주문 가능 금액을 초과했습니다.';
			return;
		}

		if (side === 'SELL' && quantity > holdingQuantity) {
			error = '보유 수량을 초과했습니다.';
			return;
		}

		const orderData: OrderData = {
			stockId: stock.stockId,
			side,
			orderType,
			price: orderType === 'MARKET' ? stock.currentPrice : price,
			quantity
		};

		onSubmit?.(orderData);

		// Reset
		quantity = 0;
		toastStore.success(
			`${stock.stockName} ${quantity}주 ${side === 'BUY' ? '매수' : '매도'} 주문이 접수되었습니다.`,
			'주문 접수'
		);
	}
</script>

<div class="trade-panel" class:compact>
	<div class="trade-panel-header">
		<button
			type="button"
			class="trade-panel-tab buy"
			class:active={side === 'BUY'}
			onclick={() => (side = 'BUY')}
		>
			<TrendingDown size={16} />
			매수
		</button>
		<button
			type="button"
			class="trade-panel-tab sell"
			class:active={side === 'SELL'}
			onclick={() => (side = 'SELL')}
		>
			<TrendingUp size={16} />
			매도
		</button>
	</div>

	<div class="trade-panel-body">
		{#if stock}
			<!-- Stock Info -->
			<div class="trade-stock-info">
				<span class="trade-stock-name">{stock.stockName}</span>
				<div class="trade-stock-price">
					<div class="trade-stock-current {priceDirection()}">
						₩{formatNumber(stock.currentPrice)}
					</div>
					<div
						class="trade-stock-change"
						class:text-stock-up={priceChange().change >= 0}
						class:text-stock-down={priceChange().change < 0}
					>
						{priceChange().change >= 0 ? '+' : ''}{formatNumber(priceChange().change)}
						({priceChange().percent >= 0 ? '+' : ''}{priceChange().percent.toFixed(2)}%)
					</div>
				</div>
			</div>

			<!-- Order Type -->
			<div class="trade-order-type">
				<button
					type="button"
					class="trade-order-type-btn"
					class:active={orderType === 'LIMIT'}
					onclick={() => (orderType = 'LIMIT')}
				>
					지정가
				</button>
				<button
					type="button"
					class="trade-order-type-btn"
					class:active={orderType === 'MARKET'}
					onclick={() => (orderType = 'MARKET')}
				>
					시장가
				</button>
			</div>

			<!-- Price Input -->
			{#if orderType === 'LIMIT'}
				<div class="trade-form-group">
					<div class="trade-form-label">
						<span>주문 가격</span>
						<span class="trade-form-hint">호가 단위: {formatNumber(getTickSize(price))}원</span>
					</div>
					<div class="trade-price-wrapper">
						<button type="button" class="trade-price-btn" onclick={() => adjustPrice(-1)}>
							<Minus size={16} />
						</button>
						<input
							type="text"
							class="trade-price-input"
							value={formatNumber(price)}
							oninput={(e) => {
								const value = e.currentTarget.value.replace(/,/g, '');
								price = parseInt(value) || 0;
							}}
						/>
						<button type="button" class="trade-price-btn" onclick={() => adjustPrice(1)}>
							<Plus size={16} />
						</button>
					</div>
				</div>
			{/if}

			<!-- Quantity Input -->
			<div class="trade-form-group">
				<div class="trade-form-label">
					<span>주문 수량</span>
					<span class="trade-form-hint">
						최대: {formatNumber(side === 'BUY' ? maxBuyQuantity() : maxSellQuantity())}주
					</span>
				</div>
				<div class="trade-quantity-wrapper">
					<input
						type="text"
						class="trade-quantity-input"
						value={formatNumber(quantity)}
						oninput={(e) => {
							const value = e.currentTarget.value.replace(/,/g, '');
							quantity = parseInt(value) || 0;
						}}
						placeholder="0"
					/>
					<div class="trade-quantity-presets">
						<button type="button" class="trade-preset-btn" onclick={() => setQuantityPercent(10)}>
							10%
						</button>
						<button type="button" class="trade-preset-btn" onclick={() => setQuantityPercent(25)}>
							25%
						</button>
						<button type="button" class="trade-preset-btn" onclick={() => setQuantityPercent(50)}>
							50%
						</button>
						<button type="button" class="trade-preset-btn" onclick={() => setQuantityPercent(100)}>
							100%
						</button>
					</div>
				</div>
			</div>

			<!-- Order Summary -->
			<div class="trade-summary">
				<div class="trade-summary-row">
					<span class="trade-summary-label">주문금액</span>
					<span class="trade-summary-value">₩{formatNumber(totalAmount)}</span>
				</div>
				<div class="trade-summary-row">
					<span class="trade-summary-label">수수료 (0.015%)</span>
					<span class="trade-summary-value">₩{formatNumber(commission)}</span>
				</div>
				<div class="trade-summary-row">
					<span class="trade-summary-label">총 {side === 'BUY' ? '결제' : '수령'}금액</span>
					<span class="trade-summary-value trade-summary-total">₩{formatNumber(finalAmount)}</span>
				</div>
			</div>

			<!-- Error -->
			{#if error}
				<div class="trade-error">{error}</div>
			{/if}

			<!-- Submit Button -->
			<button
				type="button"
				class="trade-submit-btn {side.toLowerCase()}"
				onclick={handleSubmit}
				disabled={quantity <= 0}
			>
				{side === 'BUY' ? '매수' : '매도'} 주문
			</button>

			<!-- Balance -->
			<div class="trade-balance">
				<span class="trade-balance-label">
					{side === 'BUY' ? '주문가능금액' : '보유수량'}
				</span>
				<span class="trade-balance-value">
					{side === 'BUY' ? `₩${formatNumber(availableCash)}` : `${formatNumber(holdingQuantity)}주`}
				</span>
			</div>
		{:else}
			<div class="text-center text-secondary p-lg">종목을 선택해주세요</div>
		{/if}
	</div>
</div>
