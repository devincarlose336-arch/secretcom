const User = require('../models/User');

const seedSuperAdmin = async () => {
  try {
    const existing = await User.findOne({ role: 'super_admin' });
    if (existing) {
      console.log('Super admin already exists');
      return existing;
    }

    const superAdmin = new User({
      username: 'admin',
      password: 'Chk12231@',
      name: 'Super Administrator',
      role: 'super_admin',
      isActive: true,
    });

    await superAdmin.save();
    console.log('Super admin created successfully');
    return superAdmin;
  } catch (error) {
    console.error('Failed to seed super admin:', error.message);
  }
};

module.exports = seedSuperAdmin;
