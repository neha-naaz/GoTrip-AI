# GoTrip-AI (Tripflow)

Group travel marketplace: agencies publish trips, travelers book and pay a deposit, confirmed members join trip group chat.

## Stack

- Backend: Java 21, Spring Boot 3.5, Postgres, Flyway, JWT, STOMP WebSocket
- Frontend: React + Vite + TypeScript

## Run with Docker Compose (recommended)

Starts **Postgres** + **API**. Frontend stays on Vite for Day 17.

```bash
# If you previously ran a manual Postgres container on :5433, stop it first:
# docker stop tripflow-postgres

docker compose up --build
```

- API: http://localhost:8080
- Postgres on host: `localhost:5433` (user/db/password: `tripflow`)

Frontend (separate terminal):

```bash
cd frontend
npm install
npm run dev
```

Set `VITE_API_BASE_URL=http://localhost:8080` in `frontend/.env` if needed.

## Run backend without Compose

```bash
docker start tripflow-postgres   # or use compose db only
./mvnw spring-boot:run
```

## Smoke

```bash
curl http://localhost:8080/api/trips
```

Expect `200` and a JSON array.
