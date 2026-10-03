-- Optional demo seed for a fresh database.
-- Prefer registering via UI when tripflow.demo.auto-verify-agencies=true.
--
-- Usage (compose Postgres on host :5433):
--   docker compose exec -T db psql -U tripflow -d tripflow < scripts/seed-demo.sql
--
-- Or against the named container:
--   docker exec -i tripflow-postgres psql -U tripflow -d tripflow < scripts/seed-demo.sql

-- This script only documents the intended demo accounts.
-- Password for both: password1
--
-- Agency:  demo.agency@tripflow.local  (VERIFIED via auto-verify on register)
-- Customer: demo.customer@tripflow.local
--
-- Create them with:
--   POST /api/auth/register  {"name":"Demo Agency","email":"demo.agency@tripflow.local","password":"password1","role":"AGENCY","agencyName":"Demo Adventures"}
--   POST /api/auth/register  {"name":"Demo Traveler","email":"demo.customer@tripflow.local","password":"password1","role":"CUSTOMER"}

SELECT 'Use /api/auth/register — see README demo walkthrough' AS hint;
