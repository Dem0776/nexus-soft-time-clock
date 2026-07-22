# =====================================================================
# Portal web Angular — build con Node y servido por NGINX.
# Contexto de build: raíz del repo (usa web/).
# =====================================================================
FROM node:22-alpine AS build
WORKDIR /app
COPY web/package.json web/package-lock.json* ./
RUN npm ci || npm install
COPY web/ ./
RUN npm run build

# --- Runtime ---------------------------------------------------------
FROM nginx:1.27-alpine AS runtime
# Config con fallback SPA (evita 404 en enlaces directos/refresh de rutas Angular)
COPY infra/web-nginx.conf /etc/nginx/conf.d/default.conf
# Angular >=17 emite a dist/<app>/browser
COPY --from=build /app/dist/nexus-web/browser /usr/share/nginx/html
EXPOSE 80
