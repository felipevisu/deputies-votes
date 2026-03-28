-- Rename legislative_proposal table to legislative_activity
ALTER TABLE legislative_proposal RENAME TO legislative_activity;

-- Rename the foreign key column in deputy_vote
ALTER TABLE deputy_vote RENAME COLUMN proposal_id TO activity_id;
