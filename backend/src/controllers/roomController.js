const roomService = require('../services/roomService');

const getAllRooms = async (req, res) => {
  try {
    const rooms = await roomService.getAllRooms();
    res.json({ rooms });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch rooms' });
  }
};

const getRoom = async (req, res) => {
  try {
    const room = await roomService.getRoom(parseInt(req.params.roomNumber));
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    res.json({ room });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch room' });
  }
};

const joinRoom = async (req, res) => {
  try {
    const { roomNumber } = req.params;
    const participant = {
      userId: req.user._id,
      meetingId: req.user.meetingId || req.body.meetingId,
      name: req.user.name,
    };

    const room = await roomService.joinRoom(parseInt(roomNumber), participant);
    res.json({ room, message: 'Joined room successfully' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const leaveRoom = async (req, res) => {
  try {
    const { roomNumber } = req.params;
    const meetingId = req.user.meetingId || req.body.meetingId;
    const room = await roomService.leaveRoom(parseInt(roomNumber), meetingId);
    res.json({ room, message: 'Left room successfully' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const muteParticipant = async (req, res) => {
  try {
    const { roomNumber } = req.params;
    const { meetingId, muted } = req.body;
    const room = await roomService.muteParticipant(parseInt(roomNumber), meetingId, muted);
    res.json({ room, message: `Participant ${muted ? 'muted' : 'unmuted'}` });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const removeParticipant = async (req, res) => {
  try {
    const { roomNumber } = req.params;
    const { meetingId } = req.body;
    const room = await roomService.removeParticipant(parseInt(roomNumber), meetingId);
    res.json({ room, message: 'Participant removed' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

const getRoomStats = async (req, res) => {
  try {
    const stats = await roomService.getRoomStats();
    res.json({ stats });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch room stats' });
  }
};

module.exports = {
  getAllRooms,
  getRoom,
  joinRoom,
  leaveRoom,
  muteParticipant,
  removeParticipant,
  getRoomStats,
};
