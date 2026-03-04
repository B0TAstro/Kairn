package com.example.kairn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.home.HomeScreen

@Composable
fun KairnNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.HOME,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.name,
        modifier = modifier
    ) {
        composable(Screen.HOME.name) {
            HomeScreen()
        }
        composable(Screen.EXPLORE.name) {
            HomeScreen() // TODO: Replace with ExploreScreen
        }
        composable(Screen.CHAT.name) {
            HomeScreen() // TODO: Replace with ChatScreen
        }
        composable(Screen.PROFILE.name) {
            AccountScreen()
        }
    }
}
