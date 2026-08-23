 CREATE TABLE trips (
     id              BIGSERIAL PRIMARY KEY,
     agency_id       BIGINT         NOT NULL,
     title           VARCHAR(200)   NOT NULL,
     description     TEXT,
     source          VARCHAR(100)   NOT NULL,
     destination     VARCHAR(100)   NOT NULL,
     start_date      DATE           NOT NULL,
     end_date        DATE           NOT NULL,
     price           NUMERIC(12, 2) NOT NULL,
     booking_amount  NUMERIC(12, 2) NOT NULL,
     capacity        INT            NOT NULL,
     status          VARCHAR(20)    NOT NULL,
     created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
     updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

     CONSTRAINT fk_trips_agency         FOREIGN KEY (agency_id) REFERENCES agency_profiles (id),
     CONSTRAINT ck_trips_status         CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),
     CONSTRAINT ck_trips_dates          CHECK (end_date >= start_date),
     CONSTRAINT ck_trips_capacity       CHECK (capacity > 0),
     CONSTRAINT ck_trips_price          CHECK (price > 0),
     CONSTRAINT ck_trips_booking_amount CHECK (booking_amount > 0 AND booking_amount <= price)
 );

 CREATE INDEX idx_trips_agency_id ON trips (agency_id);
 CREATE INDEX idx_trips_status_start_date ON trips (status, start_date);
 CREATE INDEX idx_trips_source_destination ON trips (source, destination);