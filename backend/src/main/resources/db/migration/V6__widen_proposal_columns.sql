-- Widen author column to accommodate longer rapporteur descriptions from the Camara API
ALTER TABLE legislative_proposal ALTER COLUMN author TYPE TEXT;
