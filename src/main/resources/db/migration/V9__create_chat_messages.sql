CREATE TABLE chat_messages (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT         NOT NULL,
    sender_user_id  BIGINT         NOT NULL,
    content         VARCHAR(1000)  NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_chat_messages_group FOREIGN KEY (group_id) REFERENCES trip_groups (id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_user_id) REFERENCES users (id)
);

CREATE INDEX idx_chat_messages_group_id ON chat_messages (group_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages (group_id, created_at);
