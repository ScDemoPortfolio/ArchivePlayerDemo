FROM node:20-alpine as build
WORKDIR /app/frontend
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
COPY package.json ./
COPY package-lock.json ./
RUN npm install
COPY . .
ENV CI=false
ENV NODE_ENV=production
RUN npm run build

FROM nginx:alpine
RUN apk add --no-cache curl
RUN addgroup -S nginxuser && adduser -S nginxuser -G nginxuser
USER nginxuser
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/frontend/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=10s --retries=5 CMD curl -f http://localhost/ || exit 1
# FIX: Generate config in /tmp and use it directly, avoiding permission issues with /etc/nginx
CMD ["/bin/sh", "-c", "envsubst '$$NGINX_BACKEND_HOST' < /etc/nginx/nginx.conf > /tmp/nginx_generated.conf && nginx -c /tmp/nginx_generated.conf -g 'daemon off;'"]