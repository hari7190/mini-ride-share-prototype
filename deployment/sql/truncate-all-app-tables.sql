-- Full application data reset for mini-ride-share (single DB: ride_share_db).
-- Tables match JPA entities: users, drivers, driver_locations, trips, ride_dispatch.
--
-- Usage (example):
--   mysql -h "$MYSQL_HOST" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < deployment/sql/truncate-all-app-tables.sql
--   mysql ... -e "source deployment/sql/truncate-all-app-tables.sql"

USE ride_share_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE ride_dispatch;
TRUNCATE TABLE trips;
TRUNCATE TABLE driver_locations;
TRUNCATE TABLE drivers;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;
