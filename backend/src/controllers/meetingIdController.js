const meetingIdService = require('../services/meetingIdService');

const generateIds = async (req, res) => {
  try {
    const count = req.body.count || 2000;
    const result = await meetingIdService.generateMeetingIds(count);
    res.json({ message: 'Meeting IDs generated', ...result });
  } catch (error) {
    res.status(500).json({ error: 'Failed to generate meeting IDs' });
  }
};

const getStats = async (req, res) => {
  try {
    const stats = await meetingIdService.getMeetingIdStats();
    res.json({ stats });
  } catch (error) {
    res.status(500).json({ error: 'Failed to get meeting ID stats' });
  }
};

const validateId = async (req, res) => {
  try {
    const { meetingId } = req.params;
    const result = await meetingIdService.validateMeetingId(meetingId);
    if (!result) {
      return res.status(404).json({ valid: false, error: 'Meeting ID not found' });
    }
    res.json({
      valid: true,
      isAssigned: result.isAssigned,
      meetingId: result.meetingId,
    });
  } catch (error) {
    res.status(500).json({ error: 'Validation failed' });
  }
};

const releaseId = async (req, res) => {
  try {
    const { meetingId } = req.params;
    const result = await meetingIdService.releaseMeetingId(meetingId);
    if (!result) {
      return res.status(404).json({ error: 'Meeting ID not found' });
    }
    res.json({ message: 'Meeting ID released', meetingId: result.meetingId });
  } catch (error) {
    res.status(500).json({ error: 'Failed to release meeting ID' });
  }
};

module.exports = { generateIds, getStats, validateId, releaseId };
