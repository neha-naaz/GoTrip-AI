CREATE TABLE bookings(
    id            BIGSERIAL       NOT NULL,
    trip_id       BIGINT          NOT NULL,
    user_id       BIGINT          NOT NULL,
    status        VARCHAR(30)     NOT NULL,
    due_amount    NUMERIC(12, 2)  NOT NULL,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bookings_trip FOREIGN_KEY (trip_id) REFERENCES trips (id),
    CONSTRAINT fk_bookings_user FOREIGN_KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_booking_status CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'EXPIRED'));
);

-- One active booking per user per trip (not cancelled/expired)
CREATE UNIQUE INDEX uq_bookings_active_per_user ON bookings (trip_id, user_id) WHERE status IN ('PENDING_PAYMENT', 'CONFIRMED');

CREATE INDEX idx_bookings_trip_id ON bookings (trip_id);
CREATE INDEX idx_bookings_user_id ON bookings (user_id);