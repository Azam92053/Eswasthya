# Build the React app with Node and serve it with Nginx.
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json ./
COPY package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
