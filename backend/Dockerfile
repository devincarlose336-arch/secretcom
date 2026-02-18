FROM node:18-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --production

COPY src/ ./src/

ENV NODE_ENV=production

EXPOSE 3000

CMD ["node", "src/server.js"]
