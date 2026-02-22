<script lang="ts">
	import { onMount } from 'svelte';
	import {
		Search,
		Grid,
		List,
		ChevronLeft,
		ChevronRight,
		TrendingUp,
		TrendingDown,
		ArrowUpRight,
		ArrowDownRight
	} from 'lucide-svelte';
	import { getStocks } from '$lib/api/stockApi.js';
	import {
		SECTOR_NAMES,
		MARKET_CAP_NAMES,
		type Sector,
		type MarketCapGrade,
		type StockListItem
	} from '$lib/types/stock.js';

	// State
	let searchQuery = $state('');
	let selectedSector = $state<Sector | 'ALL'>('ALL');
	let selectedMarketCap = $state<MarketCapGrade | 'ALL'>('ALL');
	let sortBy = $state('stockName');
	let sortOrder = $state<'asc' | 'desc'>('asc');
	let viewMode = $state<'table' | 'card'>('table');
	let stocks = $state<StockListItem[]>([]);
	let loading = $state(true);
	let totalElements = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);

	// Market stats (derived from loaded stocks or all stocks)
	let allStocksForStats = $state<StockListItem[]>([]);

	let upCount = $derived(allStocksForStats.filter((s) => s.changePercent > 0).length);
	let downCount = $derived(allStocksForStats.filter((s) => s.changePercent < 0).length);
	let flatCount = $derived(allStocksForStats.filter((s) => s.changePercent === 0).length);
	let totalStockCount = $derived(allStocksForStats.length);
	let totalPages = $derived(Math.max(1, Math.ceil(totalElements / pageSize)));

	// Sector and market cap options
	const sectors: (Sector | 'ALL')[] = [
		'ALL',
		'IT',
		'AGRICULTURE',
		'MANUFACTURING',
		'SERVICE',
		'REAL_ESTATE',
		'LUXURY',
		'FOOD'
	];
	const marketCapGrades: (MarketCapGrade | 'ALL')[] = ['ALL', 'SMALL', 'MID', 'LARGE'];

	const sortOptions = [
		{ value: 'stockName', label: '종목명' },
		{ value: 'currentPrice', label: '현재가' },
		{ value: 'changePercent', label: '등락률' },
		{ value: 'volume', label: '거래량' }
	];

	async function fetchStocks() {
		loading = true;
		try {
			const res = await getStocks({
				page: currentPage,
				size: pageSize,
				sector: selectedSector === 'ALL' ? undefined : selectedSector,
				marketCap: selectedMarketCap === 'ALL' ? undefined : selectedMarketCap,
				search: searchQuery || undefined,
				sortBy,
				sortOrder
			});
			if (res.success && res.data) {
				stocks = res.data.stocks;
				totalElements = res.data.total;
			}
		} catch (e) {
			console.error('Failed to fetch stocks:', e);
		} finally {
			loading = false;
		}
	}

	async function fetchMarketStats() {
		try {
			const res = await getStocks({ page: 0, size: 500 });
			if (res.success && res.data) {
				allStocksForStats = res.data.stocks;
			}
		} catch (e) {
			console.error('Failed to fetch market stats:', e);
		}
	}

	onMount(() => {
		fetchMarketStats();
		fetchStocks();
	});

	let debounceTimer: ReturnType<typeof setTimeout>;
	function onSearchInput() {
		clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			currentPage = 0;
			fetchStocks();
		}, 300);
	}

	function onSectorChange(sector: Sector | 'ALL') {
		selectedSector = sector;
		currentPage = 0;
		fetchStocks();
	}

	function onMarketCapChange(grade: MarketCapGrade | 'ALL') {
		selectedMarketCap = grade;
		currentPage = 0;
		fetchStocks();
	}

	function onSortChange(event: Event) {
		const target = event.target as HTMLSelectElement;
		sortBy = target.value;
		currentPage = 0;
		fetchStocks();
	}

	function toggleSortOrder() {
		sortOrder = sortOrder === 'asc' ? 'desc' : 'asc';
		currentPage = 0;
		fetchStocks();
	}

	function goToPage(page: number) {
		if (page < 0 || page >= totalPages) return;
		currentPage = page;
		fetchStocks();
	}

	function formatPrice(price: number): string {
		return price.toLocaleString();
	}

	function formatPercent(percent: number): string {
		const sign = percent >= 0 ? '+' : '';
		return `${sign}${percent.toFixed(2)}%`;
	}

	function formatVolume(volume: number): string {
		if (volume >= 1000000) return (volume / 1000000).toFixed(1) + 'M';
		if (volume >= 1000) return (volume / 1000).toFixed(0) + 'K';
		return volume.toString();
	}

	function getVisiblePages(current: number, total: number): (number | '...')[] {
		if (total <= 7) {
			return Array.from({ length: total }, (_, i) => i);
		}
		const pages: (number | '...')[] = [];
		pages.push(0);
		if (current > 3) {
			pages.push('...');
		}
		const start = Math.max(1, current - 1);
		const end = Math.min(total - 2, current + 1);
		for (let i = start; i <= end; i++) {
			pages.push(i);
		}
		if (current < total - 4) {
			pages.push('...');
		}
		pages.push(total - 1);
		return pages;
	}
