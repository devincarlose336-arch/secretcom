const { authenticateSocket } = require('../middleware/auth');
const roomService = require('./roomService');

const setupSocket = (io) => {
  io.use(authenticateSocket);

  const roomNamespace = io.of('/rooms');
  roomNamespace.use(authenticateSocket);

  roomNamespace.on('connection', (socket) => {
    console.log(`User connected: ${socket.user.name} (${socket.userId})`);

    socket.on('join-room', async (data) => {
      try {
        const { roomNumber, meetingId } = data;
        const participant = {
          userId: socket.user._id,
          meetingId: meetingId || socket.user.meetingId,
          name: socket.user.name,
          socketId: socket.id,
        };

        const room = await roomService.joinRoom(roomNumber, participant);
        socket.join(`room-${roomNumber}`);
        socket.roomNumber = roomNumber;
        socket.meetingId = meetingId || socket.user.meetingId;

        socket.emit('room-joined', {
          roomNumber,
          participants: room.participants,
        });

        socket.to(`room-${roomNumber}`).emit('participant-joined', {
          participant: {
            meetingId: participant.meetingId,
            name: participant.name,
            socketId: socket.id,
          },
        });

        roomNamespace.to(`room-${roomNumber}`).emit('participant-count', {
          roomNumber,
          count: room.participants.length,
        });
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    socket.on('leave-room', async (data) => {
      try {
        const { roomNumber } = data;
        const meetingId = socket.meetingId;
        if (!meetingId) return;

        const room = await roomService.leaveRoom(roomNumber, meetingId);
        socket.leave(`room-${roomNumber}`);

        socket.to(`room-${roomNumber}`).emit('participant-left', {
          meetingId,
          name: socket.user.name,
        });

        if (room) {
          roomNamespace.to(`room-${roomNumber}`).emit('participant-count', {
            roomNumber,
            count: room.participants.length,
          });
        }

        socket.roomNumber = null;
        socket.meetingId = null;
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    socket.on('audio-data', (data) => {
      const { roomNumber, audioData } = data;
      socket.to(`room-${roomNumber}`).emit('audio-data', {
        from: socket.meetingId,
        fromName: socket.user.name,
        audioData,
        timestamp: Date.now(),
      });
    });

    socket.on('push-to-talk-start', (data) => {
      const { roomNumber } = data;
      socket.to(`room-${roomNumber}`).emit('user-speaking', {
        meetingId: socket.meetingId,
        name: socket.user.name,
        speaking: true,
      });
    });

    socket.on('push-to-talk-end', (data) => {
      const { roomNumber } = data;
      socket.to(`room-${roomNumber}`).emit('user-speaking', {
        meetingId: socket.meetingId,
        name: socket.user.name,
        speaking: false,
      });
    });

    socket.on('webrtc-offer', (data) => {
      const { targetSocketId, offer } = data;
      roomNamespace.to(targetSocketId).emit('webrtc-offer', {
        from: socket.id,
        fromMeetingId: socket.meetingId,
        offer,
      });
    });

    socket.on('webrtc-answer', (data) => {
      const { targetSocketId, answer } = data;
      roomNamespace.to(targetSocketId).emit('webrtc-answer', {
        from: socket.id,
        fromMeetingId: socket.meetingId,
        answer,
      });
    });

    socket.on('webrtc-ice-candidate', (data) => {
      const { targetSocketId, candidate } = data;
      roomNamespace.to(targetSocketId).emit('webrtc-ice-candidate', {
        from: socket.id,
        fromMeetingId: socket.meetingId,
        candidate,
      });
    });

    socket.on('admin-mute', async (data) => {
      try {
        if (!['super_admin', 'admin'].includes(socket.user.role)) {
          return socket.emit('error', { message: 'Unauthorized' });
        }
        const { roomNumber, meetingId, muted } = data;
        await roomService.muteParticipant(roomNumber, meetingId, muted);

        roomNamespace.to(`room-${roomNumber}`).emit('participant-muted', {
          meetingId,
          muted,
          by: socket.user.name,
        });
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    socket.on('admin-remove', async (data) => {
      try {
        if (!['super_admin', 'admin'].includes(socket.user.role)) {
          return socket.emit('error', { message: 'Unauthorized' });
        }
        const { roomNumber, meetingId } = data;
        await roomService.removeParticipant(roomNumber, meetingId);

        roomNamespace.to(`room-${roomNumber}`).emit('participant-removed', {
          meetingId,
          by: socket.user.name,
        });
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    socket.on('admin-monitor', (data) => {
      if (!['super_admin', 'admin'].includes(socket.user.role)) {
        return socket.emit('error', { message: 'Unauthorized' });
      }
      const { roomNumber } = data;
      socket.join(`room-${roomNumber}`);
      socket.emit('monitor-started', { roomNumber });
    });

    socket.on('disconnect', async () => {
      console.log(`User disconnected: ${socket.user.name}`);
      if (socket.roomNumber && socket.meetingId) {
        try {
          const room = await roomService.leaveRoom(socket.roomNumber, socket.meetingId);
          socket.to(`room-${socket.roomNumber}`).emit('participant-left', {
            meetingId: socket.meetingId,
            name: socket.user.name,
          });
          if (room) {
            roomNamespace.to(`room-${socket.roomNumber}`).emit('participant-count', {
              roomNumber: socket.roomNumber,
              count: room.participants.length,
            });
          }
        } catch (error) {
          console.error('Disconnect cleanup error:', error.message);
        }
      }
    });
  });

  return io;
};

module.exports = { setupSocket };
