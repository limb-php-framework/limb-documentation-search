SET search_path = "limb-docs-searcher";

INSERT INTO updates (id, timestamp) VALUES (1, (SELECT TIMESTAMP 'epoch'));
