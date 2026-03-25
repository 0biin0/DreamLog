# DreamLog — 미래 일기 & 동기부여 플랫폼

> 미래의 나에게 보내는 편지를 작성하고, 챌린지를 통해 꾸준한 기록 습관을 만드는 웹 앱

## 핵심 기능

- **미래 일기** — 미래 날짜를 지정해 편지 작성, 해당 날짜까지 잠금, 24시간 내 수정 가능
- **챌린지 시스템** — 챌린지 생성/참가, 매일 자정 자동 정산 (15% 초과 미작성 시 실패)
- **AI 프롬프트** — 30개 큐레이션 프롬프트 + OpenAI 개인화 작성 도우미
- **PWA** — 설치 가능, Web Push 알림 지원

---

## 기술 스택

| Layer | Stack |
|-------|-------|
| **Backend (Production)** | Spring Boot 3, Java 17, Maven, PostgreSQL, Flyway |
| **Backend (Dev Server)** | Express.js, sql.js (SQLite) |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS |
| **상태 관리** | Zustand (인증), TanStack React Query v5 (서버 상태) |
| **폼 검증** | React Hook Form + Zod |
| **인증** | JWT (Access Token 15분 + Refresh Token 7일, Rotation) |
| **보안** | AES-256-GCM 일기 암호화, BCrypt 비밀번호 해싱 |
| **AI** | OpenAI API (gpt-4o-mini, 선택적 개인화) |

---

## 프로젝트 구조

```
dreamlog/
├── backend/                  # Spring Boot 3 (프로덕션 백엔드)
│   ├── auth/                 # JWT 인증 (가입/로그인/갱신/로그아웃)
│   ├── user/                 # 사용자 관리 + 30일 후 완전삭제 스케줄러
│   ├── diary/                # 미래 일기 CRUD + 날짜 잠금 + AES-256
│   ├── challenge/            # 챌린지 + 매일 자정 스케줄러 (보상 로직)
│   └── prompt/               # AI 프롬프트 (큐레이션 + OpenAI)
│
├── frontend/                 # React + Vite + TypeScript
│   ├── pages/                # Login, Signup, Home, Diary*, Challenge*
│   ├── components/           # layout/, diary/, challenge/, prompt/, common/
│   ├── api/                  # Axios 클라이언트 + API 모듈
│   ├── hooks/                # React Query 커스텀 훅
│   └── store/                # Zustand 인증 스토어
│
├── dev-server/               # Express.js 개발 서버 (Java 없이 실행)
├── docker-compose.yml        # PostgreSQL 16
└── docs/                     # 디자인 문서, 구현 계획서
```

---

## 빠른 시작

### 방법 1: 개발 서버 (Node.js만 필요)

```bash
# 백엔드 (Express + SQLite)
cd dev-server
npm install
node server.js
# → http://localhost:8080

# 프론트엔드
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### 방법 2: 프로덕션 (Java + Docker)

```bash
# PostgreSQL
docker-compose up -d

# 백엔드 (Spring Boot)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
# → http://localhost:8080

# 프론트엔드
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## 화면 구성

| 페이지 | 경로 | 설명 |
|--------|------|------|
| 로그인 | `/login` | 이메일 + 비밀번호 |
| 회원가입 | `/signup` | 이메일 + 비밀번호 + 닉네임 |
| 홈 대시보드 | `/` | 도착한 편지 + 참가 중인 챌린지 |
| 미래일기 쓰기 | `/diary/write` | AI 프롬프트 + 날짜 선택 + 작성 |
| 미래일기 목록 | `/diary/list` | 잠금/열림 상태, 페이지네이션 |
| 미래일기 상세 | `/diary/:id` | 잠금 카운트다운 / 열린 편지 내용 |
| 챌린지 목록 | `/challenges` | 내 챌린지 + 참가 가능 목록 |
| 챌린지 만들기 | `/challenges/create` | 기간, 시작일, 최대 인원 설정 |
| 챌린지 상세 | `/challenges/:id` | 진행 바, 일별 체크, 참가 버튼 |

---

## REST API

### 인증 `/api/auth`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/signup` | 회원가입 |
| POST | `/login` | 로그인 → JWT 발급 |
| POST | `/refresh` | Access Token 갱신 (Cookie RT) |
| POST | `/logout` | Refresh Token 무효화 |

### 사용자 `/api/users`
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/me` | 내 정보 조회 |
| PATCH | `/me` | 프로필 수정 |
| DELETE | `/me` | 탈퇴 (soft delete) |

### 미래 일기 `/api/diaries`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 일기 작성 |
| GET | `/` | 내 일기 목록 (페이징) |
| GET | `/:id` | 상세 (잠금 시 content=null) |
| PATCH | `/:id` | 수정 (24시간 내) |
| DELETE | `/:id` | 삭제 |
| GET | `/arrived` | 도착한 편지 목록 |

### 챌린지 `/api/challenges`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 챌린지 생성 |
| GET | `/` | 참가 가능 목록 |
| GET | `/:id` | 상세 |
| POST | `/:id/join` | 참가 |
| GET | `/:id/progress` | 내 진행상황 |
| GET | `/my` | 내 챌린지 |

### 프롬프트 `/api/prompts`
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/random` | 랜덤 프롬프트 (?category=MOTIVATION) |
| POST | `/ai` | AI 개인화 프롬프트 |

---

## 테스트

```bash
cd backend && mvn test
```

| 테스트 | 케이스 수 |
|--------|----------|
| AuthServiceTest | 10 |
| DiaryServiceTest | 11 |
| ChallengeServiceTest | 3 |
| ChallengeSchedulerTest | 5 |
| PromptServiceTest | 2 |
| AuthDiaryFlowTest (E2E) | 1 (10 assertions) |
| ChallengeFlowTest (E2E) | 2 |

---

## 환경 변수

```env
JWT_SECRET=your-256-bit-secret
ENCRYPTION_KEY=your-32-byte-aes-key
OPENAI_API_KEY=sk-your-key     # 선택 (없으면 fallback 프롬프트 사용)
```

---

## 구현 계획

| Sprint | 기간 | 내용 | 상태 |
|--------|------|------|------|
| 1 | 1주차 | 프로젝트 초기화 + JWT 인증 + Login/Signup | ✅ |
| 2 | 2주차 | 미래 일기 CRUD + 날짜 잠금 + AES-256 암호화 | ✅ |
| 3 | 3주차 | 챌린지 시스템 + 자정 스케줄러 + 보상 로직 | ✅ |
| 4 | 4주차 | AI 프롬프트 30개 + OpenAI 연동 + PWA | ✅ |
| 5 | 5주차 | E2E 통합 테스트 + 에러/로딩 UI + 마무리 | ✅ |

---

## 향후 계획 (P1/P2)

- 소셜 로그인 (Google, Kakao)
- 친구 추가 + 응원 기능
- 이메일/카카오 알림
- 유료 챌린지 (돈 걸기 — 법률 검토 후)
- 감정 태그 + 감정 분석
- 캘린더 뷰
- 다크 모드

---

## 문서

- `design-20260324-manifest.md` — 승인된 디자인 문서
- `implementation-plan.md` — 구현 계획서
- `request.md` — 원본 요구사항
