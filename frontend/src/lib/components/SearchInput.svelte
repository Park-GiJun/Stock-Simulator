<script lang="ts">
	import { Search, X, Loader2 } from 'lucide-svelte';
	import type { StockListItem } from '$lib/types/stock.js';
	import { SECTOR_NAMES } from '$lib/types/stock.js';
	import { searchStocks } from '$lib/api/stockApi.js';

	interface Props {
		value?: string;
		placeholder?: string;
		compact?: boolean;
		fullWidth?: boolean;
		onSelect?: (stock: StockListItem) => void;
	}

	let {
		value = $bindable(''),
		placeholder = '종목 검색...',
		compact = false,
		fullWidth = false,
		onSelect
	}: Props = $props();

	let isOpen = $state(false);
	let loading = $state(false);
	let results = $state<StockListItem[]>([]);
	let highlightedIndex = $state(-1);
	let inputRef = $state<HTMLInputElement | null>(null);
	let debounceTimer: ReturnType<typeof setTimeout>;

	async function handleSearch(query: string) {
		if (!query || query.length < 2) {
			results = [];
			isOpen = false;
			return;
		}

		loading = true;
		try {
			const response = await searchStocks(query);
			if (response.success && response.data) {
				results = response.data;
				isOpen = results.length > 0;
			}
		} catch {
			results = [];
		} finally {
			loading = false;
		}
	}

	function handleInput(e: Event) {
		const target = e.target as HTMLInputElement;
		value = target.value;
		highlightedIndex = -1;

		clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			handleSearch(value);
		}, 300);
	}

	function handleSelect(stock: StockListItem) {
		value = stock.stockName;
		isOpen = false;
		highlightedIndex = -1;
		onSelect?.(stock);
	}

	function handleClear() {
		value = '';
		results = [];
		isOpen = false;
		inputRef?.focus();
	}

	function handleKeyDown(e: KeyboardEvent) {
		if (!isOpen) {
			if (e.key === 'ArrowDown' && results.length > 0) {
				isOpen = true;
				highlightedIndex = 0;
				e.preventDefault();
			}
			return;
		}

		switch (e.key) {
			case 'ArrowDown':
				e.preventDefault();
				highlightedIndex = Math.min(highlightedIndex + 1, results.length - 1);
				break;
			case 'ArrowUp':
				e.preventDefault();
				highlightedIndex = Math.max(highlightedIndex - 1, 0);
				break;
			case 'Enter':
				e.preventDefault();
				if (highlightedIndex >= 0 && results[highlightedIndex]) {
					handleSelect(results[highlightedIndex]);
				}
				break;
			case 'Escape':
				isOpen = false;
				highlightedIndex = -1;
				break;
		}
	}

	function handleBlur() {
		// Delay to allow click on dropdown item
		setTimeout(() => {
			isOpen = false;
		}, 200);
	}

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}
</script>

<div class="search-wrapper" class:compact class:full-width={fullWidth}>
	<div class="search-input-container">
		<span class="search-icon">
			<Search size={18} />
		</span>
		<input
			bind:this={inputRef}
			type="text"
			class="search-input"
			{placeholder}
			{value}
			oninput={handleInput}
			onkeydown={handleKeyDown}
			onfocus={() => results.length > 0 && (isOpen = true)}
			onblur={handleBlur}
			aria-expanded={isOpen}
			aria-autocomplete="list"
			aria-controls="search-results"
			role="combobox"
		/>
		{#if loading}
			<span class="search-loading">
				<Loader2 size={18} class="animate-spin" />
			</span>
		{:else if value}
			<button type="button" class="search-clear" onclick={handleClear} aria-label="검색어 지우기">
				<X size={16} />
			</button>
		{/if}
	</div>

	{#if isOpen && results.length > 0}
		<div class="search-dropdown" id="search-results" role="listbox">
			<div class="search-dropdown-header">검색 결과 ({results.length})</div>
			{#each results as stock, index (stock.stockId)}
				<button
					type="button"
					class="search-item"
					class:highlighted={index === highlightedIndex}
					onclick={() => handleSelect(stock)}
					onmouseenter={() => (highlightedIndex = index)}
					role="option"
					aria-selected={index === highlightedIndex}
				>
					<div class="search-item-main">
						<div class="search-item-title">{stock.stockName}</div>
						<div class="search-item-subtitle">{SECTOR_NAMES[stock.sector]}</div>
					</div>
					<div class="search-item-meta">
						<div class="search-item-price">₩{formatPrice(stock.currentPrice)}</div>
						<div
							class="search-item-change"
							class:up={stock.changePercent >= 0}
							class:down={stock.changePercent < 0}
						>
							{formatPercent(stock.changePercent)}
						</div>
					</div>
				</button>
			{/each}
		</div>
	{:else if isOpen && value.length >= 2 && !loading}
		<div class="search-dropdown">
			<div class="search-empty">검색 결과가 없습니다.</div>
		</div>
	{/if}
</div>
