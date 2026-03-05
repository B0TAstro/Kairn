package com.example.kairn.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairn.ui.auth.AuthViewModel
import com.example.kairn.ui.auth.LoginScreen
import com.example.kairn.ui.auth.SignUpScreen
import com.example.kairn.ui.components.KairnBottomNavBar
import com.example.kairn.ui.components.NavBarItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object SignUp : Route("signup")
    data object Main : Route("main")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = Route.Login.route
    ) {
        authGraph(navController)
        composable(Route.Main.route) {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(Screen.HOME.name) }
    val hazeState = remember { HazeState() }

    val navBarItems = listOf(
        NavBarItem(Screen.HOME.name, "Home", Icons.Outlined.Home),
        NavBarItem(Screen.EXPLORE.name, "Explore", Icons.Outlined.Explore),
        NavBarItem(Screen.EDITOR.name, "Create", Icons.Outlined.Edit),
        NavBarItem(Screen.CHAT.name, "Chat", Icons.AutoMirrored.Outlined.Chat),
        NavBarItem(Screen.PROFILE.name, "Profile", Icons.Outlined.Person),
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        KairnNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        )

        KairnBottomNavBar(
            items = navBarItems,
            selectedItem = selectedItem,
            onItemSelected = { itemId ->
                selectedItem = itemId
                navController.navigate(itemId) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(Route.Login.route) {
        LoginScreen(
            onNavigateToSignUp = { navController.navigate(Route.SignUp.route) },
            onSignInSuccess = { navController.navigate(Route.Main.route) }
        )
    }
    
    composable(Route.SignUp.route) {
        SignUpScreen(
            onNavigateToSignIn = { navController.navigateUp() },
            onSignUpSuccess = { navController.navigate(Route.Login.route) }
        )
    }
}