# Secretcom - Admin Guide

## Admin Roles

### Super Admin
- Full system access
- Create/manage admin accounts
- Generate meeting IDs
- Manage all users
- Monitor all rooms
- Mute/remove/expel participants
- Record conversations

### Admin
- Monitor rooms
- Manage users
- Mute/remove participants
- Record conversations

## Login

### Super Admin
- Username: `admin`
- Password: `Chk12231@`

### Admin
Use credentials provided by the Super Admin.

## Admin Dashboard

Access via the settings icon in the top-right corner of the room list.

### Overview Tab
- **Meeting IDs**: Total, assigned, and available counts
- **Generate IDs**: Creates 2000 unique meeting IDs
- **Room Status**: Real-time participant counts per room
- **Monitor**: Listen to room audio silently
- **User Statistics**: Total users and admin counts

### Users Tab
- View all registered users
- Toggle user active/inactive status
- Delete users (Super Admin only)
- View user details (name, role, meeting ID)

### Recordings Tab
- View all saved recordings
- See recording details (room, size, date)
- Delete recordings
- Export recordings from device storage

### Admins Tab (Super Admin Only)
- Create new admin accounts
- View existing admins

## Key Operations

### Generate Meeting IDs
1. Go to Admin Dashboard > Overview
2. Tap "Generate 2000 IDs"
3. IDs are created in format SC-XXXXXXXX
4. Distribute IDs to users for registration

### Create Admin Account
1. Go to Admin Dashboard > Admins tab
2. Tap "Create Admin"
3. Enter name, username, and password (min 6 chars)
4. Tap "Create"

### Monitor a Room
1. Go to Admin Dashboard > Overview
2. Tap "Monitor" next to any room
3. Listen to room audio silently (participants won't know)

### Mute a Participant
1. Join or monitor a room
2. Tap the microphone icon next to a participant
3. Participant will be muted/unmuted

### Remove a Participant
1. Join or monitor a room
2. Tap the remove icon next to a participant
3. Participant will be expelled from the room

### Record Conversations
1. Join a room
2. Tap the record icon in the top bar
3. Red indicator shows recording is active
4. Tap stop icon to end recording
5. Recordings saved to device storage
6. Manage recordings in Admin Dashboard > Recordings

## Security Best Practices
- Change super admin password regularly
- Create individual admin accounts (don't share super admin)
- Monitor room activity regularly
- Review and clean up unused meeting IDs
- Back up recordings securely
