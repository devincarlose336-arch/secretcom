const mongoose = require('mongoose');

const participantSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  meetingId: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  socketId: {
    type: String,
  },
  isMuted: {
    type: Boolean,
    default: false,
  },
  joinedAt: {
    type: Date,
    default: Date.now,
  },
}, { _id: true });

const roomSchema = new mongoose.Schema({
  roomNumber: {
    type: Number,
    required: true,
    unique: true,
    min: 1,
    max: 4,
  },
  name: {
    type: String,
    required: true,
  },
  participants: [participantSchema],
  maxParticipants: {
    type: Number,
    default: 25,
  },
  isActive: {
    type: Boolean,
    default: true,
  },
  createdBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  },
}, {
  timestamps: true,
});

roomSchema.methods.isFull = function () {
  return this.participants.length >= this.maxParticipants;
};

roomSchema.methods.addParticipant = function (participant) {
  if (this.isFull()) {
    throw new Error('Room is full');
  }
  const existing = this.participants.find(
    (p) => p.meetingId === participant.meetingId
  );
  if (existing) {
    throw new Error('Meeting ID already in use in this room');
  }
  this.participants.push(participant);
  return this;
};

roomSchema.methods.removeParticipant = function (meetingId) {
  this.participants = this.participants.filter(
    (p) => p.meetingId !== meetingId
  );
  return this;
};

module.exports = mongoose.model('Room', roomSchema);
