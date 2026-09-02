CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT         NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    provider        VARCHAR(30)    NOT NULL,
    provider_ref    VARCHAR(100)   NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT uq_payments_provider_ref UNIQUE (provider_ref),
    CONSTRAINT ck_payments_status CHECK (status IN ('CREATED', 'SUCCESS', 'FAILED'))
);

CREATE INDEX idx_payments_booking_id ON payments (booking_id);
