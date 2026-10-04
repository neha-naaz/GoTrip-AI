ALTER TABLE trip_itineraries DROP CONSTRAINT ck_trip_itineraries_day;
ALTER TABLE trip_itineraries ADD CONSTRAINT ck_trip_itineraries_day CHECK (day_number >= 0);
