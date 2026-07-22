# =====================================================================
# Reverse proxy NGINX — imagen con la config horneada.
# Se construye en vez de bind-mount para no depender de rutas relativas
# del host (Portainer resuelve ./ desde la raíz del repo clonado).
# Contexto de build: raíz del repo.
# =====================================================================
FROM nginx:1.27-alpine
COPY infra/nginx/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
