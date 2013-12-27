server {
       server_name limbdocssearcherstatic;
       location /assets/* {
                root         /usr/share/limb-docs-searcher-static/public/;
                access_log   off;
       }
}