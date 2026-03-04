package com.example.kairn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.home.HomeScreen

@Composable
fun KairnNavHost(
    navController: NavHostController,
    startDestination: Screen = Screen.HOME,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.name,
    ) {
        composable(Screen.HOME.name) {
            HomeScreen()
        }
        composable(Screen.CATALOG.name) {
            HomeScreen() // TODO: Replace with CatalogScreen
        }
        composable(Screen.EDITOR.name) {
            HomeScreen() // TODO: Replace with EditorScreen
        }
        composable(Screen.SOCIAL.name) {
            HomeScreen() // TODO: Replace with SocialScreen
        }
        composable(Screen.ACCOUNT.name) {
            AccountScreen()
        }
    }
}