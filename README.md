# GoTrip-AI (Tripflow)

Group travel marketplace: agencies publish trips, travelers book and pay a deposit, confirmed members join trip group chat.

## Stack

- Backend: Java 21, Spring Boot 3.5, Postgres, Flyway, JWT, STOMP WebSocket
- Frontend: React + Vite + TypeScript (nginx in Docker)

## Run full stack with Docker Compose

```bash
# Stop any old manual Postgres container on :5433 if needed:
# docker stop tripflow-postgres

docker compose up --build
```

| Service | URL |
|---------|-----|
| Web UI | http://localhost:3000 |
| API | http://localhost:8080 |
| Postgres (host) | localhost:5433 (`tripflow` / `tripflow`) |

The browser talks to the API at `http://localhost:8080` (baked into the FE image at build time). Containers talk to each other on the Compose network (`db`, `api`).

## Frontend local dev (hot reload)

```bash
docker compose up db api
cd frontend && npm install && npm run dev
```

Vite: http://localhost:5173 — set `VITE_API_BASE_URL=http://localhost:8080` in `frontend/.env` if needed.

## Smoke

```bash
curl http://localhost:8080/api/trips
curl -I http://localhost:3000
```

Expect API `200` JSON and web `200` HTML.
