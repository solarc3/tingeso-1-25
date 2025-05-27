FROM oven/bun:latest AS builder
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install
COPY . .
ENV NODE_ENV=production
RUN bun run build

FROM nginx:stable-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN rm -f /etc/nginx/conf.d/default.conf.bak
RUN ls -la /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
