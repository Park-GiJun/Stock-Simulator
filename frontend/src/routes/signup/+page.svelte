<script lang="ts">
	import { signup } from '$lib/api/userApi';
	import { goto } from '$app/navigation';
	import { toastStore } from '$lib/stores/toastStore';
	import { onMount } from 'svelte';
	import { isAuthenticated } from '$lib/stores/authStore';
	import { get } from 'svelte/store';

	let email = '';
	let username = '';
	let password = '';
	let passwordConfirm = '';
	let isLoading = false;

	// 이미 로그인되어 있으면 홈으로 리다이렉트
	onMount(() => {
		if (get(isAuthenticated)) {
			goto('/');
		}
	});

	async function handleSignup() {
		// Validation
		if (!email || !username || !password || !passwordConfirm) {
			toastStore.error('모든 필드를 입력해주세요');
			return;
		}

		// 이메일 형식 검증
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			toastStore.error('올바른 이메일 형식을 입력해주세요');
			return;
		}

		// 닉네임 길이 검증 (Backend: 2~20자)
		if (username.length < 2 || username.length > 20) {
			toastStore.error('닉네임은 2~20자여야 합니다');
			return;
		}

		// 닉네임 형식 검증 (영문, 한글, 숫자만)
		const usernameRegex = /^[a-zA-Z0-9가-힣]+$/;
		if (!usernameRegex.test(username)) {
			toastStore.error('닉네임은 영문, 한글, 숫자만 가능합니다');
			return;
		}

		// 비밀번호 길이 검증 (Backend: 8~20자)
		if (password.length < 8 || password.length > 20) {
			toastStore.error('비밀번호는 8~20자여야 합니다');
			return;
		}

		// 비밀번호 형식 검증 (영문, 숫자, 특수문자 포함)
		const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]+$/;
		if (!passwordRegex.test(password)) {
			toastStore.error('비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다');
			return;
		}

		// 비밀번호 확인
		if (password !== passwordConfirm) {
			toastStore.error('비밀번호가 일치하지 않습니다');
			return;
		}

		isLoading = true;

		try {
			const response = await signup({ email, username, password });

			if (response.success) {
				toastStore.success('회원가입이 완료되었습니다. 로그인해주세요.');
				goto('/login');
			}
		} catch (error) {
			// api.ts에서 이미 toastStore.error 처리됨
			console.error('Signup error:', error);
		} finally {
			isLoading = false;
		}
	}

	function handleKeyPress(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handleSignup();
		}
	}
</script>

<svelte:head>
	<title>회원가입 - Stock Simulator</title>
</svelte:head>

<div class="auth-container">
	<div class="auth-card">
		<div class="auth-header">
			<h1>회원가입</h1>
			<p>모의 주식 게임을 시작하세요</p>
		</div>

		<form on:submit|preventDefault={handleSignup} class="auth-form">
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
				<span class="hint">로그인에 사용됩니다</span>
			</div>

			<div class="form-group">
				<label for="username">닉네임</label>
				<input
					type="text"
					id="username"
					bind:value={username}
					placeholder="2~20자 (영문, 한글, 숫자)"
					required
					minlength="2"
					maxlength="20"
					disabled={isLoading}
					on:keypress={handleKeyPress}
				/>
				<span class="hint">게임에서 표시되는 이름입니다</span>
			</div>

			<div class="form-group">
				<label for="password">비밀번호</label>
				<input
					type="password"
					id="password"
					bind:value={password}
					placeholder="8~20자 (영문, 숫자, 특수문자 포함)"
					required
					minlength="8"
					maxlength="20"
					disabled={isLoading}
					on:keypress={handleKeyPress}
				/>
				<span class="hint">영문, 숫자, 특수문자를 포함해야 합니다</span>
			</div>

			<div class="form-group">
				<label for="passwordConfirm">비밀번호 확인</label>
				<input
					type="password"
					id="passwordConfirm"
					bind:value={passwordConfirm}
					placeholder="비밀번호를 다시 입력하세요"
					required
					disabled={isLoading}
					on:keypress={handleKeyPress}
				/>
			</div>

			<button type="submit" class="submit-button" disabled={isLoading}>
				{#if isLoading}
					<span class="spinner"></span>
					처리 중...
				{:else}
					회원가입
				{/if}
			</button>
		</form>

		<div class="auth-footer">
			<p>이미 계정이 있으신가요?</p>
			<a href="/login">로그인</a>
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
		max-width: 420px;
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

	.hint {
		font-size: 0.75rem;
		color: var(--color-text-tertiary);
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
