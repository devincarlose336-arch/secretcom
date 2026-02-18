const express = require('express');
const router = express.Router();
const roomController = require('../controllers/roomController');
const { authenticate, authorize } = require('../middleware/auth');

router.get('/', authenticate, roomController.getAllRooms);
router.get('/stats', authenticate, roomController.getRoomStats);
router.get('/:roomNumber', authenticate, roomController.getRoom);
router.post('/:roomNumber/join', authenticate, roomController.joinRoom);
router.post('/:roomNumber/leave', authenticate, roomController.leaveRoom);
router.post('/:roomNumber/mute', authenticate, authorize('super_admin', 'admin'), roomController.muteParticipant);
router.post('/:roomNumber/remove', authenticate, authorize('super_admin', 'admin'), roomController.removeParticipant);

module.exports = router;
