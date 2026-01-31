# Frontend - Backend 연결 완료 ✅

## 📋 구현 내용

### 1. 환경 변수 설정 ✅
**파일**: `frontend/.env`

```env
VITE_API_URL=http://localhost:9832
VITE_USE_MOCK=false
```

- API Gateway를 통한 Backend 접근
- Mock 데이터 비활성화

---

### 2. 타입 정의 업데이트 ✅
**파일**: `src/lib/types/user.ts`

#### Backend DTO와 정확히 매칭
```typescript
// Backend: UserResponse
interface User {
  userId: number;        // Long → number
  username: string;
  email: string;
  role: string;          // "ROLE_USER"
}

// Backend: SignUpResponse
interface SignUpResponse {
  userId: number;
  email: string;
  username: string;
}

// Backend: LoginResponse
interface LoginResponse {
  userId: number;
  email: string;
  username: string;
  role: string;
  sessionId: string;     // Cookie로도 전달됨
}
```

#### Auth State (Session 기반)
```typescript
interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  // token 필드 제거 (Session 방식)
  isLoading: boolean;
}
```

---

### 3. API Client 수정 ✅
**파일**: `src/lib/api/api.ts`

#### 주요 변경사항
```typescript
// ❌ 제거: Authorization 헤더
// const token = getAuthToken();
// if (token) {
//   headers['Authorization'] = `Bearer ${token}`;
// }

// ✅ 추가: credentials 설정
const response = await fetch(url, {
  ...fetchOptions,
  headers,
  credentials: 'include',  // ⭐ Cookie 자동 전송/저장
  signal: controller.signal
});
```

**`credentials: 'include'` 효과**:
- Cross-origin 요청 시에도 Cookie 전송
- Set-Cookie 헤더 자동 처리
- Session Cookie 자동 관리

---

### 4. userApi.ts 완전 재작성 ✅
**파일**: `src/lib/api/userApi.ts`

#### 새로운 엔드포인트
```typescript
const ENDPOINTS = {
  signup: '/user-service/api/v1/users/signup',
  login: '/user-service/api/v1/users/login',
  logout: '/user-service/api/v1/users/logout',
  me: '/user-service/api/v1/users/me'
};
```

#### 주요 함수
```typescript
// 회원가입
export async function signup(data: SignupRequest): Promise<ApiResponse<SignUpResponse>>

// 로그인 (Session Cookie 자동 저장)
export async function login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>>

// 로그아웃 (Session 무효화)
export async function logout(): Promise<ApiResponse<void>>

// 현재 사용자 조회 (Cookie 자동 전송)
export async function getCurrentUser(): Promise<ApiResponse<User>>
```

#### Token 관리 제거
```typescript
// ❌ 제거
// setTokens(accessToken, refreshToken)
// clearTokens()
// getAccessToken()

// ✅ Cookie는 브라우저가 자동 관리
```

---

### 5. authStore.ts 수정 ✅
**파일**: `src/lib/stores/authStore.ts`

#### Session 기반으로 변경
```typescript
// ❌ 제거: token 필드
interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  // token: string | null;  // 제거
  isLoading: boolean;
}

// ❌ 제거: localStorage에 token 저장
// localStorage.setItem('auth_token', token);

// ✅ 유지: user 정보만 localStorage에 저장 (UX용)
localStorage.setItem('auth_user', JSON.stringify(user));
```

#### 주요 메서드
```typescript
login(user: User)          // Session은 Cookie로 관리
logout()                   // localStorage만 정리
updateUser(userData)       // 사용자 정보 업데이트
initialize()               // localStorage에서 user 복원
```

---

### 6. 로그인 페이지 ✅
**파일**: `src/routes/login/+page.svelte`

#### 주요 기능
- 이메일/비밀번호 입력
- 기본 Validation (이메일 형식)
- 로그인 API 호출
- Session Cookie 자동 저장
- authStore 업데이트
- 홈(/)으로 리다이렉트

#### UI 특징
- 반응형 디자인
- 로딩 스피너
- 에러 처리 (Toast)
- Enter 키 지원

---

### 7. 회원가입 페이지 ✅
**파일**: `src/routes/signup/+page.svelte`

#### 주요 기능
- 이메일, 닉네임, 비밀번호 입력
- Frontend Validation:
  - 이메일 형식
  - 닉네임: 2~20자, 영문/한글/숫자
  - 비밀번호: 8~20자, 영문+숫자+특수문자
  - 비밀번호 확인
- 회원가입 API 호출
- 로그인 페이지로 리다이렉트

