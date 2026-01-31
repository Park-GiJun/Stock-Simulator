<script lang="ts" generics="T">
	import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-svelte';
	import type { Snippet } from 'svelte';

	interface Column<T> {
		key: string;
		label: string;
		sortable?: boolean;
		width?: string;
		align?: 'left' | 'center' | 'right';
		render?: (item: T, index: number) => string;
	}

	interface Props {
		columns: Column<T>[];
		data: T[];
		sortKey?: string;
		sortOrder?: 'asc' | 'desc';
		striped?: boolean;
		hoverable?: boolean;
		compact?: boolean;
		stickyHeader?: boolean;
		loading?: boolean;
		emptyMessage?: string;
		onSort?: (key: string, order: 'asc' | 'desc') => void;
		onRowClick?: (item: T, index: number) => void;
		rowCell?: Snippet<[{ item: T; column: Column<T>; index: number }]>;
	}

	let {
		columns,
		data,
		sortKey = $bindable(''),
		sortOrder = $bindable<'asc' | 'desc'>('asc'),
		striped = false,
		hoverable = true,
		compact = false,
		stickyHeader = false,
		loading = false,
		emptyMessage = '데이터가 없습니다.',
		onSort,
		onRowClick,
		rowCell
	}: Props = $props();

	function handleSort(column: Column<T>) {
		if (!column.sortable) return;

		if (sortKey === column.key) {
			sortOrder = sortOrder === 'asc' ? 'desc' : 'asc';
		} else {
			sortKey = column.key;
			sortOrder = 'asc';
		}

		onSort?.(sortKey, sortOrder);
	}

	function getValue(item: T, key: string): unknown {
		const keys = key.split('.');
		let value: unknown = item;
		for (const k of keys) {
			if (value && typeof value === 'object' && k in value) {
				value = (value as Record<string, unknown>)[k];
			} else {
				return undefined;
			}
		}
		return value;
	}

	function formatValue(item: T, column: Column<T>, index: number): string {
		if (column.render) {
			return column.render(item, index);
		}
		const value = getValue(item, column.key);
		if (value === null || value === undefined) return '-';
		if (typeof value === 'number') return value.toLocaleString();
		return String(value);
	}
</script>

<div class="table-wrapper" class:sticky-header={stickyHeader}>
	<table class="table" class:striped class:hoverable class:compact>
		<thead class="table-header">
			<tr>
				{#each columns as column (column.key)}
					<th
						class="table-th"
						class:sortable={column.sortable}
						style:width={column.width}
						style:text-align={column.align ?? 'left'}
					>
						{#if column.sortable}
							<button
								type="button"
								class="table-sort-btn"
								onclick={() => handleSort(column)}
								aria-label="정렬: {column.label}"
							>
								<span>{column.label}</span>
								<span class="table-sort-icon">
									{#if sortKey === column.key}
										{#if sortOrder === 'asc'}
											<ChevronUp size={14} />
										{:else}
											<ChevronDown size={14} />
										{/if}
									{:else}
										<ChevronsUpDown size={14} />
									{/if}
								</span>
							</button>
						{:else}
							{column.label}
						{/if}
					</th>
				{/each}
			</tr>
		</thead>
		<tbody class="table-body">
			{#if loading}
				{#each Array(5) as _, i (i)}
					<tr class="table-row">
						{#each columns as column (column.key)}
							<td class="table-td" style:text-align={column.align ?? 'left'}>
								<div class="skeleton skeleton-text" style="width: 80%"></div>
							</td>
						{/each}
					</tr>
				{/each}
			{:else if data.length === 0}
				<tr class="table-row table-empty">
					<td class="table-td" colspan={columns.length}>
						<div class="table-empty-message">{emptyMessage}</div>
					</td>
				</tr>
			{:else}
				{#each data as item, index (index)}
					<tr
						class="table-row"
						class:clickable={!!onRowClick}
						onclick={() => onRowClick?.(item, index)}
						onkeydown={(e) => e.key === 'Enter' && onRowClick?.(item, index)}
						tabindex={onRowClick ? 0 : undefined}
						role={onRowClick ? 'button' : undefined}
					>
						{#each columns as column (column.key)}
							<td class="table-td" style:text-align={column.align ?? 'left'}>
								{#if rowCell}
									{@render rowCell({ item, column, index })}
								{:else}
									{formatValue(item, column, index)}
								{/if}
							</td>
						{/each}
					</tr>
				{/each}
			{/if}
		</tbody>
	</table>
</div>

<style>
	.table-wrapper {
		width: 100%;
		overflow-x: auto;
	}

	.table-wrapper.sticky-header {
		max-height: 500px;
		overflow-y: auto;
	}

	.table {
		width: 100%;
		border-collapse: collapse;
		font-size: var(--font-size-sm);
	}

	.table-header {
		background: var(--color-bg-tertiary);
	}

	.sticky-header .table-header {
		position: sticky;
		top: 0;
		z-index: 1;
	}

	.table-th {
		padding: var(--spacing-sm) var(--spacing-md);
		font-weight: 600;
		color: var(--color-text-secondary);
		text-transform: uppercase;
		font-size: var(--font-size-xs);
		letter-spacing: 0.05em;
		border-bottom: 1px solid var(--color-border);
	}

	.table-th.sortable {
		padding: 0;
	}

	.table-sort-btn {
		display: flex;
		align-items: center;
		gap: var(--spacing-xs);
		width: 100%;
		padding: var(--spacing-sm) var(--spacing-md);
		border: none;
		background: transparent;
		color: inherit;
		font: inherit;
		text-align: inherit;
		cursor: pointer;
		transition: background var(--transition-fast);
	}

	.table-sort-btn:hover {
		background: var(--color-bg-hover);
	}

	.table-sort-icon {
		display: flex;
		align-items: center;
		color: var(--color-text-disabled);
	}

	.table-row {
		border-bottom: 1px solid var(--color-border);
	}

	.table.striped .table-row:nth-child(even) {
		background: var(--color-bg-secondary);
	}

	.table.hoverable .table-row:hover {
		background: var(--color-bg-hover);
	}

	.table-row.clickable {
		cursor: pointer;
	}

	.table-row.clickable:focus {
		outline: 2px solid var(--color-primary);
		outline-offset: -2px;
	}

	.table-td {
		padding: var(--spacing-sm) var(--spacing-md);
		color: var(--color-text-primary);
	}

	.table.compact .table-th,
	.table.compact .table-td {
		padding: var(--spacing-xs) var(--spacing-sm);
	}

	.table-empty-message {
		text-align: center;
		color: var(--color-text-secondary);
		padding: var(--spacing-xl);
	}

	/* Skeleton */
	.skeleton {
		height: 1em;
		background: linear-gradient(
			90deg,
			var(--color-bg-tertiary) 25%,
			var(--color-bg-secondary) 50%,
			var(--color-bg-tertiary) 75%
		);
		background-size: 200% 100%;
		animation: shimmer 1.5s ease-in-out infinite;
		border-radius: var(--radius-sm);
	}

	@keyframes shimmer {
		0% { background-position: 200% 0; }
		100% { background-position: -200% 0; }
	}
</style>
