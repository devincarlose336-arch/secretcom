const express = require('express');
const router = express.Router();
const config = require('../config');
const { authenticate } = require('../middleware/auth');

router.get('/ice-servers', authenticate, (req, res) => {
  const iceServers = config.stun.map((url) => ({ urls: url }));

  if (config.turn.url) {
    iceServers.push({
      urls: config.turn.url,
      username: config.turn.username,
      credential: config.turn.password,
    });
  }

  res.json({ iceServers });
});

module.exports = router;
