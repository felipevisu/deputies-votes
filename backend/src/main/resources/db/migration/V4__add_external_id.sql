ALTER TABLE deputy ADD COLUMN external_id BIGINT UNIQUE;
ALTER TABLE legislative_proposal ADD COLUMN external_id VARCHAR(100) UNIQUE;
