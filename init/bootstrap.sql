CREATE SCHEMA "limb-docs-searcher";
SET search_path = 'limb-docs-searcher';

CREATE USER "limb-docs-searcher" UNENCRYPTED PASSWORD '123';
ALTER USER "limb-docs-searcher" SET search_path = 'limb-docs-searcher';

GRANT USAGE ON SCHEMA "limb-docs-searcher" TO "limb-docs-searcher";

CREATE TABLE files (id SERIAL, url TEXT PRIMARY KEY, header1 TEXT, header2 TEXT, header3 TEXT, header4 TEXT, header5 TEXT, header6 TEXT, content TEXT);

GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA "limb-docs-searcher" TO "limb-docs-searcher";

GRANT ALL PRIVILEGES ON SEQUENCE files_id_seq TO "limb-docs-searcher";
