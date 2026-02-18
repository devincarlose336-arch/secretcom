const { body, param, validationResult } = require('express-validator');

const handleValidation = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};

const validateLogin = [
  body('username').trim().notEmpty().withMessage('Username is required'),
  body('password').notEmpty().withMessage('Password is required'),
  handleValidation,
];

const validateRegister = [
  body('name').trim().notEmpty().withMessage('Name is required')
    .isLength({ min: 2, max: 100 }).withMessage('Name must be 2-100 characters'),
  body('meetingId').trim().notEmpty().withMessage('Meeting ID is required'),
  handleValidation,
];

const validateCreateAdmin = [
  body('username').trim().notEmpty().withMessage('Username is required')
    .isLength({ min: 3, max: 50 }).withMessage('Username must be 3-50 characters'),
  body('password').notEmpty().withMessage('Password is required')
    .isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
  body('name').trim().notEmpty().withMessage('Name is required'),
  handleValidation,
];

const validateRoomNumber = [
  param('roomNumber').isInt({ min: 1, max: 4 }).withMessage('Room number must be 1-4'),
  handleValidation,
];

module.exports = {
  validateLogin,
  validateRegister,
  validateCreateAdmin,
  validateRoomNumber,
  handleValidation,
};
