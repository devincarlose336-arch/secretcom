require('dotenv').config();

module.exports = {
  port: process.env.PORT || 3000,
  mongoUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/secretcom',
  redisUrl: process.env.REDIS_URL || 'redis://localhost:6379',
  jwt: {
    secret: process.env.JWT_SECRET || 'secretcom-jwt-secret-key-2024',
    refreshSecret: process.env.JWT_REFRESH_SECRET || 'secretcom-jwt-refresh-secret-key-2024',
    expiry: process.env.JWT_EXPIRY || '1h',
    refreshExpiry: process.env.JWT_REFRESH_EXPIRY || '7d',
  },
  turn: {
    url: process.env.TURN_SERVER_URL || '',
    username: process.env.TURN_USERNAME || '',
    password: process.env.TURN_PASSWORD || '',
  },
  stun: [
    'stun:stun.l.google.com:19302',
    'stun:stun1.l.google.com:19302',
    'stun:stun2.l.google.com:19302',
  ],
  rooms: {
    maxRooms: 4,
    maxParticipantsPerRoom: 25,
  },
  meetingIds: {
    totalIds: 2000,
  },
  nodeEnv: process.env.NODE_ENV || 'development',
};
