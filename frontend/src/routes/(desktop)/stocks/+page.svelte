<script lang="ts">
	import { Search, Filter } from 'lucide-svelte';
	import { Button, Card, Input } from '$lib/components';
	import { getStockListItems } from '$lib/mock/stocks.js';
	import { SECTOR_NAMES, MARKET_CAP_NAMES, type Sector, type MarketCapGrade } from '$lib/types/stock.js';

	let searchQuery = $state('');
	let selectedSector = $state<Sector | 'ALL'>('ALL');

	const allStocks = getStockListItems();

	const filteredStocks = $derived(() => {
		let result = allStocks;

		if (searchQuery) {
			const query = searchQuery.toLowerCase();
			result = result.filter(s =>
				s.stockName.toLowerCase().includes(query) ||
				s.stockId.toLowerCase().includes(query)
			);
		}

		if (selectedSector !== 'ALL') {
			result = result.filter(s => s.sector === selectedSector);
		}

		return result;
	});

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

	const sectors: (Sector | 'ALL')[] = ['ALL', 'IT', 'AGRICULTURE', 'MANUFACTURING', 'SERVICE', 'REALESTATE', 'LUXURY', 'FOOD'];
</script>

<div class="stocks-page">
	<div class="flex justify-between items-center mb-lg">
		<h1 class="text-2xl font-bold">주식 투자</h1>
	</div>

	<!-- Filters -->
	<div class="flex gap-md mb-lg flex-col-tablet">
		<div class="flex-1">
			<div class="input-icon-wrapper">
				<span class="input-icon"><Search size={18} /></span>
				<input
					type="search"
					class="input"
					placeholder="종목명 또는 코드로 검색..."
					bind:value={searchQuery}
				/>
			</div>
		</div>
		<div class="flex gap-sm flex-wrap">
			{#each sectors as sector}
				<button
					class="btn btn-sm"
					class:btn-primary={selectedSector === sector}
					class:btn-secondary={selectedSector !== sector}
					onclick={() => selectedSector = sector}
				>
					{sector === 'ALL' ? '전체' : SECTOR_NAMES[sector]}
				</button>
			{/each}
		</div>
	</div>

	<!-- Stock Table -->
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
				{#each filteredStocks() as stock}
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
						<td class="stock-price text-right">₩{formatPrice(stock.currentPrice)}</td>
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

		{#if filteredStocks().length === 0}
			<div class="table-empty">
				<div class="table-empty-icon">
					<Search size={48} />
				</div>
				<div class="table-empty-text">검색 결과가 없습니다</div>
			</div>
		{/if}
	</div>
</div>
