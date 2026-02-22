// Portfolio Helper - Enriches backend portfolio data with stock info for UI display
import { getStocks } from './stockApi.js';
import { getPortfolioRaw, getBalance } from './tradingApi.js';
import type { BackendTradeResponse } from './tradingApi.js';
import type { StockListItem } from '$lib/types/stock.js';
import type { Portfolio, Holding, Transaction } from '$lib/types/user.js';

/**
 * Fetches all stocks and builds a lookup map by stockId.
 */
export async function getStockLookupMap(): Promise<Map<string, StockListItem>> {
	const map = new Map<string, StockListItem>();
	try {
		const res = await getStocks({ page: 0, size: 500 });
		if (res.success && res.data) {
			for (const stock of res.data.stocks) {
				map.set(stock.stockId, stock);
			}
		}
	} catch {
		// Return empty map on failure
	}
	return map;
}

/**
 * Fetches portfolio + balance from backend and enriches holdings with stock info.
 * Returns a UI-friendly Portfolio object.
 */
export async function getEnrichedPortfolio(
	investorId: string,
	investorType: string = 'USER'
): Promise<Portfolio | null> {
	try {
		const [portfolioRes, balanceRes, stockMap] = await Promise.all([
			getPortfolioRaw(investorId, investorType),
			getBalance(investorId, investorType).catch(() => null),
			getStockLookupMap()
		]);

		if (!portfolioRes.success || !portfolioRes.data) return null;

		const cash = balanceRes?.success && balanceRes.data ? balanceRes.data.cash : 0;
		const backendHoldings = portfolioRes.data.holdings;

		const holdings: Holding[] = backendHoldings.map((h) => {
			const stock = stockMap.get(h.stockId);
			const currentPrice = stock?.currentPrice ?? h.averagePrice;
			const stockName = stock?.stockName ?? h.stockId;
			const totalValue = currentPrice * h.quantity;
			const profitLoss = totalValue - h.totalInvested;
			const profitLossPercent = h.totalInvested > 0 ? (profitLoss / h.totalInvested) * 100 : 0;

			return {
				stockId: h.stockId,
				stockName,
				quantity: h.quantity,
				averagePrice: h.averagePrice,
				currentPrice,
				totalValue,
				profitLoss,
				profitLossPercent
			};
		});

		const totalStockValue = holdings.reduce((sum, h) => sum + h.totalValue, 0);
		const totalInvested = backendHoldings.reduce((sum, h) => sum + h.totalInvested, 0);
		const totalProfitLoss = totalStockValue - totalInvested;
		const totalProfitLossPercent = totalInvested > 0 ? (totalProfitLoss / totalInvested) * 100 : 0;
		const totalAssetValue = cash + totalStockValue;

		return {
			userId: investorId,
			holdings,
			cashBalance: cash,
			totalStockValue,
			totalAssetValue,
			totalProfitLoss,
			totalProfitLossPercent
		};
	} catch {
		return null;
	}
}

/**
 * Converts backend TradeResponse[] to frontend Transaction[] for display.
 * Determines BUY/SELL based on whether the user is the buyer or seller.
 */
export function mapTradesToTransactions(
	trades: BackendTradeResponse[],
	userId: string,
	stockMap: Map<string, StockListItem>
): Transaction[] {
	return trades.map((t) => {
		const side = t.buyerId === userId ? 'BUY' : 'SELL';
		const stock = stockMap.get(t.stockId);
		return {
			transactionId: t.tradeId,
			orderId: side === 'BUY' ? t.buyOrderId : t.sellOrderId,
			stockId: t.stockId,
			stockName: stock?.stockName ?? t.stockId,
			side,
			quantity: t.quantity,
			price: t.price,
			totalAmount: t.tradeAmount,
			timestamp: t.tradedAt
		};
	});
}
