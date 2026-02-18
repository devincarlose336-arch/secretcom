const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');

const config = require('./config');
const connectDB = require('./config/database');
const { getRedis } = require('./config/redis');
const { setupSocket } = require('./services/socketService');
const { initializeRooms } = require('./services/roomService');
const seedSuperAdmin = require('./utils/seedSuperAdmin');

const authRoutes = require('./routes/auth');
const roomRoutes = require('./routes/rooms');
const meetingIdRoutes = require('./routes/meetingIds');
const adminRoutes = require('./routes/admin');
const webrtcRoutes = require('./routes/webrtc');

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST'],
  },
  pingTimeout: 60000,
  pingInterval: 25000,
});

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
});

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(morgan('combined'));
app.use('/api/', limiter);

app.use('/api/auth', authRoutes);
app.use('/api/rooms', roomRoutes);
app.use('/api/meeting-ids', meetingIdRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/webrtc', webrtcRoutes);

app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    version: '1.0.0',
  });
});

app.use((err, req, res, _next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ error: 'Internal server error' });
});

const start = async () => {
  try {
    await connectDB();

    const redis = getRedis();
    try {
      await redis.connect();
    } catch (e) {
      console.warn('Redis not available, continuing without it');
    }

    await seedSuperAdmin();
    await initializeRooms();

    setupSocket(io);

    server.listen(config.port, () => {
      console.log(`Secretcom server running on port ${config.port}`);
      console.log(`Environment: ${config.nodeEnv}`);
    });
  } catch (error) {
    console.error('Server startup failed:', error);
    process.exit(1);
  }
};

start();

module.exports = { app, server, io };
