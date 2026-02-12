<script lang="ts">
	import { onMount } from 'svelte';
	import { Search } from 'lucide-svelte';
	import { getStocks } from '$lib/api/stockApi.js';
	import { SECTOR_NAMES, MARKET_CAP_NAMES, type Sector, type MarketCapGrade, type StockListItem } from '$lib/types/stock.js';

	let searchQuery = $state('');
	let selectedSector = $state<Sector | 'ALL'>('ALL');
	let stocks = $state<StockListItem[]>([]);
	let loading = $state(true);
	let totalElements = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(50);

	async function fetchStocks() {
		loading = true;
		try {
			const res = await getStocks({
				page: currentPage,
				size: pageSize,
				sector: selectedSector === 'ALL' ? undefined : selectedSector,
				search: searchQuery || undefined,
				sortBy: 'stockName',
				sortOrder: 'asc'
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

	onMount(() => {
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

	const sectors: (Sector | 'ALL')[] = ['ALL', 'IT', 'AGRICULTURE', 'MANUFACTURING', 'SERVICE', 'REAL_ESTATE', 'LUXURY', 'FOOD'];
</script>

<div class="stocks-page">
	<div class="stocks-page-header flex justify-between items-center mb-lg">
		<h1 class="text-2xl font-bold gradient-text">주식 투자</h1>
	</div>

	<!-- Filters -->
	<div class="stocks-filter-bar flex gap-md mb-lg flex-col-tablet">
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
		<div class="flex gap-sm flex-wrap">
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
	</div>

	<!-- Stock Table -->
	<div class="table-wrapper">
		{#if loading}
			<div class="stocks-loading">
				<div class="stocks-loading-spinner"></div>
				<div class="text-secondary text-sm">로딩 중...</div>
			</div>
		{:else}
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
						<tr class="clickable" onclick={() => location.href = `/stocks/${stock.stockId}`}>
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
								<div class="stock-change" class:up={stock.changePercent >= 0} class:down={stock.changePercent < 0}>
									{formatPercent(stock.changePercent)}
								</div>
							</td>
							<td class="stock-volume">{formatVolume(stock.volume)}</td>
						</tr>
					{/each}
				</tbody>
			</table>

			{#if stocks.length === 0}
				<div class="table-empty">
					<div class="table-empty-icon">
						<Search size={48} />
					</div>
					<div class="table-empty-text">검색 결과가 없습니다</div>
				</div>
			{/if}
		{/if}
	</div>
</div>
