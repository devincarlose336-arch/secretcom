const jwt = require('jsonwebtoken');
const User = require('../models/User');
const config = require('../config');
const meetingIdService = require('../services/meetingIdService');

const generateTokens = (userId, role) => {
  const accessToken = jwt.sign(
    { userId, role },
    config.jwt.secret,
    { expiresIn: config.jwt.expiry }
  );
  const refreshToken = jwt.sign(
    { userId, role },
    config.jwt.refreshSecret,
    { expiresIn: config.jwt.refreshExpiry }
  );
  return { accessToken, refreshToken };
};

const login = async (req, res) => {
  try {
    const { username, password } = req.body;
    const user = await User.findOne({ username });
    if (!user || !user.isActive) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const tokens = generateTokens(user._id, user.role);
    user.refreshToken = tokens.refreshToken;
    user.lastLogin = new Date();
    await user.save();

    res.json({
      user: user.toJSON(),
      ...tokens,
    });
  } catch (error) {
    res.status(500).json({ error: 'Login failed' });
  }
};

const register = async (req, res) => {
  try {
    const { name, meetingId } = req.body;

    const meetingIdDoc = await meetingIdService.validateMeetingId(meetingId);
    if (!meetingIdDoc) {
      return res.status(400).json({ error: 'Invalid meeting ID' });
    }
    if (meetingIdDoc.isAssigned) {
      return res.status(400).json({ error: 'Meeting ID already in use' });
    }

    const existingUser = await User.findOne({ meetingId });
    if (existingUser) {
      return res.status(400).json({ error: 'Meeting ID already registered' });
    }

    const username = `user_${meetingId.toLowerCase().replace(/[^a-z0-9]/g, '')}`;
    const password = `${meetingId}_${Date.now()}`;

    const user = new User({
      username,
      password,
      name,
      meetingId,
      role: 'user',
    });
    await user.save();

    await meetingIdService.assignMeetingId(meetingId, user._id);

    const tokens = generateTokens(user._id, user.role);
    user.refreshToken = tokens.refreshToken;
    await user.save();

    res.status(201).json({
      user: user.toJSON(),
      ...tokens,
    });
  } catch (error) {
    if (error.code === 11000) {
      return res.status(400).json({ error: 'Username or meeting ID already exists' });
    }
    res.status(500).json({ error: 'Registration failed' });
  }
};

const createAdmin = async (req, res) => {
  try {
    const { username, password, name } = req.body;

    const existing = await User.findOne({ username });
    if (existing) {
      return res.status(400).json({ error: 'Username already exists' });
    }

    const admin = new User({
      username,
      password,
      name,
      role: 'admin',
    });
    await admin.save();

    res.status(201).json({ user: admin.toJSON() });
  } catch (error) {
    res.status(500).json({ error: 'Failed to create admin' });
  }
};

const refreshToken = async (req, res) => {
  try {
    const { refreshToken: token } = req.body;
    if (!token) {
      return res.status(400).json({ error: 'Refresh token required' });
    }

    const decoded = jwt.verify(token, config.jwt.refreshSecret);
    const user = await User.findById(decoded.userId);
    if (!user || user.refreshToken !== token) {
      return res.status(401).json({ error: 'Invalid refresh token' });
    }

    const tokens = generateTokens(user._id, user.role);
    user.refreshToken = tokens.refreshToken;
    await user.save();

    res.json(tokens);
  } catch (error) {
    res.status(401).json({ error: 'Invalid refresh token' });
  }
};

const getProfile = async (req, res) => {
  res.json({ user: req.user });
};

const logout = async (req, res) => {
  try {
    const user = await User.findById(req.userId);
    if (user) {
      user.refreshToken = null;
      await user.save();
    }
    res.json({ message: 'Logged out successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Logout failed' });
  }
};

module.exports = { login, register, createAdmin, refreshToken, getProfile, logout };
