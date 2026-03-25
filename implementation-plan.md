# Dream Log MVP 구현 계획서

## Context

**문제:** 자기계발을 하고 싶지만 의지가 부족한 사람들을 위한 서비스. 기존 일기/목표 관리 앱은 "이미 동기가 있는 사람"을 위해 설계됨. 핵심 모순: "의지가 없는 사람에게 의지를 요구하는 도구를 주는 것"을 시스템(챌린지+AI프롬프트)으로 해결.

**디자인 문서:** `design-20260324-manifest.md` (2026-03-24 APPROVED)

---

## Step 0: 범위 결정

### 범위 간소화 (3가지)

1. **AI 프롬프트 → 하이브리드:** 큐레이션 프롬프트 30개를 DB 시드로 제공 (기본). OpenAI는 "개인화 프롬프트" 버튼으로 선택적 호출. 비용 절감 + API 장애 시에도 서비스 가능.

2. **챌린지 상태 → 3개로 단순화:** ACTIVE, COMPLETED, FAILED만. MISSED는 별도 상태가 아니라 `challenge_daily_logs` 테이블의 레코드로 판단.

3. **이메일 → MVP에서 제외:** 앱 내 알림으로 대체 (로그인 시 "도착한 편지가 있습니다!" 배너). 이메일은 P1.

---

## 1. 프로젝트 구조 (모노레포)

```
dreamlog/
├── backend/                          # Spring Boot
│   ├── pom.xml                       # Maven
│   └── src/
│       ├── main/java/com/dreamlog/
│       │   ├── DreamlogApplication.java
│       │   ├── global/
│       │   │   ├── config/           # SecurityConfig, CorsConfig, JpaConfig, OpenAiConfig
│       │   │   ├── security/         # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│       │   │   ├── exception/        # GlobalExceptionHandler, ErrorCode, BusinessException
│       │   │   ├── common/           # BaseTimeEntity, ApiResponse
│       │   │   └── util/             # EncryptionUtil (AES-256), KstDateUtil
│       │   ├── auth/                 # AuthController, AuthService, DTOs
│       │   ├── user/                 # UserController, UserService, User entity
│       │   ├── diary/                # DiaryController, DiaryService, Diary entity
│       │   ├── challenge/            # ChallengeController, ChallengeService, ChallengeScheduler
│       │   └── prompt/               # PromptController, PromptService, PromptTemplate entity
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-local.yml
│       │   └── db/migration/         # Flyway: V1__init_schema.sql, V2__seed_prompts.sql
│       └── test/java/com/dreamlog/
│
├── frontend/                         # React + Vite + TypeScript
│   └── src/
│       ├── api/                      # client.ts, authApi, diaryApi, challengeApi, promptApi
│       ├── hooks/                    # useAuth, useDiaries, useChallenges
│       ├── pages/                    # Login, Signup, Home, DiaryWrite/List/Detail, Challenge*
│       ├── components/               # layout/, diary/, challenge/, prompt/, common/
│       ├── store/                    # authStore.ts (Zustand)
│       ├── types/                    # auth.ts, diary.ts, challenge.ts
│       └── styles/                   # Tailwind CSS
└── docs/
```

---

## 2. 데이터베이스 스키마

```sql
-- users
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,          -- BCrypt
    nickname        VARCHAR(50)  NOT NULL,
    profile_image   VARCHAR(500),
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP                       -- soft delete
);

-- diaries (미래 일기)
CREATE TABLE diaries (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    title           VARCHAR(200) NOT NULL,
    content         TEXT         NOT NULL,           -- AES-256 암호화
    unlock_date     DATE         NOT NULL,           -- 잠금 해제 날짜
    edit_deadline   TIMESTAMP    NOT NULL,           -- 작성 + 24시간
    written_date    DATE         NOT NULL,           -- KST 기준 작성일
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

-- challenges
CREATE TABLE challenges (
    id              BIGSERIAL PRIMARY KEY,
    creator_id      BIGINT       NOT NULL REFERENCES users(id),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    duration_days   INT          NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    max_participants INT         DEFAULT 50,
    fail_threshold  DECIMAL(3,2) NOT NULL DEFAULT 0.15,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- challenge_participants
CREATE TABLE challenge_participants (
    id              BIGSERIAL PRIMARY KEY,
    challenge_id    BIGINT       NOT NULL REFERENCES challenges(id),
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    missed_count    INT          NOT NULL DEFAULT 0,
    joined_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    UNIQUE(challenge_id, user_id)
);

-- challenge_daily_logs
CREATE TABLE challenge_daily_logs (
    id              BIGSERIAL PRIMARY KEY,
    participant_id  BIGINT       NOT NULL REFERENCES challenge_participants(id),
    log_date        DATE         NOT NULL,
    achieved        BOOLEAN      NOT NULL DEFAULT FALSE,
    checked_at      TIMESTAMP,
    UNIQUE(participant_id, log_date)
);

-- prompt_templates
CREATE TABLE prompt_templates (
    id              BIGSERIAL PRIMARY KEY,
    category        VARCHAR(50)  NOT NULL,    -- MOTIVATION, REFLECTION, GRATITUDE, DREAM, RANDOM
    content_ko      TEXT         NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- refresh_tokens
CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    token           VARCHAR(500) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### ER 다이어그램

```
  users 1──N diaries
    │
    ├── 1──N challenges (creator)
    │
    └── 1──N challenge_participants ──N──1 challenges
                    │
                    └── 1──N challenge_daily_logs

  prompt_templates (독립)
  refresh_tokens N──1 users