</script>

<div class="stocks-page">
	<!-- Page Header with Market Stats -->
	<div class="stocks-page-header flex justify-between items-center mb-lg">
		<h1 class="text-2xl font-bold gradient-text">주식 투자</h1>
		<div class="view-mode-toggle">
			<button
				class="view-mode-btn"
				class:active={viewMode === 'table'}
				onclick={() => (viewMode = 'table')}
				title="테이블 보기"
			>
				<List size={18} />
			</button>
			<button
				class="view-mode-btn"
				class:active={viewMode === 'card'}
				onclick={() => (viewMode = 'card')}
				title="카드 보기"
			>
				<Grid size={18} />
			</button>
		</div>
	</div>

	<!-- Market Stats Summary -->
	<div class="grid grid-cols-4 gap-md mb-lg">
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon">
					<TrendingUp size={16} />
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">전체 종목</span>
					<span class="text-lg font-bold">{totalStockCount}</span>
				</div>
			</div>
		</div>
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon" style="background: rgba(34, 197, 94, 0.15); color: var(--color-stock-up);">
					<ArrowUpRight size={16} />
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">상승</span>
					<span class="text-lg font-bold" style="color: var(--color-stock-up);">{upCount}</span>
				</div>
			</div>
		</div>
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon" style="background: rgba(239, 68, 68, 0.15); color: var(--color-stock-down);">
					<ArrowDownRight size={16} />
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">하락</span>
					<span class="text-lg font-bold" style="color: var(--color-stock-down);">{downCount}</span>
				</div>
			</div>
		</div>
		<div class="card card-glass p-sm">
			<div class="flex items-center gap-sm">
				<div class="stock-icon" style="background: rgba(156, 163, 175, 0.15); color: var(--color-text-secondary);">
					<TrendingDown size={16} />
				</div>
				<div class="stack-xs">
					<span class="text-xs text-secondary">보합</span>
					<span class="text-lg font-bold">{flatCount}</span>
				</div>
			</div>
		</div>
	</div>

	<!-- Filter Bar -->
	<div class="stocks-filter-bar flex gap-md mb-lg flex-col-tablet">
		<!-- Search Input -->
		<div class="flex-1">
			<div class="input-icon-wrapper">
				<span class="input-icon"><Search size={18} /></span>
				<input
					type="search"
					class="input"
					placeholder="종목명 또는 코드로 검색..."
					bind:value={searchQuery}
					oninput={onSearchInput}
				/>
			</div>
		</div>

		<!-- Sort Controls -->
		<div class="flex gap-sm items-center">
			<select class="input" onchange={onSortChange} style="width: auto; min-width: 120px;">
				{#each sortOptions as opt}
					<option value={opt.value} selected={sortBy === opt.value}>{opt.label}</option>
				{/each}
			</select>
			<button
				class="btn btn-sm btn-secondary"
				onclick={toggleSortOrder}
				title={sortOrder === 'asc' ? '오름차순' : '내림차순'}
			>
				{sortOrder === 'asc' ? '↑' : '↓'}
			</button>
		</div>
	</div>

	<!-- Sector Filter Buttons -->
	<div class="flex gap-sm flex-wrap mb-md">
		{#each sectors as sector}
			<button
				class="btn btn-sm"
				class:btn-primary={selectedSector === sector}
				class:btn-secondary={selectedSector !== sector}
				onclick={() => onSectorChange(sector)}
			>
				{sector === 'ALL' ? '전체' : SECTOR_NAMES[sector]}
			</button>
		{/each}
	</div>

	<!-- Market Cap Filter Buttons -->
	<div class="flex gap-sm flex-wrap mb-lg">
		{#each marketCapGrades as grade}
			<button
				class="btn btn-sm"
				class:btn-primary={selectedMarketCap === grade}
				class:btn-secondary={selectedMarketCap !== grade}
				onclick={() => onMarketCapChange(grade)}
			>
				{grade === 'ALL' ? '전체 시총' : MARKET_CAP_NAMES[grade]}
			</button>
		{/each}
	</div>

	<!-- Loading State -->
	{#if loading}
		<div class="stocks-loading">
			<div class="stocks-loading-spinner"></div>
			<div class="text-secondary text-sm">로딩 중...</div>
		</div>
	{:else if stocks.length === 0}
		<!-- Empty State -->
		<div class="table-wrapper">
			<div class="table-empty">
				<div class="table-empty-icon">
					<Search size={48} />
				</div>
				<div class="table-empty-text">검색 결과가 없습니다</div>
			</div>
		</div>
	{:else if viewMode === 'table'}
		<!-- Table View -->
		<div class="table-wrapper">
			<table class="table table-stock">
				<thead>
					<tr>
						<th>종목명</th>
						<th>산업</th>
						<th>시가총액</th>
						<th class="text-right">현재가</th>
						<th class="text-right">등락률</th>
						<th class="text-right">거래량</th>
					</tr>
				</thead>
				<tbody>
					{#each stocks as stock}
						<tr class="clickable" onclick={() => (location.href = `/stocks/${stock.stockId}`)}>
							<td>
								<div class="stock-name">
									<div class="stock-icon">{stock.stockName.charAt(0)}</div>
									<div class="stock-info">
										<span class="stock-title">{stock.stockName}</span>
										<span class="stock-code">{stock.stockId}</span>
									</div>
								</div>
							</td>
							<td>
								<span class="badge badge-sector-{stock.sector.toLowerCase()}">
									{SECTOR_NAMES[stock.sector]}
								</span>
							</td>
							<td>{MARKET_CAP_NAMES[stock.marketCapGrade]}</td>
							<td class="stock-price text-right">{formatPrice(stock.currentPrice)}</td>
							<td class="text-right">
								<div
									class="stock-change"
									class:up={stock.changePercent >= 0}
									class:down={stock.changePercent < 0}
								>
									{#if stock.changePercent >= 0}
										<ArrowUpRight size={14} />
									{:else}
										<ArrowDownRight size={14} />
									{/if}
									{formatPercent(stock.changePercent)}
								</div>
							</td>
							<td class="stock-volume">{formatVolume(stock.volume)}</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	{:else}
		<!-- Card Grid View -->
		<div class="stock-card-grid">
			{#each stocks as stock}
				<div
					class="stock-grid-card"
					role="button"
					tabindex="0"
					onclick={() => (location.href = `/stocks/${stock.stockId}`)}
					onkeydown={(e) => {
						if (e.key === 'Enter') location.href = `/stocks/${stock.stockId}`;
					}}
				>
					<div class="stock-grid-card-header">
						<div>
							<div class="stock-grid-card-name">{stock.stockName}</div>
							<div class="stock-grid-card-code">{stock.stockId}</div>
						</div>
						<span class="badge badge-sector-{stock.sector.toLowerCase()}">
							{SECTOR_NAMES[stock.sector]}
						</span>
					</div>

					<div
						class="stock-grid-card-price"
						class:up={stock.changePercent >= 0}
						class:down={stock.changePercent < 0}
					>
						{formatPrice(stock.currentPrice)}
					</div>

					<div class="stock-grid-card-bottom">
						<div
							class="stock-change"
							class:up={stock.changePercent >= 0}
							class:down={stock.changePercent < 0}
						>
							{#if stock.changePercent >= 0}
								<ArrowUpRight size={14} />
							{:else}
								<ArrowDownRight size={14} />
							{/if}
							{formatPercent(stock.changePercent)}
						</div>
						<div class="stock-volume text-sm">
							{formatVolume(stock.volume)}
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}

	<!-- Pagination -->
	{#if !loading && stocks.length > 0}
		<div class="pagination-bar">
			<button
				class="pagination-btn"
				disabled={currentPage === 0}
				onclick={() => goToPage(currentPage - 1)}
			>
				<ChevronLeft size={16} />
			</button>

			{#each getVisiblePages(currentPage, totalPages) as page}
				{#if page === '...'}
					<span class="pagination-ellipsis">...</span>
				{:else}
					<button
						class="pagination-btn"
						class:active={currentPage === page}
						onclick={() => goToPage(page)}
					>
						{page + 1}
					</button>
				{/if}
			{/each}

			<button
				class="pagination-btn"
				disabled={currentPage >= totalPages - 1}
				onclick={() => goToPage(currentPage + 1)}
			>
				<ChevronRight size={16} />
			</button>

			<span class="pagination-info">
				<strong>{currentPage * pageSize + 1}</strong>-<strong>{Math.min((currentPage + 1) * pageSize, totalElements)}</strong>
				/ {totalElements}건
			</span>
		</div>
	{/if}
</div>
