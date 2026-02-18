# Secretcom - Maintenance Guide

## Regular Maintenance

### Database Maintenance
```bash
# MongoDB backup
mongodump --db secretcom --out /backups/$(date +%Y%m%d)

# MongoDB restore
mongorestore --db secretcom /backups/20240101/secretcom

# Clean expired sessions (Redis)
redis-cli FLUSHDB
```

### Log Management
```bash
# View backend logs
pm2 logs secretcom

# Rotate logs
pm2 install pm2-logrotate
pm2 set pm2-logrotate:max_size 10M
pm2 set pm2-logrotate:retain 30
```

### Server Updates
```bash
cd /opt/secretcom/backend
git pull origin main
npm install --production
pm2 restart secretcom
```

## Monitoring

### Health Check
```bash
curl http://localhost:3000/api/health
```

### Process Monitoring
```bash
pm2 monit
pm2 status
```

### Database Monitoring
```bash
# MongoDB stats
mongosh --eval "db.stats()"

# Redis stats
redis-cli INFO
```

## Troubleshooting

### Backend Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Server won't start | Port in use | `lsof -i :3000` and kill process |
| MongoDB connection error | MongoDB not running | `sudo systemctl start mongod` |
| Redis connection error | Redis not running | `sudo systemctl start redis` |
| High memory usage | Memory leak | Restart with `pm2 restart secretcom` |

### Android App Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Connection timeout | Wrong server URL | Check BASE_URL in build config |
| Audio not working | Permission denied | Grant microphone permission |
| WebRTC failure | STUN/TURN issue | Check ICE server configuration |
| Battery drain | Background service | Optimize foreground service |

### WebRTC Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Cannot connect peers | NAT traversal failure | Ensure TURN server is configured |
| Audio quality poor | Network congestion | Lower audio bitrate |
| One-way audio | Firewall blocking | Open UDP ports 49152-65535 |
| Echo | No echo cancellation | Enable hardware AEC |

## Scaling

### Horizontal Scaling
- Use MongoDB replica set for database redundancy
- Use Redis Cluster for session management
- Deploy multiple backend instances behind a load balancer
- Use sticky sessions for Socket.IO

### Vertical Scaling
- Increase server CPU/RAM as user count grows
- Optimize MongoDB indexes
- Monitor and adjust Node.js memory limits

## Security Updates
- Regularly update Node.js dependencies: `npm audit fix`
- Update Android dependencies in Gradle
- Rotate JWT secrets periodically
- Update SSL certificates before expiry
- Review and patch security vulnerabilities

## Backup Strategy
1. Daily MongoDB backups
2. Weekly full system snapshots
3. Store backups in separate location
4. Test restore procedures monthly
