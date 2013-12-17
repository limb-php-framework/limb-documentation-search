 0 0 * * sun curl -X POST http://0.0.0.0:9000/update?token=qwerty
 0 5 * * sun indexer --config /etc/limb-docs-indexer/sphinx.conf --all --rotate
