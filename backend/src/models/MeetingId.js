const mongoose = require('mongoose');

const meetingIdSchema = new mongoose.Schema({
  meetingId: {
    type: String,
    required: true,
    unique: true,
  },
  isAssigned: {
    type: Boolean,
    default: false,
  },
  assignedTo: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    default: null,
  },
  assignedAt: {
    type: Date,
    default: null,
  },
}, {
  timestamps: true,
});

meetingIdSchema.index({ isAssigned: 1 });
meetingIdSchema.index({ meetingId: 1 });

module.exports = mongoose.model('MeetingId', meetingIdSchema);
