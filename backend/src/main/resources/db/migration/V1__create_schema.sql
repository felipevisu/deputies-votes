CREATE TABLE deputy (
    id       BIGSERIAL    PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    party    VARCHAR(50)  NOT NULL,
    state    VARCHAR(2)   NOT NULL,
    avatar   VARCHAR(512),
    followed BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE legislative_proposal (
    id        BIGSERIAL    PRIMARY KEY,
    title     VARCHAR(500) NOT NULL,
    summary   TEXT         NOT NULL,
    author    VARCHAR(255) NOT NULL,
    category  VARCHAR(100) NOT NULL,
    vote_date DATE         NOT NULL
);

CREATE TABLE deputy_vote (
    id          BIGSERIAL   PRIMARY KEY,
    deputy_id   BIGINT      NOT NULL REFERENCES deputy(id),
    proposal_id BIGINT      NOT NULL REFERENCES legislative_proposal(id),
    vote        VARCHAR(20) NOT NULL,
    UNIQUE(deputy_id, proposal_id)
);

CREATE INDEX idx_deputy_followed ON deputy(followed);
CREATE INDEX idx_deputy_vote_deputy_id ON deputy_vote(deputy_id);
CREATE INDEX idx_deputy_vote_proposal_id ON deputy_vote(proposal_id);
CREATE INDEX idx_legislative_proposal_vote_date ON legislative_proposal(vote_date DESC);
