-- Drop all application tables in ride_share_db (schema recreated by Hibernate on service restart).
-- Tables match JPA entities: users, drivers, driver_locations, trips, ride_dispatch.
--
-- Usage (example):
--   mysql -h "$MYSQL_HOST" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < deployment/sql/drop-all-app-tables.sql
--
-- After running, restart JPA services so ddl-auto: update recreates tables:
--   kubectl rollout restart deployment -n <namespace> auth-service driver-service rider-service dispatch-engine location-tracker

USE ride_share_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ride_dispatch;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS driver_locations;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;
