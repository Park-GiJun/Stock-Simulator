<script lang="ts">
	import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-svelte';

	interface Props {
		currentPage?: number;
		totalPages: number;
		totalItems?: number;
		pageSize?: number;
		siblingCount?: number;
		size?: 'sm' | 'md' | 'lg';
		compact?: boolean;
		showInfo?: boolean;
		showFirstLast?: boolean;
		onChange?: (page: number) => void;
	}

	let {
		currentPage = $bindable(1),
		totalPages,
		totalItems,
		pageSize = 20,
		siblingCount = 1,
		size = 'md',
		compact = false,
		showInfo = false,
		showFirstLast = true,
		onChange
	}: Props = $props();

	// Generate page range
	const pageRange = $derived(() => {
		const range: (number | 'ellipsis')[] = [];

		const leftSibling = Math.max(currentPage - siblingCount, 1);
		const rightSibling = Math.min(currentPage + siblingCount, totalPages);

		const showLeftEllipsis = leftSibling > 2;
		const showRightEllipsis = rightSibling < totalPages - 1;

		if (!showLeftEllipsis && showRightEllipsis) {
			// Show first few pages
			for (let i = 1; i <= Math.max(rightSibling, 3); i++) {
				range.push(i);
			}
			range.push('ellipsis');
			range.push(totalPages);
		} else if (showLeftEllipsis && !showRightEllipsis) {
			// Show last few pages
			range.push(1);
			range.push('ellipsis');
			for (let i = Math.min(leftSibling, totalPages - 2); i <= totalPages; i++) {
				range.push(i);
			}
		} else if (showLeftEllipsis && showRightEllipsis) {
			// Show middle pages
			range.push(1);
			range.push('ellipsis');
			for (let i = leftSibling; i <= rightSibling; i++) {
				range.push(i);
			}
			range.push('ellipsis');
			range.push(totalPages);
		} else {
			// Show all pages
			for (let i = 1; i <= totalPages; i++) {
				range.push(i);
			}
		}

		return range;
	});

	const startItem = $derived((currentPage - 1) * pageSize + 1);
	const endItem = $derived(Math.min(currentPage * pageSize, totalItems ?? totalPages * pageSize));

	function goToPage(page: number) {
		if (page < 1 || page > totalPages || page === currentPage) return;
		currentPage = page;
		onChange?.(page);
	}
</script>

<div class="pagination-full">
	{#if showInfo && totalItems !== undefined}
		<div class="pagination-info">
			<span><strong>{startItem}</strong> - <strong>{endItem}</strong> / <strong>{totalItems}</strong>개</span>
		</div>
	{/if}

	<nav class="pagination pagination-{size}" class:pagination-compact={compact} aria-label="페이지네이션">
		{#if showFirstLast}
			<button
				type="button"
				class="pagination-btn pagination-nav"
				disabled={currentPage === 1}
				onclick={() => goToPage(1)}
				aria-label="첫 페이지"
			>
				<ChevronsLeft size={16} />
			</button>
		{/if}

		<button
			type="button"
			class="pagination-btn pagination-nav"
			disabled={currentPage === 1}
			onclick={() => goToPage(currentPage - 1)}
			aria-label="이전 페이지"
		>
			<ChevronLeft size={16} />
		</button>

		{#each pageRange() as item, i (i)}
			{#if item === 'ellipsis'}
				<span class="pagination-ellipsis">...</span>
			{:else}
				<button
					type="button"
					class="pagination-btn"
					class:active={currentPage === item}
					onclick={() => goToPage(item)}
					aria-label="페이지 {item}"
					aria-current={currentPage === item ? 'page' : undefined}
				>
					{item}
				</button>
			{/if}
		{/each}

		<button
			type="button"
			class="pagination-btn pagination-nav"
			disabled={currentPage === totalPages}
			onclick={() => goToPage(currentPage + 1)}
			aria-label="다음 페이지"
		>
			<ChevronRight size={16} />
		</button>

		{#if showFirstLast}
			<button
				type="button"
				class="pagination-btn pagination-nav"
				disabled={currentPage === totalPages}
				onclick={() => goToPage(totalPages)}
				aria-label="마지막 페이지"
			>
				<ChevronsRight size={16} />
			</button>
		{/if}
	</nav>
</div>
