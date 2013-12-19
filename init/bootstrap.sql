CREATE SCHEMA "limb-docs-searcher";
SET search_path = 'limb-docs-searcher';

CREATE USER "limb-docs-searcher" UNENCRYPTED PASSWORD '123';

GRANT USAGE ON SCHEMA "limb-docs-searcher" TO "limb-docs-searcher";

CREATE TABLE files (id SERIAL, url TEXT PRIMARY KEY, header TEXT, content TEXT);
CREATE TABLE updates (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT NOW());
