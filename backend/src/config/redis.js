const Redis = require('ioredis');
const config = require('./index');

let redis = null;

const getRedis = () => {
  if (!redis) {
    redis = new Redis(config.redisUrl, {
      retryStrategy: (times) => {
        if (times > 3) {
          console.warn('Redis connection failed, running without Redis');
          return null;
        }
        return Math.min(times * 200, 2000);
      },
      maxRetriesPerRequest: 3,
      lazyConnect: true,
    });

    redis.on('error', (err) => {
      console.warn('Redis error:', err.message);
    });

    redis.on('connect', () => {
      console.log('Redis connected');
    });
  }
  return redis;
};

module.exports = { getRedis };
