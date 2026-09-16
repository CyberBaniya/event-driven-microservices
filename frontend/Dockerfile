FROM nginx:1.27-alpine
COPY index.html /usr/share/nginx/html/index.html
COPY style.css  /usr/share/nginx/html/style.css
COPY js/        /usr/share/nginx/html/js/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
