CREATE TABLE trip_itineraries (
    id           BIGSERIAL PRIMARY KEY,
    trip_id      BIGINT       NOT NULL,
    day_number   INT          NOT NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_trip_itineraries_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT uq_trip_itineraries_trip_day UNIQUE (trip_id, day_number),
    CONSTRAINT ck_trip_itineraries_day CHECK (day_number > 0)
);

CREATE TABLE trip_inclusions (
    id           BIGSERIAL  PRIMARY KEY,
    trip_id      BIGINT       NOT NULL,
    description  VARCHAR(500) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_trip_inclusions_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE
);

CREATE TABLE trip_exclusions (
    id           BIGSERIAL  PRIMARY KEY,
    trip_id      BIGINT       NOT NULL,
    description  VARCHAR(500) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_trip_exclusions_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE
);

CREATE INDEX idx_trip_itineraries_trip_id ON trip_itineraries (trip_id);
CREATE INDEX idx_trip_inclusions_trip_id ON trip_inclusions (trip_id);
CREATE INDEX idx_trip_exclusions_trip_id ON trip_exclusions (trip_id);