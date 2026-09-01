CREATE TABLE bookings (
    id            BIGSERIAL PRIMARY KEY,
    trip_id       BIGINT          NOT NULL,
    user_id       BIGINT          NOT NULL,
    status        VARCHAR(30)     NOT NULL,
    amount_due    NUMERIC(12, 2)  NOT NULL,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bookings_trip FOREIGN KEY (trip_id) REFERENCES trips (id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_bookings_status CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'EXPIRED'))
);

CREATE UNIQUE INDEX uq_bookings_active_user_trip
    ON bookings (trip_id, user_id)
    WHERE status IN ('PENDING_PAYMENT', 'CONFIRMED');

CREATE INDEX idx_bookings_trip_id ON bookings (trip_id);
CREATE INDEX idx_bookings_user_id ON bookings (user_id);
