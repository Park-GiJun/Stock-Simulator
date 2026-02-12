<script lang="ts">
	import { login } from '$lib/api/userApi';
	import { authStore } from '$lib/stores/authStore';
	import { goto } from '$app/navigation';
	import { toastStore } from '$lib/stores/toastStore';
	import { onMount } from 'svelte';
	import { isAuthenticated } from '$lib/stores/authStore';
	import { get } from 'svelte/store';

	let email = '';
	let password = '';
	let isLoading = false;

	// 이미 로그인되어 있으면 홈으로 리다이렉트
	onMount(() => {
		if (get(isAuthenticated)) {
			goto('/');
		}
	});

	async function handleLogin() {
		if (!email || !password) {
			toastStore.error('이메일과 비밀번호를 입력해주세요');
			return;
		}

		// 기본 이메일 형식 검증
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			toastStore.error('올바른 이메일 형식을 입력해주세요');
			return;
		}

		isLoading = true;
		authStore.setLoading(true);

		try {
			const response = await login({ email, password });

			if (response.success && response.data) {
				// authStore에 사용자 정보 저장 (Session은 Cookie로 자동 관리)
				authStore.login({
					userId: response.data.userId,
					email: response.data.email,
					username: response.data.username,
					role: response.data.role
				});

				toastStore.success('로그인되었습니다');
				goto('/');
			}
		} catch (error) {
			// api.ts에서 이미 toastStore.error 처리됨
			console.error('Login error:', error);
		} finally {
			isLoading = false;
			authStore.setLoading(false);
		}
	}

	function handleKeyPress(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handleLogin();
		}
	}
</script>

<svelte:head>
	<title>로그인 - Stock Simulator</title>
</svelte:head>

<div class="auth-container">
	<div class="auth-card">
		<div class="auth-header">
			<h1>로그인</h1>
			<p>모의 주식 게임에 오신 것을 환영합니다</p>
		</div>

		<form on:submit|preventDefault={handleLogin} class="auth-form">
			<div class="form-group">
				<label for="email">이메일</label>
				<input
					type="email"
					id="email"
					bind:value={email}
					placeholder="example@email.com"
					required
					disabled={isLoading}
					on:keypress={handleKeyPress}
				/>
			</div>

			<div class="form-group">
				<label for="password">비밀번호</label>
				<input
					type="password"
					id="password"
					bind:value={password}
					placeholder="비밀번호를 입력하세요"
					required
					disabled={isLoading}
					on:keypress={handleKeyPress}
				/>
			</div>

			<button type="submit" class="submit-button" disabled={isLoading}>
				{#if isLoading}
					<span class="spinner"></span>
					로그인 중...
				{:else}
					로그인
				{/if}
			</button>
		</form>

		<div class="auth-footer">
			<p>계정이 없으신가요?</p>
			<a href="/signup">회원가입</a>
		</div>
	</div>
</div>
