FROM docker.io/library/node:24.22.3-alpine AS build
WORKDIR /app/frontend
RUN npm install --global pnpm@12.21.0
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml frontend/.npmrc ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
COPY img/ /app/img/
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN pnpm build

FROM docker.io/library/nginx:stable-alpine AS runtime
ENV API_UPSTREAM=http://host.docker.internal:8080
COPY frontend/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/frontend/dist/ /usr/share/nginx/html/
EXPOSE 80
