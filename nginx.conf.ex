upstream limbdocssearcher {
         server 127.0.0.1:9000;
}

server {
       server_name limb-docs-searcher.dev;
       listen 80;

       access_log /var/log/limb-docs-searcher/limb-docs-searcher.access.log;
       error_log  /var/log/limb-docs-searcher/limb-docs-searcher.error.log;

       proxy_buffering    off;

       proxy_set_header   X-Real-IP $remote_addr;
       proxy_set_header   X-Scheme $scheme;
       proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header   Host $http_host;


       location / {
               proxy_pass  http://limbdocssearcher;
               proxy_read_timeout 5s;
       }
}
