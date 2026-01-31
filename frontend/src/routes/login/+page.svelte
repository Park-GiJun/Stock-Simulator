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

<style>
	.auth-container {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 2rem;
		background: var(--color-bg-secondary);
	}

	.auth-card {
		width: 100%;
		max-width: 400px;
		background: var(--color-bg-primary);
		border-radius: 12px;
		padding: 2rem;
		box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
	}

	.auth-header {
		text-align: center;
		margin-bottom: 2rem;
	}

	.auth-header h1 {
		font-size: 1.75rem;
		font-weight: 700;
		color: var(--color-text-primary);
		margin-bottom: 0.5rem;
	}

	.auth-header p {
		color: var(--color-text-secondary);
		font-size: 0.875rem;
	}

	.auth-form {
		display: flex;
		flex-direction: column;
		gap: 1.25rem;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.form-group label {
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--color-text-primary);
	}

	.form-group input {
		padding: 0.75rem;
		border: 1px solid var(--color-border);
		border-radius: 8px;
		font-size: 1rem;
		background: var(--color-bg-secondary);
		color: var(--color-text-primary);
		transition: all 0.2s;
	}

	.form-group input:focus {
		outline: none;
		border-color: var(--color-primary);
		box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
	}

	.form-group input:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.submit-button {
		margin-top: 0.5rem;
		padding: 0.875rem;
		background: var(--color-primary);
		color: white;
		border: none;
		border-radius: 8px;
		font-size: 1rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
	}

	.submit-button:hover:not(:disabled) {
		background: var(--color-primary-dark);
		transform: translateY(-1px);
	}

	.submit-button:active:not(:disabled) {
		transform: translateY(0);
	}

	.submit-button:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.spinner {
		width: 16px;
		height: 16px;
		border: 2px solid rgba(255, 255, 255, 0.3);
		border-top-color: white;
		border-radius: 50%;
		animation: spin 0.6s linear infinite;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	.auth-footer {
		margin-top: 1.5rem;
		text-align: center;
		padding-top: 1.5rem;
		border-top: 1px solid var(--color-border);
	}

	.auth-footer p {
		color: var(--color-text-secondary);
		font-size: 0.875rem;
		margin-bottom: 0.5rem;
	}

	.auth-footer a {
		color: var(--color-primary);
		text-decoration: none;
		font-weight: 600;
		transition: color 0.2s;
	}

	.auth-footer a:hover {
		color: var(--color-primary-dark);
		text-decoration: underline;
	}

	@media (max-width: 640px) {
		.auth-container {
			padding: 1rem;
		}

		.auth-card {
			padding: 1.5rem;
		}

		.auth-header h1 {
			font-size: 1.5rem;
		}
	}
</style>
