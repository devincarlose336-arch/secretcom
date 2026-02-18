const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { authenticate, authorize } = require('../middleware/auth');

router.get('/dashboard', authenticate, authorize('super_admin', 'admin'), adminController.getDashboard);
router.get('/users', authenticate, authorize('super_admin', 'admin'), adminController.getUsers);
router.get('/admins', authenticate, authorize('super_admin'), adminController.getAdmins);
router.patch('/users/:userId/toggle', authenticate, authorize('super_admin', 'admin'), adminController.toggleUserStatus);
router.delete('/users/:userId', authenticate, authorize('super_admin'), adminController.deleteUser);

module.exports = router;
