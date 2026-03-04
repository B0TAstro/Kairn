package com.example.kairn.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairn.domain.model.SessionState
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.auth.AuthUiState
import com.example.kairn.ui.auth.AuthViewModel
import com.example.kairn.ui.auth.LoginScreen
import com.example.kairn.ui.auth.OnboardingScreen
import com.example.kairn.ui.auth.SignUpScreen
import com.example.kairn.ui.home.HomeScreen

private sealed class AuthRoute(val route: String) {
    data object Onboarding : AuthRoute("onboarding")
    data object SignIn : AuthRoute("sign_in")
    data object SignUp : AuthRoute("sign_up")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

    when (sessionState) {
        is SessionState.Loading -> {
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

@Composable
private fun UnauthenticatedNavigation(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.clearError()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthRoute.Onboarding.route,
        modifier = modifier,
    ) {
        authGraph(navController = navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(AuthRoute.Onboarding.route) {
        OnboardingScreen(
            onNavigateToSignUp = { navController.navigate(AuthRoute.SignUp.route) },
            onNavigateToSignIn = { navController.navigate(AuthRoute.SignIn.route) },
        )
    }

    composable(AuthRoute.SignIn.route) {
        LoginScreen(
            onNavigateToSignUp = { navController.navigate(AuthRoute.SignUp.route) },
            onSignInSuccess = {
                // No manual navigation needed — sessionState drives the app flow.
            },
        )
    }

    composable(AuthRoute.SignUp.route) {
        SignUpScreen(
            onNavigateToSignIn = { navController.navigate(AuthRoute.SignIn.route) },
            onSignUpSuccess = {
                // No manual navigation needed — sessionState drives the app flow.
            },
        )
    }
}

private data class TabItem(
    val tab: Tab,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun MainScaffold(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabNavController = rememberNavController()
    var selectedIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem(Tab.HOME, "Home", Icons.Outlined.Home),
        TabItem(Tab.CATALOG, "Catalog", Icons.Outlined.Explore),
        TabItem(Tab.EDITOR, "Editor", Icons.Outlined.Edit),
        TabItem(Tab.SOCIAL, "Social", Icons.Outlined.People),
        TabItem(Tab.ACCOUNT, "Account", Icons.Outlined.Person),
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
                                    imageVector = item.icon,
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