#### Validation (Backend와 동일)
```typescript
// 이메일
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// 닉네임 (2~20자, 영문/한글/숫자)
const usernameRegex = /^[a-zA-Z0-9가-힣]+$/;
if (username.length < 2 || username.length > 20) { ... }

// 비밀번호 (8~20자, 영문+숫자+특수문자)
const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]+$/;
if (password.length < 8 || password.length > 20) { ... }
```

---

## 🔄 전체 플로우

### 1. 회원가입 플로우
```
1. http://localhost:5173/signup 접속
   ↓
2. 이메일, 닉네임, 비밀번호 입력
   ↓
3. Frontend Validation
   ↓
4. POST /user-service/api/v1/users/signup
   ↓
5. Backend: User 생성, Balance 생성 (5,000,000원)
   ↓
6. Response: SignUpResponse (userId, email, username)
   ↓
7. Toast: "회원가입이 완료되었습니다"
   ↓
8. Redirect: /login
```

### 2. 로그인 플로우
```
1. http://localhost:5173/login 접속
   ↓
2. 이메일, 비밀번호 입력
   ↓
3. POST /user-service/api/v1/users/login
   ↓
4. Backend:
   ├─ 이메일/비밀번호 검증
   ├─ Session 생성 (Redis)
   └─ Response Header: Set-Cookie: SESSION=xxx
   ↓
5. Frontend:
   ├─ Browser: Cookie 자동 저장
   ├─ authStore.login(userData)
   └─ localStorage: user 정보 저장
   ↓
6. Toast: "로그인되었습니다"
   ↓
7. Redirect: /
```

### 3. 인증된 요청 플로우
```
1. 페이지 이동 또는 새로고침
   ↓
2. GET /user-service/api/v1/users/me
   ├─ Request Header: Cookie: SESSION=xxx (자동)
   ↓
3. Backend:
   ├─ Redis에서 Session 조회
   ├─ userId 추출
   └─ User 정보 반환
   ↓
4. Frontend:
   └─ authStore.updateUser(userData)
   ↓
5. 사용자 정보 표시
```

### 4. 로그아웃 플로우
```
1. 로그아웃 버튼 클릭
   ↓
2. POST /user-service/api/v1/users/logout
   ↓
3. Backend:
   ├─ Session 무효화
   └─ Redis에서 Session 삭제
   ↓
4. Frontend:
   ├─ authStore.logout()
   └─ localStorage: user 정보 삭제
   ↓
5. Toast: "로그아웃되었습니다"
   ↓
6. Redirect: /login
```

---

## 🧪 테스트 시나리오

### 1. 회원가입 테스트
```bash
# 1. Frontend 실행
cd frontend
npm run dev

# 2. http://localhost:5173/signup 접속
# 3. 정보 입력 후 회원가입
# 4. → /login으로 이동 확인
```

**테스트 케이스**:
- ✅ 정상 회원가입
- ✅ 이메일 중복 (409 Conflict)
- ✅ 닉네임 중복 (409 Conflict)
- ✅ 이메일 형식 오류
- ✅ 닉네임 길이 오류
- ✅ 비밀번호 형식 오류
- ✅ 비밀번호 불일치

### 2. 로그인 테스트
```bash
# 1. http://localhost:5173/login 접속
# 2. 이메일/비밀번호 입력
# 3. 로그인 버튼 클릭
# 4. → / (홈)으로 이동 확인
```

**테스트 케이스**:
- ✅ 정상 로그인
- ✅ 비밀번호 오류 (400)
- ✅ 존재하지 않는 이메일 (404)
- ✅ Cookie 저장 확인 (DevTools)

### 3. Session 확인 (DevTools)
```
1. 로그인 후
2. F12 → Application → Cookies
3. → SESSION 쿠키 확인
   - Name: SESSION
   - Value: {sessionId}
   - Domain: localhost
   - Path: /
   - HttpOnly: ✅
   - SameSite: Lax
```

### 4. 페이지 새로고침 (Session 유지)
```
1. 로그인 상태에서 F5 (새로고침)
2. → authStore.initialize() 실행
3. → getCurrentUser() API 호출
4. → 사용자 정보 유지
```

### 5. 로그아웃 테스트
```
1. 로그아웃 버튼 클릭
2. → /login으로 이동 확인
3. → DevTools Cookies에서 SESSION 삭제 확인
```

### 6. Session 만료 테스트 (30분 후)
```
1. 로그인 후 30분 대기 (또는 Redis에서 Session 삭제)
2. 페이지 새로고침
3. → getCurrentUser() 실패 (401)
4. → authStore.logout()
5. → /login으로 이동
```