```

---

## 3. REST API

### 인증 `/api/auth`
- `POST /signup` — 회원가입 (email, password, nickname)
- `POST /login` — 로그인 → {accessToken, refreshToken, expiresIn}
- `POST /refresh` — AT 갱신 (Cookie의 RT 사용)
- `POST /logout` — RT 무효화

### 사용자 `/api/users`
- `GET /me` — 내 정보
- `PATCH /me` — 정보 수정
- `DELETE /me` — 탈퇴 (soft delete)

### 미래 일기 `/api/diaries`
- `POST /` — 일기 작성 (title, content, unlockDate)
- `GET /` — 내 일기 목록 (페이징, 잠긴 일기는 content=null)
- `GET /{id}` — 상세 (잠금 해제 시 복호화된 content, 잠김 시 content=null + daysUntilUnlock)
- `PATCH /{id}` — 수정 (24시간 내만)
- `DELETE /{id}` — 삭제
- `GET /arrived` — 도착한 편지 목록 (unlock_date <= today)

### 챌린지 `/api/challenges`
- `POST /` — 챌린지 생성
- `GET /` — 참가 가능한 챌린지 목록
- `GET /{id}` — 상세
- `POST /{id}/join` — 참가
- `GET /{id}/progress` — 내 진행상황
- `GET /my` — 내가 참가 중인 챌린지

### 프롬프트 `/api/prompts`
- `GET /random` — 랜덤 프롬프트 (category 파라미터 선택)
- `POST /ai` — OpenAI 개인화 프롬프트 (선택)

---

## 4. 인증 플로우 (JWT)

```
Client (React)                Spring Boot                  PostgreSQL
    │                              │                            │
    │  POST /login {email, pwd}    │                            │
    │─────────────────────────────>│  BCrypt.matches()          │
    │                              │  Generate AT (15분)        │
    │                              │  Generate RT (7일)         │
    │                              │  INSERT refresh_tokens     │
    │                              │───────────────────────────>│
    │  {accessToken, refreshToken} │                            │
    │  + Set-Cookie: RT (HttpOnly) │                            │
    │<─────────────────────────────│                            │
    │                              │                            │
    │  API 요청: Bearer {AT}       │                            │
    │─────────────────────────────>│  JwtAuthFilter 검증        │
    │                              │                            │
    │  AT 만료 시: POST /refresh   │                            │
    │  (Cookie RT 사용)            │                            │
    │─────────────────────────────>│  RT 검증 + 새 AT 발급      │
    │  {새 accessToken}            │                            │
    │<─────────────────────────────│                            │
```

- JJWT 0.12.6 사용
- SecurityFilterChain (NOT WebSecurityConfigurerAdapter)
- BCrypt 비밀번호 해싱
- Refresh Token Rotation (갱신 시 기존 RT 삭제 + 새 RT 발급)

---

## 5. 챌린지 엔진

### 상태 전이

```
    참가 ──> ACTIVE ──┬──> COMPLETED  (마지막 날 && missed <= max)
                      └──> FAILED     (missed > max)

    maxMiss = floor(duration_days * fail_threshold)
    예: 7일, 15% → floor(1.05) = 1일 허용
