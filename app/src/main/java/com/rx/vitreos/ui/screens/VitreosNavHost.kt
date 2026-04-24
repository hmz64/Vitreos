package com.rx.vitreos.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rx.vitreos.ui.screens.auth.AuthScreen
import com.rx.vitreos.ui.screens.chat.ChatScreen
import com.rx.vitreos.ui.screens.contacts.ContactListScreen
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Contacts : Screen("contacts")
    data object Chat : Screen("chat/{userId}/{username}") {
        fun createRoute(userId: String, username: String): String {
            return "chat/${URLEncoder.encode(userId, "UTF-8")}/${URLEncoder.encode(username, "UTF-8")}"
        }
    }
}

@Composable
fun VitreosNavHost() {
    val navController = rememberNavController()
    val viewModel: VitreosViewModel = hiltViewModel()
    val currentUserId by viewModel.currentUserId.collectAsState(initial = "")
    val isLoggedIn by viewModel.isLoggedIn.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn && currentUserId.isNotEmpty()) Screen.Contacts.route else Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = { userId, username ->
                    viewModel.saveUser(userId, username)
                    navController.navigate(Screen.Contacts.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Contacts.route) {
            ContactListScreen(
                currentUserId = currentUserId,
                contacts = viewModel.contacts.collectAsState(initial = emptyList()).value,
                onContactClick = { contact ->
                    navController.navigate(
                        Screen.Chat.createRoute(contact.user.userId, contact.user.username)
                    )
                },
                onAddContact = {
                    // TODO: Show add contact dialog
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("userId") ?: ""
            val receiverUsername = remember(backStackEntry) {
                URLDecoder.decode(
                    backStackEntry.arguments?.getString("username") ?: "",
                    "UTF-8"
                )
            }

            ChatScreen(
                currentUserId = currentUserId,
                contact = viewModel.contacts.collectAsState(initial = emptyList())
                    .value.find { it.user.userId == receiverId }?.user
                    ?: com.rx.vitreos.domain.model.User(receiverId, receiverUsername, ""),
                messages = viewModel.currentChatMessages.collectAsState(initial = emptyList()).value,
                isContactTyping = viewModel.isContactTyping.collectAsState(initial = false).value,
                isContactOnline = viewModel.isContactOnline.collectAsState(initial = false).value,
                onSendMessage = { message ->
                    viewModel.sendMessage(receiverId, message)
                },
                onTyping = {
                    viewModel.sendTyping(receiverId)
                },
                onStopTyping = {
                    viewModel.sendStopTyping(receiverId)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}