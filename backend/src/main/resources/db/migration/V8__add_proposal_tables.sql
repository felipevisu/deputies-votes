CREATE TABLE proposal (
    id                  BIGSERIAL     PRIMARY KEY,
    external_id         BIGINT        UNIQUE NOT NULL,
    type_code           VARCHAR(20)   NOT NULL,
    number              INTEGER       NOT NULL,
    year                INTEGER       NOT NULL,
    ementa              TEXT          NOT NULL,
    keywords            TEXT,
    presentation_date   DATE          NOT NULL,
    status_description  VARCHAR(500)
);

CREATE TABLE proposal_author (
    proposal_id   BIGINT  NOT NULL REFERENCES proposal(id) ON DELETE CASCADE,
    deputy_id     BIGINT  NOT NULL REFERENCES deputy(id) ON DELETE CASCADE,
    signing_order INTEGER NOT NULL DEFAULT 1,
    proponent     BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (proposal_id, deputy_id)
);

CREATE INDEX idx_proposal_date ON proposal(presentation_date DESC);
CREATE INDEX idx_proposal_author_deputy ON proposal_author(deputy_id);
