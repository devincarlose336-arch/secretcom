# Backend Deployment Guide

## Option 1: Docker Deployment

### Build and Run
```bash
cd backend
docker build -t secretcom-backend .
docker run -d \
  -p 3000:3000 \
  -e MONGODB_URI=mongodb://your-mongodb-host:27017/secretcom \
  -e REDIS_URL=redis://your-redis-host:6379 \
  -e JWT_SECRET=your-production-secret \
  -e JWT_REFRESH_SECRET=your-refresh-secret \
  -e NODE_ENV=production \
  --name secretcom-backend \
  secretcom-backend
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      - MONGODB_URI=mongodb://mongo:27017/secretcom
      - REDIS_URL=redis://redis:6379
      - JWT_SECRET=${JWT_SECRET}
      - JWT_REFRESH_SECRET=${JWT_REFRESH_SECRET}
      - NODE_ENV=production
    depends_on:
      - mongo
      - redis

  mongo:
    image: mongo:7
    volumes:
      - mongo_data:/data/db
    ports:
      - "27017:27017"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  mongo_data:
```

## Option 2: AWS EC2 Deployment

### 1. Launch EC2 Instance
- Ubuntu 22.04 LTS, t3.medium or larger
- Open ports: 22 (SSH), 80 (HTTP), 443 (HTTPS), 3000 (API)

### 2. Install Dependencies
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs mongodb-org redis-server nginx certbot
```

### 3. Setup with PM2
```bash
npm install -g pm2
cd /opt/secretcom/backend
npm install --production
pm2 start src/server.js --name secretcom
pm2 save
pm2 startup
```

### 4. Nginx Reverse Proxy
```nginx
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_cache_bypass $http_upgrade;
    }
}
```

### 5. SSL Certificate
```bash
sudo certbot --nginx -d api.yourdomain.com
```

## Option 3: Google Cloud Run

```bash
gcloud builds submit --tag gcr.io/PROJECT_ID/secretcom-backend
gcloud run deploy secretcom-backend \
  --image gcr.io/PROJECT_ID/secretcom-backend \
  --platform managed \
  --allow-unauthenticated \
  --set-env-vars "MONGODB_URI=..." \
  --set-env-vars "NODE_ENV=production"
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| PORT | Server port (default: 3000) | No |
| MONGODB_URI | MongoDB connection string | Yes |
| REDIS_URL | Redis connection string | No |
| JWT_SECRET | JWT signing secret | Yes |
| JWT_REFRESH_SECRET | Refresh token secret | Yes |
| JWT_EXPIRY | Access token expiry (default: 1h) | No |
| JWT_REFRESH_EXPIRY | Refresh token expiry (default: 7d) | No |
| TURN_SERVER_URL | TURN server URL | No |
| TURN_USERNAME | TURN server username | No |
| TURN_PASSWORD | TURN server password | No |
| NODE_ENV | Environment (development/production) | No |

## Health Check

```bash
curl https://api.yourdomain.com/api/health
```

## Monitoring

Use PM2 monitoring:
```bash
pm2 monit
pm2 logs secretcom
```
