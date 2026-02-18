# Secretcom - Installation Guide

## Prerequisites

### Android Development
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 34
- Kotlin 1.9.20+

### Backend Development
- Node.js 18+ and npm
- MongoDB 6.0+
- Redis 7.0+ (optional, for session management)

## Android App Setup

### 1. Clone and Open Project
```bash
git clone <repository-url>
cd secretcom/android
```
Open the `android/` folder in Android Studio.

### 2. Configure Backend URL
Edit `android/app/build.gradle.kts`:
- **Debug**: Change `BASE_URL` and `SOCKET_URL` to your backend server address
- **Release**: Set production URLs

### 3. Configure STUN/TURN Servers
Edit `android/app/src/main/res/values/webrtc_config.xml`:
```xml
<string name="turn_server_url">turn:your-server.com:3478</string>
<string name="turn_username">your_username</string>
<string name="turn_password">your_password</string>
```

### 4. Build and Run
```bash
# From Android Studio: Build > Make Project
# Or via command line:
./gradlew assembleDebug
```

### 5. Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Backend Setup

### 1. Install Dependencies
```bash
cd backend
npm install
```

### 2. Configure Environment
```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Start MongoDB
```bash
mongod --dbpath /data/db
```

### 4. Start Redis (Optional)
```bash
redis-server
```

### 5. Run Backend
```bash
# Development
npm run dev

# Production
npm start
```

### 6. Verify
Visit `http://localhost:3000/api/health` - should return `{"status":"ok"}`

## First-Time Setup

1. Start the backend server
2. The super admin account is auto-created:
   - Username: `admin`
   - Password: `Chk12231@`
3. Login as super admin on the Android app
4. Navigate to Admin Dashboard
5. Generate meeting IDs (2000 IDs)
6. Create admin accounts as needed
7. Share meeting IDs with users for registration

## Troubleshooting

- **Connection refused**: Ensure backend is running and URL is correct
- **WebRTC not working**: Check STUN/TURN configuration
- **Audio not working**: Ensure microphone permission is granted
- **Socket disconnects**: Check network stability and firewall settings
