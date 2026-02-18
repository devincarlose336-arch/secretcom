const User = require('../models/User');
const roomService = require('../services/roomService');
const meetingIdService = require('../services/meetingIdService');

const getDashboard = async (req, res) => {
  try {
    const [roomStats, meetingIdStats, userCount, adminCount] = await Promise.all([
      roomService.getRoomStats(),
      meetingIdService.getMeetingIdStats(),
      User.countDocuments({ role: 'user' }),
      User.countDocuments({ role: 'admin' }),
    ]);

    res.json({
      rooms: roomStats,
      meetingIds: meetingIdStats,
      users: { total: userCount, admins: adminCount },
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch dashboard' });
  }
};

const getUsers = async (req, res) => {
  try {
    const users = await User.find({ role: { $ne: 'super_admin' } })
      .select('-password -refreshToken')
      .sort({ createdAt: -1 });
    res.json({ users });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch users' });
  }
};

const getAdmins = async (req, res) => {
  try {
    const admins = await User.find({ role: 'admin' })
      .select('-password -refreshToken')
      .sort({ createdAt: -1 });
    res.json({ admins });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch admins' });
  }
};

const toggleUserStatus = async (req, res) => {
  try {
    const { userId } = req.params;
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    if (user.role === 'super_admin') {
      return res.status(403).json({ error: 'Cannot modify super admin' });
    }

    user.isActive = !user.isActive;
    await user.save();
    res.json({ user: user.toJSON(), message: `User ${user.isActive ? 'activated' : 'deactivated'}` });
  } catch (error) {
    res.status(500).json({ error: 'Failed to update user status' });
  }
};

const deleteUser = async (req, res) => {
  try {
    const { userId } = req.params;
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    if (user.role === 'super_admin') {
      return res.status(403).json({ error: 'Cannot delete super admin' });
    }

    if (user.meetingId) {
      await meetingIdService.releaseMeetingId(user.meetingId);
      await roomService.leaveAllRooms(user.meetingId);
    }

    await User.findByIdAndDelete(userId);
    res.json({ message: 'User deleted successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to delete user' });
  }
};

module.exports = { getDashboard, getUsers, getAdmins, toggleUserStatus, deleteUser };
