CREATE USER limb_docs_searcher WITH PASSWORD 'qwerty';
CREATE DATABASE limb_docs_searcher;
GRANT ALL PRIVILEGES ON DATABASE limb_docs_searcher to limb_docs_searcher;
CREATE TABLE files (id SERIAL, url TEXT PRIMARY KEY, header TEXT, content TEXT);
CREATE TABLE updates (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT NOW());
