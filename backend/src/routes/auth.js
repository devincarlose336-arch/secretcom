const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { authenticate, authorize } = require('../middleware/auth');
const { validateLogin, validateRegister, validateCreateAdmin } = require('../middleware/validator');

router.post('/login', validateLogin, authController.login);
router.post('/register', validateRegister, authController.register);
router.post('/refresh-token', authController.refreshToken);
router.post('/admin/create', authenticate, authorize('super_admin'), validateCreateAdmin, authController.createAdmin);
router.get('/profile', authenticate, authController.getProfile);
router.post('/logout', authenticate, authController.logout);

module.exports = router;
