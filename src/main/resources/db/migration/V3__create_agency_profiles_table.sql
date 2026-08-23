CREATE TABLE agency_profiles (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT       NOT NULL,
    agency_name           VARCHAR(150) NOT NULL,
    description           TEXT,
    verification_status   VARCHAR(20)  NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_agency_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_agency_profiles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_agency_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX idx_agency_profiles_verification_status ON agency_profiles (verification_status);