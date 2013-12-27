 0 0 * * *  curl --data "token=`cat /etc/limb-docs-searcher/application.conf|grep token|awk '{print $3}'|sed 's/"//g'`" http://0.0.0.0:9000/update -X POST
 0 5 * * *  indexer --config /etc/limb-docs-searcher/sphinx.conf --all --rotate
