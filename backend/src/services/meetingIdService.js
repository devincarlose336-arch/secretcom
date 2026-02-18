const { v4: uuidv4 } = require('uuid');
const MeetingId = require('../models/MeetingId');
const config = require('../config');

const generateMeetingIds = async (count = config.meetingIds.totalIds) => {
  const existingCount = await MeetingId.countDocuments();
  if (existingCount >= count) {
    return { generated: 0, total: existingCount };
  }

  const toGenerate = count - existingCount;
  const ids = [];
  const existingIds = new Set(
    (await MeetingId.find({}, { meetingId: 1 })).map((m) => m.meetingId)
  );

  for (let i = 0; i < toGenerate; i++) {
    let id;
    do {
      id = `SC-${uuidv4().substring(0, 8).toUpperCase()}`;
    } while (existingIds.has(id));
    existingIds.add(id);
    ids.push({ meetingId: id });
  }

  if (ids.length > 0) {
    await MeetingId.insertMany(ids);
  }

  return { generated: ids.length, total: existingCount + ids.length };
};

const getAvailableMeetingId = async () => {
  const meetingId = await MeetingId.findOne({ isAssigned: false });
  return meetingId;
};

const assignMeetingId = async (meetingIdStr, userId) => {
  const meetingId = await MeetingId.findOneAndUpdate(
    { meetingId: meetingIdStr, isAssigned: false },
    { isAssigned: true, assignedTo: userId, assignedAt: new Date() },
    { new: true }
  );
  return meetingId;
};

const releaseMeetingId = async (meetingIdStr) => {
  const meetingId = await MeetingId.findOneAndUpdate(
    { meetingId: meetingIdStr },
    { isAssigned: false, assignedTo: null, assignedAt: null },
    { new: true }
  );
  return meetingId;
};

const validateMeetingId = async (meetingIdStr) => {
  const meetingId = await MeetingId.findOne({ meetingId: meetingIdStr });
  return meetingId;
};

const getMeetingIdStats = async () => {
  const total = await MeetingId.countDocuments();
  const assigned = await MeetingId.countDocuments({ isAssigned: true });
  return { total, assigned, available: total - assigned };
};

module.exports = {
  generateMeetingIds,
  getAvailableMeetingId,
  assignMeetingId,
  releaseMeetingId,
  validateMeetingId,
  getMeetingIdStats,
};
