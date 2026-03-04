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
    startDestination: Tab = Tab.HOME,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.name,
    ) {
        composable(Tab.HOME.name) {
            HomeScreen()
        }
        composable(Tab.CATALOG.name) {
            HomeScreen() // TODO: Replace with CatalogScreen
        }
        composable(Tab.EDITOR.name) {
            HomeScreen() // TODO: Replace with EditorScreen
        }
        composable(Tab.SOCIAL.name) {
            HomeScreen() // TODO: Replace with SocialScreen
        }
        composable(Tab.ACCOUNT.name) {
            AccountScreen()
        }
    }
}
