upstream limbdocssearcher {
         server 127.0.0.1:9000;
}

server {
       access_log /var/log/limb-docs-searcher/nginx.access.log;
       error_log  /var/log/limb-docs-searcher/nginx.error.log;
       proxy_buffering    off;
       proxy_set_header   X-Real-IP $remote_addr;
       proxy_set_header   X-Scheme $scheme;
       proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header   Host $http_host;
       listen 80;

       location /assets/* {
               root         /usr/share/limb-docs-searcher-static/public/;
               access_log   off;
       }
       location / {
               proxy_pass  http://limbdocssearcher;
               proxy_read_timeout 5s;
       }
}
