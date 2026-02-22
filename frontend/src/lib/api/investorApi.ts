// Investor API - Institution and NPC investor related API functions
import { api, type ApiResponse } from './api.js';
import type { Institution, Npc } from '$lib/types/investor.js';

// API Endpoints
const ENDPOINTS = {
	institutions: '/stock-service/api/investors/institutions',
	npcs: '/stock-service/api/investors/npcs'
};

// Backend PageResponse format
interface PageResponse<T> {
	content: T[];
	page: number;
	size: number;
	totalElements: number;
	totalPages: number;
	first: boolean;
	last: boolean;
	empty: boolean;
}

// Types for API responses
export interface InstitutionsResponse {
	institutions: Institution[];
	total: number;
	page: number;
	size: number;
}

export interface NpcsResponse {
	npcs: Npc[];
	total: number;
	page: number;
	size: number;
}

// Query params
export interface InvestorQueryParams {
	page?: number;
	size?: number;
	sortBy?: string;
	sortOrder?: 'asc' | 'desc';
}

// API Functions
export async function getInstitutions(
	params?: InvestorQueryParams
): Promise<ApiResponse<InstitutionsResponse>> {
	const res = await api.get<PageResponse<Institution>>(ENDPOINTS.institutions, {
		params: params as Record<string, string | number | boolean | undefined>
	});
	return {
		...res,
		data: res.data
			? {
					institutions: res.data.content,
					total: res.data.totalElements,
					page: res.data.page,
					size: res.data.size
				}
			: null
	};
}

export async function getNpcs(params?: InvestorQueryParams): Promise<ApiResponse<NpcsResponse>> {
	const res = await api.get<PageResponse<Npc>>(ENDPOINTS.npcs, {
		params: params as Record<string, string | number | boolean | undefined>
	});
	return {
		...res,
		data: res.data
			? {
					npcs: res.data.content,
					total: res.data.totalElements,
					page: res.data.page,
					size: res.data.size
				}
			: null
	};
}
