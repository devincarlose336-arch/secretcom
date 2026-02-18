# WebRTC Server Setup Guide

## STUN Servers

Secretcom uses Google's public STUN servers by default:
- `stun:stun.l.google.com:19302`
- `stun:stun1.l.google.com:19302`
- `stun:stun2.l.google.com:19302`

These are configured in `android/app/src/main/res/values/webrtc_config.xml`.

## TURN Server Setup

A TURN server is required for reliable connectivity through NAT/firewalls.

### Option 1: Coturn (Self-Hosted)

#### Install
```bash
sudo apt install coturn
```

#### Configure `/etc/turnserver.conf`
```
listening-port=3478
tls-listening-port=5349
listening-ip=0.0.0.0
relay-ip=YOUR_SERVER_IP
external-ip=YOUR_PUBLIC_IP
realm=yourdomain.com
server-name=yourdomain.com
lt-cred-mech
user=secretcom:your_turn_password
total-quota=100
max-bps=0
stale-nonce=600
cert=/etc/letsencrypt/live/yourdomain.com/fullchain.pem
pkey=/etc/letsencrypt/live/yourdomain.com/privkey.pem
no-stdout-log
log-file=/var/log/turnserver.log
```

#### Start
```bash
sudo systemctl enable coturn
sudo systemctl start coturn
```

#### Firewall Rules
```bash
sudo ufw allow 3478/tcp
sudo ufw allow 3478/udp
sudo ufw allow 5349/tcp
sudo ufw allow 5349/udp
sudo ufw allow 49152:65535/udp
```

### Option 2: Twilio TURN

1. Sign up at https://www.twilio.com
2. Get API credentials
3. Use Twilio's Network Traversal Service
4. Update backend `.env`:
```
TURN_SERVER_URL=turn:global.turn.twilio.com:3478
TURN_USERNAME=your_twilio_api_key
TURN_PASSWORD=your_twilio_api_secret
```

### Option 3: Metered.ca TURN

1. Sign up at https://www.metered.ca
2. Create a TURN server
3. Get credentials from dashboard
4. Update configuration accordingly

### Option 4: Xirsys TURN

1. Sign up at https://xirsys.com
2. Create a channel
3. Use the provided TURN credentials

## Configuring in Secretcom

### Backend Configuration
Update `.env`:
```
TURN_SERVER_URL=turn:your-turn-server.com:3478
TURN_USERNAME=your_username
TURN_PASSWORD=your_password
```

### Android Configuration
Update `res/values/webrtc_config.xml`:
```xml
<string name="turn_server_url">turn:your-turn-server.com:3478</string>
<string name="turn_username">your_username</string>
<string name="turn_password">your_password</string>
```

## Testing WebRTC

1. Use https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/ to test STUN/TURN
2. Check browser console for ICE candidate gathering
3. Test on different networks (WiFi, cellular, behind NAT)

## Audio Quality Optimization

- Enable echo cancellation (enabled by default)
- Enable noise suppression (enabled by default)
- Enable auto gain control (enabled by default)
- Enable high-pass filter (enabled by default)
- Configure audio bitrate based on network conditions
