FROM nginx:alpine
ARG TARGETARCH
RUN test "$TARGETARCH" = "arm64"
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
