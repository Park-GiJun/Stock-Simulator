// Static Menu Configuration

import type { MenuItem } from '$lib/types/menu.js';

export const STATIC_MENU: MenuItem[] = [
	{
		id: 'dashboard',
		name: '대시보드',
		path: '/',
		icon: 'LayoutDashboard',
		order: 1,
		isVisible: true
	},
	{
		id: 'stocks',
		name: '주식투자',
		path: '/stocks',
		icon: 'TrendingUp',
		order: 2,
		isVisible: true
	},
	{
		id: 'investors',
		name: '투자자',
		path: '/investors',
		icon: 'Users',
		order: 3,
		isVisible: true
	},
	{
		id: 'news',
		name: '뉴스',
		path: '/news',
		icon: 'Newspaper',
		order: 4,
		isVisible: true
	},
	{
		id: 'mypage',
		name: '마이페이지',
		path: '/mypage',
		icon: 'User',
		order: 5,
		isVisible: true
	},
	{
		id: 'ranking',
		name: '랭킹',
		path: '/ranking',
		icon: 'Trophy',
		order: 6,
		isVisible: true
	}
];

export const MOBILE_MENU: MenuItem[] = STATIC_MENU.filter((item) => item.isVisible);
