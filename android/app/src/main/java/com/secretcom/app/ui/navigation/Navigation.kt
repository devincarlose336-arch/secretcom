package com.secretcom.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.secretcom.app.ui.admin.AdminScreen
import com.secretcom.app.ui.admin.AdminViewModel
import com.secretcom.app.ui.auth.AuthViewModel
import com.secretcom.app.ui.auth.LoginScreen
import com.secretcom.app.ui.auth.RegisterScreen
import com.secretcom.app.ui.meeting.AudioRoomScreen
import com.secretcom.app.ui.meeting.MeetingViewModel
import com.secretcom.app.ui.meeting.RoomListScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object RoomList : Screen("rooms")
    data object AudioRoom : Screen("audio_room/{roomNumber}") {
        fun createRoute(roomNumber: Int) = "audio_room/$roomNumber"
    }
    data object Admin : Screen("admin")
}

@Composable
fun SecretcomNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState.loginSuccess, authState.registerSuccess) {
        if (authState.loginSuccess || authState.registerSuccess) {
            navController.navigate(Screen.RoomList.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            authViewModel.resetSuccess()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.isLoggedIn) Screen.RoomList.route else Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                state = authState,
                onLogin = authViewModel::login,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onClearError = authViewModel::clearError,
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                state = authState,
                onRegister = authViewModel::register,
                onNavigateBack = { navController.popBackStack() },
                onClearError = authViewModel::clearError,
            )
        }

        composable(Screen.RoomList.route) {
            val meetingViewModel: MeetingViewModel = hiltViewModel()
            val meetingState by meetingViewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                meetingViewModel.loadRooms()
                meetingViewModel.connectSocket()
                meetingViewModel.initializeWebRTC()
            }

            RoomListScreen(
                state = meetingState,
                userRole = authState.userRole,
                userName = authState.userName,
                meetingId = authState.meetingId,
                onJoinRoom = { roomNumber ->
                    val mid = authState.meetingId ?: return@RoomListScreen
                    meetingViewModel.joinRoom(roomNumber, mid)
                    navController.navigate(Screen.AudioRoom.createRoute(roomNumber))
                },
                onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                onLogout = {
                    meetingViewModel.disconnect()
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRefresh = { meetingViewModel.loadRooms() },
            )
        }

        composable(
            route = Screen.AudioRoom.route,
            arguments = listOf(navArgument("roomNumber") { type = NavType.IntType }),
        ) {
            val meetingViewModel: MeetingViewModel = hiltViewModel()
            val meetingState by meetingViewModel.state.collectAsState()

            AudioRoomScreen(
                state = meetingState,
                userRole = authState.userRole,
                onLeaveRoom = {
                    meetingViewModel.leaveRoom()
                    navController.popBackStack()
                },
                onStartSpeaking = meetingViewModel::startSpeaking,
                onStopSpeaking = meetingViewModel::stopSpeaking,
                onMuteParticipant = meetingViewModel::muteParticipant,
                onRemoveParticipant = meetingViewModel::removeParticipant,
                onStartRecording = meetingViewModel::startRecording,
                onStopRecording = meetingViewModel::stopRecording,
            )
        }

        composable(Screen.Admin.route) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            val adminState by adminViewModel.state.collectAsState()
            val meetingViewModel: MeetingViewModel = hiltViewModel()

            AdminScreen(
                state = adminState,
                userRole = authState.userRole,
                onBack = { navController.popBackStack() },
                onGenerateIds = adminViewModel::generateMeetingIds,
                onLoadUsers = adminViewModel::loadUsers,
                onLoadAdmins = adminViewModel::loadAdmins,
                onToggleUser = adminViewModel::toggleUserStatus,
                onDeleteUser = adminViewModel::deleteUser,
                onCreateAdmin = adminViewModel::createAdmin,
                onDeleteRecording = adminViewModel::deleteRecording,
                onMonitorRoom = meetingViewModel::monitorRoom,
                onClearError = adminViewModel::clearError,
                onClearSuccess = adminViewModel::clearSuccess,
            )
        }
    }
}
