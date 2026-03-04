package com.example.kairn.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.kairn.R
import com.example.kairn.domain.model.SessionState
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.auth.AuthUiState
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
// Root navigation (auth-first, reactive)
// ---------------------------------------------------------------------------

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

    when (sessionState) {
        is SessionState.Loading -> {
            // SDK is restoring the persisted session — show a splash / loading indicator
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is SessionState.Authenticated -> {
            AuthenticatedNavigation(
                authViewModel = authViewModel,
                modifier = modifier,
            )
        }

        is SessionState.NotAuthenticated -> {
            UnauthenticatedNavigation(
                authViewModel = authViewModel,
                modifier = modifier,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Authenticated flow — main scaffold with bottom navigation
// ---------------------------------------------------------------------------

@Composable
private fun AuthenticatedNavigation(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    MainScaffold(
        onSignOut = { authViewModel.signOut() },
        modifier = modifier,
    )
}

// ---------------------------------------------------------------------------
// Unauthenticated flow — sign in / sign up
// ---------------------------------------------------------------------------

@Composable
private fun UnauthenticatedNavigation(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // When sign-in succeeds the sessionState will flip to Authenticated,
    // which causes AppNavigation to recompose into AuthenticatedNavigation.
    // We just need to reset the UI state so it's clean for next time.
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.clearError()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route,
        modifier = modifier,
    ) {
        authGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(Screen.SignIn.route) {
        LoginScreen(
            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
            onSignInSuccess = {
                // No manual navigation needed — the sessionState flow will
                // automatically recompose AppNavigation to show MainScaffold.
            },
        )
    }

    composable(Screen.SignUp.route) {
        SignUpScreen(
            onNavigateToSignIn = { navController.navigateUp() },
            onSignUpSuccess = {
                // With "Confirm email" disabled, Supabase creates a session
                // automatically after sign-up. The sessionState flow will flip
                // to Authenticated, causing AppNavigation to recompose into
                // MainScaffold — no manual navigation needed.
            },
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
    modifier: Modifier = Modifier,
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
        modifier = modifier,
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
