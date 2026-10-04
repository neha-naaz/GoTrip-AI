# GoTrip-AI (Tripflow)

Group travel marketplace: agencies publish trips, travelers book and pay a deposit, confirmed members join trip group chat.

## Stack

- Backend: Java 21, Spring Boot 3.5, Postgres, Flyway, JWT, STOMP WebSocket
- Frontend: React + Vite + TypeScript (nginx in Docker)
- Tests: JUnit + MockMvc + Zonky embedded Postgres

## Architecture (short)

Modular monolith under `com.tripflow.*`:

`auth` → `agency` / `user` → `trip` → `booking` → `payment` → `group` → `chat`

Happy path:

```text
Agency draft + content → publish → customer books (seat hold)
→ sandbox/webhook pay → CONFIRMED → group membership → chat
```

## Run full stack with Docker Compose

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Web UI | http://localhost:3000 |
| API | http://localhost:8080 |
| Health | http://localhost:8080/api/health |
| Postgres (host) | localhost:5433 (`tripflow` / `tripflow`) |

Copy `.env.example` / `frontend/.env.example` when you need local overrides. Do not commit real secrets.

## Frontend local dev (hot reload)

```bash
docker compose up db api
cd frontend && npm install && npm run dev
```

Vite: http://localhost:5173 — set `VITE_API_BASE_URL=http://localhost:8080` in `frontend/.env` if needed.

## Demo walkthrough (local)

Local/compose sets `tripflow.demo.auto-verify-agencies=true` so agencies can publish without an admin step.

1. **Register agency** → Create trip → Edit content (basics + itinerary) → Publish  
2. **Register customer** → Explore trips (optional date filter) → Book  
3. **My bookings** → Pay booking amount → Open group chat  
4. As agency → Travelers roster on the trip  

Demo accounts (create via Register UI or `POST /api/auth/register`):

| Role | Email | Password |
|------|-------|----------|
| Agency | `demo.agency@tripflow.local` | `password1` |
| Customer | `demo.customer@tripflow.local` | `password1` |

## Payments

`PaymentProvider` is swappable via `tripflow.payment.provider` / `TRIPFLOW_PAYMENT_PROVIDER`:

| Value | Behavior |
|-------|----------|
| `mock` (default) | Local/demo. UI calls `POST /api/bookings/{id}/sandbox-confirm`. Tests use `POST /api/payments/webhook` + `X-Tripflow-Webhook-Secret`. |
| `razorpay` | Creates a Razorpay Order on pay; browser opens Checkout; success confirmed via `POST /api/bookings/{id}/confirm-checkout` (signature) and/or `POST /api/payments/razorpay/webhook`. |

Razorpay env (test keys from the [Razorpay Dashboard](https://dashboard.razorpay.com/)):

```bash
TRIPFLOW_PAYMENT_PROVIDER=razorpay
TRIPFLOW_RAZORPAY_KEY_ID=rzp_test_...
TRIPFLOW_RAZORPAY_KEY_SECRET=...
TRIPFLOW_RAZORPAY_WEBHOOK_SECRET=...   # optional but recommended for dashboard webhooks
```

Sandbox confirm is rejected when the active provider is Razorpay.

## What’s mocked

- **Payments (default):** `MockPaymentProvider` for local/CI without gateway keys.
- **Agency verification:** auto-verified in local/demo; set `TRIPFLOW_DEMO_AUTO_VERIFY_AGENCIES=false` for production-like behavior.

## Intentionally locked

After publish (especially with bookings): no unpublish / free edit of itinerary, inclusions, or exclusions. Drafts remain fully editable.

## Tests

```bash
./mvnw test
```

Critical-path ITs: register → publish → book → pay → group member, capacity conflict, webhook idempotency, cancel pending booking.

CI runs backend tests + frontend build on push/PR (`.github/workflows/ci.yml`).

## Smoke

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/trips
curl -I http://localhost:3000
```

Expect health `{"status":"UP"}`, trips JSON, and web `200` HTML.

## Deploy notes

When hosting, set at least:

- `SPRING_DATASOURCE_*` → managed Postgres  
- `TRIPFLOW_JWT_SECRET` → long random secret  
- `TRIPFLOW_PAYMENT_WEBHOOK_SECRET`  
- `TRIPFLOW_CORS_ALLOWED_ORIGINS` → your public frontend origin(s)  
- `TRIPFLOW_DEMO_AUTO_VERIFY_AGENCIES=false`  
- Frontend build arg / env: `VITE_API_BASE_URL` → public API URL  
