server {
       server_name limbdocssearcherstatic;
       location / {
                root         /usr/share/limb-docs-searcher-static/public/;
                access_log   off;
       }
}