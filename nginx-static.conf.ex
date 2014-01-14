server {
       server_name i.limb-docs-searcher.dev;
       location / {
                root         /usr/share/limb-docs-searcher-static/public/;
                access_log   off;
       }
}
