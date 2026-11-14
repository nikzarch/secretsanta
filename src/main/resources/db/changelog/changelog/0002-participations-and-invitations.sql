-- liquibase formatted sql

-- changeset secretsanta:0002-participation-invitations

ALTER TABLE event_participants
    ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING',
    ADD COLUMN requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN decided_at TIMESTAMPTZ;

UPDATE event_participants
SET status     = 'ACCEPTED',
    decided_at = requested_at;

ALTER TABLE event_participants
    ADD CONSTRAINT chk_event_participants_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));

ALTER TABLE event_participants
    ADD CONSTRAINT chk_event_participants_decision
        CHECK (
            (status = 'PENDING' AND decided_at IS NULL)
                OR (status IN ('ACCEPTED', 'REJECTED') AND decided_at IS NOT NULL)
            );

CREATE INDEX idx_event_participants_event_status
    ON event_participants (event_id, status);

ALTER TABLE secret_santa_events
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE UNIQUE INDEX idx_wishlists_owner_unique
    ON wishlists (owner_id);

CREATE TABLE registration_tokens(
    token     TEXT NOT NULL,
    for_username TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    UNIQUE (token, for_username)
)