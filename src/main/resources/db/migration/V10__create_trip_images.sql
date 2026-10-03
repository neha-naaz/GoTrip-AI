CREATE TABLE trip_images (
    id           BIGSERIAL PRIMARY KEY,
    trip_id      BIGINT       NOT NULL,
    source_type  VARCHAR(20)  NOT NULL,
    url          TEXT         NOT NULL,
    sort_order   INT          NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_trip_images_trip
        FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT uq_trip_images_trip_sort UNIQUE (trip_id, sort_order),
    CONSTRAINT ck_trip_images_source_type
        CHECK (source_type IN ('UPLOAD', 'URL', 'WEB', 'AI')),
    CONSTRAINT ck_trip_images_sort_order CHECK (sort_order >= 0)
);

CREATE INDEX idx_trip_images_trip_id ON trip_images (trip_id);
