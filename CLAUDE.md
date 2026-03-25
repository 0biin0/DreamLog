# DreamLog

## Project Structure
- `backend/` — Spring Boot 3 (Java 17, Maven)
- `frontend/` — React 18 + TypeScript + Vite

## Quick Commands
- Backend: `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local`
- Frontend: `cd frontend && npm install && npm run dev`
- DB: `docker-compose up -d`
- Backend tests: `cd backend && mvn test`

## Conventions
- All dates use KST (Asia/Seoul timezone)
- Diary content is AES-256-GCM encrypted (title is plaintext)
- JWT: AT 15min, RT 7days with rotation
- REST API prefix: /api/
- Flyway migrations in src/main/resources/db/migration/
- Package structure: feature-based (auth/, user/, diary/, challenge/, prompt/)
- Soft delete with deleted_at column, 30-day cleanup scheduler

## Key Patterns
- ApiResponse<T> wrapper for all endpoints
- BusinessException + ErrorCode enum for error handling
- @AuthenticationPrincipal UserDetails for user context
- React Query for server state, Zustand for auth state
- Axios interceptors for auto AT refresh on 401

## Schedulers
- ChallengeScheduler: daily KST 00:05 — diary check + compensation logic
- UserCleanupScheduler: daily KST 03:00 — permanent delete after 30 days
