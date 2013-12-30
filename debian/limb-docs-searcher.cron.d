 0 0 * * *  curl --data "token=`cat /etc/limb-docs-searcher/application.conf|grep token|awk '{print $3}'|sed 's/"//g'`" http://localhost:`cat /etc/default/limb-docs-searcher|grep PORT|sed 's/PORT=//g'`/update -X POST
 0 5 * * *  indexer --config /etc/limb-docs-searcher/sphinx.conf --all --rotate
