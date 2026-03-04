package com.example.kairn.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.kairn.R
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.auth.AuthViewModel
import com.example.kairn.ui.auth.LoginScreen
import com.example.kairn.ui.auth.SignUpScreen
import com.example.kairn.ui.home.HomeScreen

// ---------------------------------------------------------------------------
// Route definitions
// ---------------------------------------------------------------------------

sealed class Screen(val route: String) {
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Main : Screen("main")
}

// ---------------------------------------------------------------------------
// Root navigation (auth-first)
// ---------------------------------------------------------------------------

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val startDestination = if (authViewModel.isAuthenticated) {
        Screen.Main.route
    } else {
        Screen.SignIn.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authGraph(navController)
        composable(Screen.Main.route) {
            MainScaffold(
                onSignOut = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Auth sub-graph
// ---------------------------------------------------------------------------

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(Screen.SignIn.route) {
        LoginScreen(
            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
            onSignInSuccess = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )
    }

    composable(Screen.SignUp.route) {
        SignUpScreen(
            onNavigateToSignIn = { navController.navigateUp() },
            onSignUpSuccess = { navController.navigate(Screen.SignIn.route) },
        )
    }
}

// ---------------------------------------------------------------------------
// Main scaffold with bottom navigation
// ---------------------------------------------------------------------------

private data class TabItem(
    val tab: Tab,
    val label: String,
    val iconRes: Int,
)

@Composable
private fun MainScaffold(
    onSignOut: () -> Unit,
) {
    val tabNavController = rememberNavController()
    var selectedIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem(Tab.HOME, "Home", R.drawable.ic_home),
        TabItem(Tab.CATALOG, "Catalog", R.drawable.ic_catalog),
        TabItem(Tab.EDITOR, "Editor", R.drawable.ic_editor),
        TabItem(Tab.SOCIAL, "Social", R.drawable.ic_social),
        TabItem(Tab.ACCOUNT, "Account", R.drawable.ic_account),
    )

    Scaffold(
        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                tabNavController.navigate(item.tab.name) {
                                    popUpTo(tabNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = Tab.HOME.name,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.HOME.name) { HomeScreen() }
            composable(Tab.CATALOG.name) { HomeScreen() } // TODO: CatalogScreen
            composable(Tab.EDITOR.name) { HomeScreen() } // TODO: EditorScreen
            composable(Tab.SOCIAL.name) { HomeScreen() } // TODO: SocialScreen
            composable(Tab.ACCOUNT.name) { AccountScreen(onSignOut = onSignOut) }
        }
    }
}
