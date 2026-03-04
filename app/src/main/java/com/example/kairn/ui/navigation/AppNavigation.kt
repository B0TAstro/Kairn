package com.example.kairn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairn.ui.auth.AuthViewModel
import com.example.kairn.ui.auth.LoginScreen
import com.example.kairn.ui.auth.SignUpScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Main : Screen("main")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        authGraph(navController)
        // mainGraph(navController) // To be implemented
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(Screen.Login.route) {
        LoginScreen(
            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
            onLoginSuccess = { navController.navigate(Screen.Main.route) }
        )
    }
    
    composable(Screen.SignUp.route) {
        SignUpScreen(
            onNavigateToLogin = { navController.navigateUp() },
            onSignUpSuccess = { navController.navigate(Screen.Login.route) }
        )
    }
}