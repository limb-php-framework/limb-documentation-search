CREATE TABLE files (id SERIAL, url TEXT PRIMARY KEY, header TEXT, content TEXT);
CREATE TABLE updates (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT NOW());
INSERT INTO updates (id, timestamp) VALUES (1, ('1970-01-01 00:00:00+00'::timestamptz));
