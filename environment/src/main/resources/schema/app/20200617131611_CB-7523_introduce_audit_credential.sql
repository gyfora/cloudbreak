-- // CB-7523 introduce audit credential
-- Migration SQL that makes the change goes here.

ALTER TABLE credential ADD COLUMN IF NOT EXISTS type VARCHAR(255);
UPDATE credential SET type='ENVIRONMENT';

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE environment DROP COLUMN IF EXISTS type;