```

### ChallengeScheduler (매일 KST 00:05)

1. ACTIVE 참가자 전체 조회
2. 어제 날짜에 일기 작성 여부 확인 (`diaries` 테이블 체크)
3. `challenge_daily_logs` INSERT (achieved=true/false)
4. 미달성이면 missed_count++ → maxMiss 초과 시 FAILED
5. 마지막 날이면 COMPLETED 처리

---

## 6. 프론트엔드 아키텍처

- **React 18 + TypeScript + Vite**
- **상태 관리:** Zustand (인증), TanStack React Query v5 (서버 상태)
- **라우팅:** React Router v6
- **스타일:** Tailwind CSS
- **HTTP:** Axios (인터셉터로 AT 자동 첨부 + 401 시 RT 갱신)
- **폼:** React Hook Form + Zod

### 페이지 구조
```
/login, /signup              — 비인증
/                            — HomePage (대시보드: 도착 편지 + 챌린지 현황)
/diary/write                 — DiaryWritePage (프롬프트 + 날짜 + 작성)
/diary/list                  — DiaryListPage (잠금/열림 상태 카드)
/diary/:id                   — DiaryDetailPage (잠금 시 LockedDiary)
/challenges                  — ChallengeListPage
/challenges/create           — ChallengeCreatePage
/challenges/:id              — ChallengeDetailPage (진행 바 + 체크마크)
```

---

## 7. 구현 순서 (5 Sprint)

```
Sprint 1 (1주차): 기반 + 인증
  - 백엔드 프로젝트 초기화 (Maven, Spring Boot 3)
  - 프론트엔드 프로젝트 초기화 (Vite + React + TS)
  - Docker Compose (PostgreSQL + TZ=Asia/Seoul)
  - DB 스키마 (Flyway V1)
  - User 엔티티 + JWT 인증 전체
  - LoginPage, SignupPage, ProtectedRoute
  - AuthService 단위 테스트 (10개 코드패스)

Sprint 2 (2주차): 미래 일기
  - Diary CRUD + 잠금 로직 + AES-256 암호화
  - EncryptionUtil (AES-256-GCM)
  - DiaryWritePage, DiaryListPage, DiaryDetailPage
  - DatePicker, DiaryCard, LockedDiary 컴포넌트
  - DiaryService 단위 테스트 (11개 코드패스)

Sprint 3 (3주차): 챌린지 시스템
  - Challenge + Participant + DailyLog 엔티티
  - ChallengeScheduler (매일 자정 정산 + 보상 로직)
  - N+1 방지 JOIN 쿼리
  - ChallengeListPage, ChallengeDetailPage, ChallengeCreatePage
  - ChallengeScheduler 통합 테스트 (5개) + ChallengeService 단위 (3개)

Sprint 4 (4주차): AI 프롬프트 + 대시보드 + PWA Push
  - PromptTemplate 시드 데이터 30개 (큐레이션)
  - Spring AI + OpenAI 연동 (선택적 개인화)
  - HomePage 대시보드 (도착 편지 + 챌린지 현황)
  - PWA 설정 + Web Push Notification (편지 도착 알림)
  - PromptService 테스트 (2개)

Sprint 5 (5주차): 통합 테스트 + 마무리
  - E2E 테스트 2개 (회원가입→일기 플로우, 챌린지 플로우)
  - 에러 처리 정리 + 로딩 상태
  - 회원 탈퇴 (soft delete + 30일 후 완전 삭제)
  - 전체 테스트 점검 + README
```

---

## 8. 핵심 주의사항

- **일기 암호화:** AES-256-GCM, 키는 환경변수. `EncryptionUtil`의 encrypt/decrypt만 노출
- **KST 날짜:** 모든 날짜 비교에 `ZoneId.of("Asia/Seoul")` 명시. 스케줄러 cron도 KST. Docker Compose에 `TZ=Asia/Seoul` 환경변수 필수
- **스케줄러 보상 로직:** 서버 다운 등으로 누락된 날짜를 소급 처리. "마지막 체크 날짜" 이후 모든 미체크 날짜 순회
- **PWA Push:** Web Push Notification으로 편지 도착 알림. Service Worker 등록 필요
- **일기 삭제와 챌린지:** 이미 체크된 날의 일기 삭제해도 챌린지 기록 소급 변경 안 함
- **24시간 수정 제한:** 서버에서 반드시 검증 (프론트 카운트다운은 UX용)
- **RT Rotation:** refresh 시 기존 RT 삭제 + 새 RT 발급

---

## 9. 빌드 & 실행

```bash
# PostgreSQL
CREATE DATABASE dreamlog;
CREATE USER dreamlog_user WITH PASSWORD 'dreamlog_pass';

# Backend
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local
# → http://localhost:8080

