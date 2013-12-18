 0 0 * * sun curl --data "token=`cat /etc/limb-docs-searcher/application.conf|grep token|awk '{print $3}'|sed 's/"//g'`" http://0.0.0.0:9000/update -X POST
 0 5 * * sun indexer --config /etc/limb-docs-indexer/sphinx.conf --all --rotate
