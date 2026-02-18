package com.secretcom.app.ui.admin

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
import com.secretcom.app.domain.model.User
import com.secretcom.app.domain.model.UserRole
import com.secretcom.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    state: AdminState,
    userRole: UserRole?,
    onBack: () -> Unit,
    onGenerateIds: () -> Unit,
    onLoadUsers: () -> Unit,
    onLoadAdmins: () -> Unit,
    onToggleUser: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onCreateAdmin: (String, String, String) -> Unit,
    onDeleteRecording: (com.secretcom.app.data.local.entity.RecordingEntity) -> Unit,
    onMonitorRoom: (Int) -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            kotlinx.coroutines.delay(3000)
            onClearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            onClearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                ) {
                    Text(
                        state.error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                }
            }

            if (state.successMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Accent.copy(alpha = 0.1f),
                ) {
                    Text(
                        state.successMessage,
                        modifier = Modifier.padding(12.dp),
                        color = Accent,
                        fontSize = 12.sp,
                    )
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Overview", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; onLoadUsers() }) {
                    Text("Users", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Recordings", modifier = Modifier.padding(12.dp))
                }
                if (userRole == UserRole.SUPER_ADMIN) {
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3; onLoadAdmins() }) {
                        Text("Admins", modifier = Modifier.padding(12.dp))
                    }
                }
            }

            when (selectedTab) {
                0 -> DashboardTab(state, onGenerateIds, onMonitorRoom)
                1 -> UsersTab(state, onToggleUser, onDeleteUser, userRole)
                2 -> RecordingsTab(state, onDeleteRecording)
                3 -> AdminsTab(state, onShowCreateAdmin = { showCreateAdmin = true })
            }
        }
    }

    if (showCreateAdmin) {
        CreateAdminDialog(
            onDismiss = { showCreateAdmin = false },
            onCreate = { username, password, name ->
                onCreateAdmin(username, password, name)
                showCreateAdmin = false
            },
        )
    }
}

@Composable
fun DashboardTab(
    state: AdminState,
    onGenerateIds: () -> Unit,
    onMonitorRoom: (Int) -> Unit,
) {
    val dashboard = state.dashboard

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Meeting IDs", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem("Total", dashboard?.meetingIds?.total?.toString() ?: "0")
                        StatItem("Assigned", dashboard?.meetingIds?.assigned?.toString() ?: "0")
                        StatItem("Available", dashboard?.meetingIds?.available?.toString() ?: "0")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onGenerateIds,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate 2000 IDs")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rooms", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        items(dashboard?.rooms ?: emptyList()) { room ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(room.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${room.participantCount}/${room.maxParticipants} participants",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    Row {
                        OutlinedButton(onClick = { onMonitorRoom(room.roomNumber) }) {
                            Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Monitor", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StatItem("Users", dashboard?.totalUsers?.toString() ?: "0")
                    StatItem("Admins", dashboard?.totalAdmins?.toString() ?: "0")
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Secondary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
fun UsersTab(
    state: AdminState,
    onToggleUser: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    userRole: UserRole?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.users) { user ->
            UserItem(user, onToggleUser, onDeleteUser, userRole)
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    userRole: UserRole?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Medium)
                Text(
                    "${user.role.value} | ${user.meetingId ?: "No ID"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            IconButton(onClick = { onToggle(user.id) }) {
                Icon(
                    if (user.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                    contentDescription = "Toggle",
                    tint = if (user.isActive) Accent else MaterialTheme.colorScheme.error,
                )
            }
            if (userRole == UserRole.SUPER_ADMIN) {
                IconButton(onClick = { onDelete(user.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun RecordingsTab(
    state: AdminState,
    onDelete: (com.secretcom.app.data.local.entity.RecordingEntity) -> Unit,
) {
    if (state.recordings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recordings yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.recordings) { recording ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, tint = Secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recording.roomName, fontWeight = FontWeight.Medium)
                            Text(
                                "Size: ${recording.fileSize / 1024} KB",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                        IconButton(onClick = { onDelete(recording) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminsTab(
    state: AdminState,
    onShowCreateAdmin: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onShowCreateAdmin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary),
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Admin")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.admins) { admin ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(admin.name, fontWeight = FontWeight.Medium)
                            Text(admin.username, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAdminDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Admin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(username, password, name) },
                enabled = username.isNotBlank() && password.length >= 6 && name.isNotBlank(),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
