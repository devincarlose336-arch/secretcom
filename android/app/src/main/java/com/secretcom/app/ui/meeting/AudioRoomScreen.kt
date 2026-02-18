package com.secretcom.app.ui.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretcom.app.domain.model.Participant
import com.secretcom.app.domain.model.UserRole
import com.secretcom.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoomScreen(
    state: MeetingState,
    userRole: UserRole?,
    onLeaveRoom: () -> Unit,
    onStartSpeaking: () -> Unit,
    onStopSpeaking: () -> Unit,
    onMuteParticipant: (String, Boolean) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
) {
    val room = state.currentRoom ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(room.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${state.participants.size}/${room.maxParticipants} participants",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeaveRoom) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Leave")
                    }
                },
                actions = {
                    if (userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ADMIN) {
                        IconButton(
                            onClick = {
                                if (state.isRecording) onStopRecording() else onStartRecording()
                            }
                        ) {
                            Icon(
                                if (state.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                contentDescription = if (state.isRecording) "Stop Recording" else "Record",
                                tint = if (state.isRecording) Color.Red else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.isRecording) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Red.copy(alpha = 0.2f),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recording...", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(state.participants) { participant ->
                    ParticipantItem(
                        participant = participant,
                        isSpeaking = state.speakingUsers.contains(participant.meetingId),
                        isAdmin = userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ADMIN,
                        onMute = { onMuteParticipant(participant.meetingId, !participant.isMuted) },
                        onRemove = { onRemoveParticipant(participant.meetingId) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(if (state.isSpeaking) PttActive else PttInactive)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onStartSpeaking()
                                tryAwaitRelease()
                                onStopSpeaking()
                            }
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Push to Talk",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (state.isSpeaking) "Release\nto Stop" else "Push\nto Talk",
                        color = Color.White,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLeaveRoom,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXIT ROOM")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ParticipantItem(
    participant: Participant,
    isSpeaking: Boolean,
    isAdmin: Boolean,
    onMute: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpeaking) Accent.copy(alpha = 0.15f) else SurfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSpeaking) Accent else PttInactive),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    participant.name.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(participant.name, fontWeight = FontWeight.Medium)
                Text(
                    participant.meetingId,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }

            if (isSpeaking) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = "Speaking",
                    tint = Accent,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (participant.isMuted) {
                Icon(
                    Icons.Default.MicOff,
                    contentDescription = "Muted",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (isAdmin) {
                IconButton(onClick = onMute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (participant.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (participant.isMuted) "Unmute" else "Mute",
                        modifier = Modifier.size(18.dp),
                        tint = if (participant.isMuted) MaterialTheme.colorScheme.error else Secondary,
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
