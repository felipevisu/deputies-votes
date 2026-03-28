CREATE TABLE event (
    id              BIGSERIAL PRIMARY KEY,
    external_id     BIGINT NOT NULL UNIQUE,
    event_type      VARCHAR(100) NOT NULL,
    description     TEXT,
    agenda_summary  TEXT,
    situation       VARCHAR(50),
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    location        VARCHAR(500),
    organ_code      VARCHAR(20),
    organ_name      VARCHAR(500),
    video_url       VARCHAR(500),
    event_date      DATE NOT NULL
);

CREATE INDEX idx_event_date ON event(event_date DESC);
CREATE INDEX idx_event_organ_code ON event(organ_code);

CREATE TABLE event_deputy (
    event_id    BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    deputy_id   BIGINT NOT NULL REFERENCES deputy(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, deputy_id)
);

CREATE INDEX idx_event_deputy_deputy_id ON event_deputy(deputy_id);
