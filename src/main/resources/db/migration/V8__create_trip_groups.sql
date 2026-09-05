CREATE TABLE trip_groups (
    id          BIGSERIAL PRIMARY KEY,
    trip_id     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_trip_groups_trip_id UNIQUE (trip_id),
    CONSTRAINT fk_trip_groups_trip FOREIGN KEY (trip_id) REFERENCES trips (id)
);

CREATE TABLE group_members (
    id          BIGSERIAL PRIMARY KEY,
    group_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    booking_id  BIGINT       NOT NULL,
    joined_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_group_members_booking_id UNIQUE (booking_id),
    CONSTRAINT uq_group_members_group_user UNIQUE (group_id, user_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES trip_groups (id),
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_group_members_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

CREATE INDEX idx_group_members_group_id ON group_members (group_id);
CREATE INDEX idx_group_members_user_id ON group_members (user_id);
