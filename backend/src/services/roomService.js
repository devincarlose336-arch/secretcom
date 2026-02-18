const Room = require('../models/Room');
const { getRedis } = require('../config/redis');
const config = require('../config');

const initializeRooms = async () => {
  for (let i = 1; i <= config.rooms.maxRooms; i++) {
    const existing = await Room.findOne({ roomNumber: i });
    if (!existing) {
      await Room.create({
        roomNumber: i,
        name: `Room ${i}`,
        maxParticipants: config.rooms.maxParticipantsPerRoom,
      });
    }
  }
  console.log(`${config.rooms.maxRooms} rooms initialized`);
};

const getAllRooms = async () => {
  return Room.find({ isActive: true }).populate('participants.userId', 'name username role');
};

const getRoom = async (roomNumber) => {
  return Room.findOne({ roomNumber, isActive: true })
    .populate('participants.userId', 'name username role');
};

const joinRoom = async (roomNumber, participant) => {
  const room = await Room.findOne({ roomNumber, isActive: true });
  if (!room) throw new Error('Room not found');
  if (room.isFull()) throw new Error('Room is full');

  const existingInAnyRoom = await Room.findOne({
    'participants.meetingId': participant.meetingId,
  });
  if (existingInAnyRoom) {
    throw new Error('Meeting ID already active in another room');
  }

  room.addParticipant(participant);
  await room.save();

  const redis = getRedis();
  try {
    await redis.hset(
      `room:${roomNumber}:participants`,
      participant.meetingId,
      JSON.stringify({ ...participant, joinedAt: new Date() })
    );
  } catch (e) {
    // Redis optional
  }

  return room;
};

const leaveRoom = async (roomNumber, meetingId) => {
  const room = await Room.findOne({ roomNumber });
  if (!room) return null;

  room.removeParticipant(meetingId);
  await room.save();

  const redis = getRedis();
  try {
    await redis.hdel(`room:${roomNumber}:participants`, meetingId);
  } catch (e) {
    // Redis optional
  }

  return room;
};

const leaveAllRooms = async (meetingId) => {
  const rooms = await Room.find({ 'participants.meetingId': meetingId });
  for (const room of rooms) {
    room.removeParticipant(meetingId);
    await room.save();
  }
  return rooms;
};

const muteParticipant = async (roomNumber, meetingId, muted) => {
  const room = await Room.findOne({ roomNumber });
  if (!room) throw new Error('Room not found');

  const participant = room.participants.find((p) => p.meetingId === meetingId);
  if (!participant) throw new Error('Participant not found');

  participant.isMuted = muted;
  await room.save();
  return room;
};

const removeParticipant = async (roomNumber, meetingId) => {
  return leaveRoom(roomNumber, meetingId);
};

const getRoomStats = async () => {
  const rooms = await Room.find({ isActive: true });
  return rooms.map((room) => ({
    roomNumber: room.roomNumber,
    name: room.name,
    participantCount: room.participants.length,
    maxParticipants: room.maxParticipants,
    isFull: room.isFull(),
  }));
};

module.exports = {
  initializeRooms,
  getAllRooms,
  getRoom,
  joinRoom,
  leaveRoom,
  leaveAllRooms,
  muteParticipant,
  removeParticipant,
  getRoomStats,
};
