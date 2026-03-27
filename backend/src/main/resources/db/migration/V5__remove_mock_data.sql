-- Remove seed/mock data: deputies and proposals that have no external_id
-- (real data synced from the Camara API always has an external_id)

-- Remove votes that reference mock deputies or mock proposals
DELETE FROM deputy_vote
WHERE deputy_id IN (SELECT id FROM deputy WHERE external_id IS NULL)
   OR proposal_id IN (SELECT id FROM legislative_proposal WHERE external_id IS NULL);

-- Remove follow relationships to mock deputies
DELETE FROM account_deputy_follow
WHERE deputy_id IN (SELECT id FROM deputy WHERE external_id IS NULL);

-- Remove mock proposals
DELETE FROM legislative_proposal WHERE external_id IS NULL;

-- Remove mock deputies
DELETE FROM deputy WHERE external_id IS NULL;
