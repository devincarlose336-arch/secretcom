const express = require('express');
const router = express.Router();
const meetingIdController = require('../controllers/meetingIdController');
const { authenticate, authorize } = require('../middleware/auth');

router.post('/generate', authenticate, authorize('super_admin', 'admin'), meetingIdController.generateIds);
router.get('/stats', authenticate, authorize('super_admin', 'admin'), meetingIdController.getStats);
router.get('/validate/:meetingId', meetingIdController.validateId);
router.post('/release/:meetingId', authenticate, authorize('super_admin', 'admin'), meetingIdController.releaseId);

module.exports = router;