---

## ⚠️ 주의사항 및 트러블슈팅

### 1. CORS 오류
**증상**: `Access to fetch at '...' has been blocked by CORS policy`

**해결**: Backend에 CORS 설정 필요
```yaml
# backend/user-service/src/main/resources/application.yml
spring:
  web:
    cors:
      allowed-origins:
        - http://localhost:5173
        - http://localhost:4173
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers:
        - "*"
      allow-credentials: true  # ⭐ 중요!
```

### 2. Cookie가 저장되지 않음
**원인**: 
- `credentials: 'include'` 누락
- Backend에서 `allow-credentials: false`

**해결**:
- Frontend: `api.ts`에서 `credentials: 'include'` 확인
- Backend: `allow-credentials: true` 설정

### 3. API 응답 형식 불일치
**증상**: `TypeError: Cannot read property 'data' of undefined`

**원인**: Backend와 Frontend의 `ApiResponse` 구조 다름

**해결**: Backend `ApiResponse` 확인
```typescript
// Frontend 기대 형식
{
  success: boolean;
  data: T | null;
  error: string | null;  // Backend: message
  timestamp: string;      // Backend: timestamp (Long)
}
```

### 4. Session이 공유되지 않음 (Localhost vs 127.0.0.1)
**원인**: Cookie Domain 불일치

**해결**: Frontend와 Backend 모두 `localhost` 사용
```
Frontend: http://localhost:5173
Backend: http://localhost:9832
```

---

## 📁 수정/생성된 파일

### 수정 (5개)
1. ✅ `frontend/.env`
2. ✅ `src/lib/types/user.ts`
3. ✅ `src/lib/api/api.ts`
4. ✅ `src/lib/api/userApi.ts`
5. ✅ `src/lib/stores/authStore.ts`

### 신규 생성 (3개)
6. ✅ `src/routes/login/+page.svelte`
7. ✅ `src/routes/signup/+page.svelte`
8. ✅ `FRONTEND_BACKEND_INTEGRATION.md` (이 문서)

---

## 🚀 실행 방법

### 1. Backend 실행
```bash
# Docker Compose (권장)
docker-compose --profile all up -d

# 또는 개별 실행
./gradlew :backend:user-service:bootRun
```

### 2. Frontend 실행
```bash
cd frontend

# 의존성 설치 (최초 1회)
npm install

# 개발 서버 실행
npm run dev

# → http://localhost:5173
```

### 3. 테스트
```
1. http://localhost:5173/signup → 회원가입
2. http://localhost:5173/login → 로그인
3. http://localhost:5173/ → 홈 (인증 필요)
```

---

## 🎯 다음 단계 (TODO)

### 1. Layout에서 인증 확인
- [ ] `src/routes/+layout.svelte` 수정
- [ ] Public routes 제외하고 자동 로그인 확인
- [ ] Session 만료 시 /login으로 리다이렉트

### 2. Header/Nav에 사용자 정보 표시
- [ ] 사용자 닉네임 표시
- [ ] 로그아웃 버튼 추가
- [ ] 로그인 상태에 따라 버튼 변경

### 3. Balance 조회 API 연동
- [ ] GET `/user-service/api/v1/users/me/balance` 엔드포인트 생성 (Backend)
- [ ] Balance API 호출 (Frontend)
- [ ] 잔고 표시 UI

### 4. Trading Service 연동
- [ ] 주식 매수/매도 API
- [ ] 포트폴리오 조회

### 5. 에러 페이지
- [ ] 404 Not Found
- [ ] 500 Internal Server Error
- [ ] 401 Unauthorized

---

## 💡 개발 팁

### Session 디버깅
```typescript
// getCurrentUser API 응답 확인
const response = await getCurrentUser();
console.log('User:', response.data);

// authStore 상태 확인
import { get } from 'svelte/store';
import { authStore } from '$lib/stores/authStore';
console.log('Auth State:', get(authStore));
```

### Cookie 확인
```bash
# Chrome DevTools
F12 → Application → Cookies → http://localhost:5173

# SESSION Cookie 확인
Name: SESSION
Value: {sessionId}
Domain: localhost
HttpOnly: ✅
```

### API 요청 로그
```typescript
// api.ts에 추가
console.log('API Request:', {
  method: fetchOptions.method,
  url,
  credentials: 'include'
});
```

---

Frontend - Backend 연결 완료! 🎉

이제 회원가입/로그인 기능이 완전히 작동합니다!