# Frontend
cd frontend && npm install && npm run dev
# → http://localhost:5173 (Vite 프록시로 /api → :8080)
```

---

## Eng Review 결정사항

| # | 이슈 | 결정 |
|---|------|------|
| 범위1 | AI 프롬프트 | 하이브리드 (큐레이션 30개 + OpenAI 선택적) |
| 범위2 | 이메일 알림 | MVP 제외, 앱 내 알림으로 대체 |
| 아키1 | DB 설정 | Docker Compose로 PostgreSQL |
| 아키2 | 일기 암호화 | AES-256-GCM 포함 (제목은 평문) |
| 아키3 | OpenAI 연동 | Spring AI 프레임워크 사용 |
| 코드1 | 빌드 도구 | Maven (pom.xml) |
| 테스트 | 커버리지 | 전체 30개 코드패스 + E2E 2개 |
| 성능 | N+1 방지 | ChallengeScheduler에 JOIN 쿼리 사용 |

---

## NOT in scope (명시적으로 제외)

- **유료 챌린지 (돈 걸기):** 사행성 규제 법률 검토 완료 전까지 개발 안 함
- **소셜 로그인 (Google, Kakao):** P1. MVP는 이메일/비밀번호만
- **친구 시스템 / 소셜 기능:** P1
- **이메일 알림:** P1. 앱 내 알림으로 대체
- **감정 태그 / 감정 분석:** P2
- **캘린더 뷰:** P2
- **공개 게시판:** 수요 검증 후
- **마이 페이스 진단:** 수요 검증 후
- **다크 모드:** P2
- **안드로이드 네이티브 앱:** 웹 앱 검증 후
- **Key Rotation (암호화 키 교체):** 운영 단계에서

---

## What already exists

기존 코드 없음 (그린필드 프로젝트). `request.md`와 승인된 디자인 문서만 존재.

---

## Failure Modes

| 코드패스 | 실패 시나리오 | 테스트 커버 | 에러 처리 | 사용자 영향 |
|---------|-------------|-----------|---------|-----------|
| JWT 만료 | AT 만료 후 API 호출 | 계획됨 | Axios 인터셉터 자동 갱신 | 투명 |
| AES 복호화 실패 | 키 변경 후 기존 데이터 | 계획됨 | BusinessException | 에러 메시지 |
| 스케줄러 누락 | 서버 다운으로 자정 정산 미실행 | 계획됨 | 다음 실행 시 보상 로직 필요 | **CRITICAL GAP** |
| OpenAI 장애 | API 타임아웃/500 | 계획됨 | 큐레이션 프롬프트 fallback | 투명 |
| 동시 챌린지 참가 | 같은 유저가 동시에 join | 계획됨 | UNIQUE 제약 | 에러 메시지 |

**Critical Gap 1개:** 스케줄러 누락 시 보상 로직. 서버가 다운되어 자정 정산이 실행되지 않으면, 해당 날짜의 챌린지 체크가 영구 누락됩니다. → 스케줄러 실행 시 "마지막 체크 날짜" 이후 모든 미체크 날짜를 소급 처리하는 보상 로직 추가 필요.

---

## Completion Summary

- Step 0: Scope Challenge — **scope accepted** (AI 하이브리드, 이메일 제외, PWA Push 추가)
- Architecture Review: **3 issues** (DB설정, 암호화, OpenAI연동) — 모두 해결
- Code Quality Review: **1 issue** (빌드도구 Maven) — 해결
- Test Review: diagram produced, **30 gaps** identified → 전체 커버리지 계획
- Performance Review: **1 issue** (N+1) — JOIN 쿼리로 해결
- NOT in scope: written
- What already exists: written (없음 — 그린필드)
- TODOS.md updates: 0 items (모두 계획에 직접 반영)
- Failure modes: **1 critical gap** (스케줄러 보상 로직 → 계획에 반영)
- Outside voice: ran (Claude subagent) — 2개 주요 지적 반영 (PWA Push, Sprint 재분배)
- Lake Score: 7/7 recommendations chose complete option

---

## Verification

1. 백엔드: `mvn test` — 전체 단위/통합 테스트
2. 프론트: `npm test` — 컴포넌트/훅 테스트
3. 수동 테스트:
   - 회원가입 → 로그인 → AT 발급 확인
   - 미래 일기 작성 → 잠금 상태 확인 → 수정(24h 내) → 삭제
   - 챌린지 생성 → 참가 → 일기 작성 → 스케줄러 수동 실행 → 진행 확인
   - 프롬프트 랜덤 호출 → AI 프롬프트 (OpenAI 키 설정 시)

## GSTACK REVIEW REPORT

| Review | Trigger | Why | Runs | Status | Findings |
|--------|---------|-----|------|--------|----------|
| CEO Review | `/plan-ceo-review` | Scope & strategy | 0 | — | — |
| Codex Review | `/codex review` | Independent 2nd opinion | 0 | — | — |
| Eng Review | `/plan-eng-review` | Architecture & tests (required) | 1 | CLEAR | 7 issues, 1 critical gap (all resolved) |
| Design Review | `/plan-design-review` | UI/UX gaps | 0 | — | — |

- **OUTSIDE VOICE:** Claude subagent — 10개 지적, 2개 주요 반영 (PWA Push 추가, Sprint 5주 확장)
- **UNRESOLVED:** 0
- **VERDICT:** ENG CLEARED — ready to implement
