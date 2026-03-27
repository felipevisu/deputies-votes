CREATE TABLE account (
    id        BIGSERIAL    PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE account_deputy_follow (
    account_id BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    deputy_id  BIGINT NOT NULL REFERENCES deputy(id) ON DELETE CASCADE,
    PRIMARY KEY (account_id, deputy_id)
);

CREATE INDEX idx_account_deputy_follow_account ON account_deputy_follow(account_id);
CREATE INDEX idx_account_deputy_follow_deputy ON account_deputy_follow(deputy_id);

-- Seed default account with same follows as before (deputies 1,2,4,6,8)
INSERT INTO account (id, name, last_name, email) VALUES
(1, 'Default', 'User', 'user@deolhoneles.com');

INSERT INTO account_deputy_follow (account_id, deputy_id) VALUES
(1, 1), (1, 2), (1, 4), (1, 6), (1, 8);

SELECT setval('account_id_seq', (SELECT MAX(id) FROM account));

-- Remove followed column from deputy (now managed via account relationship)
DROP INDEX idx_deputy_followed;
ALTER TABLE deputy DROP COLUMN followed;
