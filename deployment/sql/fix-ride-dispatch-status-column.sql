-- ride_dispatch.status was created as a MySQL ENUM before FAILED was added to DispatchStatus.
-- Hibernate ddl-auto: update does not extend ENUM values, so saving FAILED causes:
--   Data truncated for column 'status' at row 1
--
-- Usage:
--   mysql -h "$MYSQL_HOST" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < deployment/sql/fix-ride-dispatch-status-column.sql

USE ride_share_db;

ALTER TABLE ride_dispatch
  MODIFY COLUMN status VARCHAR(32) NOT NULL;
