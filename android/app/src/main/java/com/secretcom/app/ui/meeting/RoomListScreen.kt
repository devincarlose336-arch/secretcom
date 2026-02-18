package com.secretcom.app.ui.meeting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secretcom.app.domain.model.Room
import com.secretcom.app.domain.model.UserRole
import com.secretcom.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    state: MeetingState,
    userRole: UserRole?,
    userName: String,
    meetingId: String?,
    onJoinRoom: (Int) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Secretcom", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Welcome, $userName",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                },
                actions = {
                    if (userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ADMIN) {
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    if (meetingId != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = Secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Your Meeting ID", fontSize = 12.sp, color = Secondary)
                                    Text(meetingId, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        "Available Rooms",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                items(state.rooms) { room ->
                    RoomCard(
                        room = room,
                        onJoin = { onJoinRoom(room.roomNumber) },
                    )
                }
            }
        }
    }
}

@Composable
fun RoomCard(
    room: Room,
    onJoin: () -> Unit,
) {
    val participantCount = room.participants.size
    val isFull = participantCount >= room.maxParticipants

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(room.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "$participantCount/${room.maxParticipants} participants",
                            fontSize = 12.sp,
                            color = if (isFull) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }

                Button(
                    onClick = onJoin,
                    enabled = !isFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFull) PttInactive else Accent,
                    ),
                ) {
                    Text(if (isFull) "FULL" else "JOIN")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { participantCount.toFloat() / room.maxParticipants.toFloat() },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (isFull) MaterialTheme.colorScheme.error else Accent,
                trackColor = PttInactive,
            )
        }
    }
}
