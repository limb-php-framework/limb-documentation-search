CREATE DATABASE "limb-docs-searcher";
\connect limb-docs-searcher;
CREATE SCHEMA "limb-docs-searcher";
SET search_path = 'limb-docs-searcher';

CREATE USER "limb-docs-searcher" UNENCRYPTED PASSWORD '123';

GRANT USAGE ON SCHEMA "limb-docs-searcher" TO "limb-docs-searcher";

CREATE TABLE files (id SERIAL, url TEXT PRIMARY KEY, header TEXT, content TEXT);
CREATE TABLE updates (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT NOW());

GRANT SELECT, UPDATE, INSERT ON ALL TABLES IN SCHEMA "limb-docs-searcher" TO "limb-docs-searcher";

GRANT all privileges ON SEQUENCE files_id_seq TO "limb-docs-searcher";
