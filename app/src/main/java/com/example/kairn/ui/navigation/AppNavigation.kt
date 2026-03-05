package com.example.kairn.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kairn.domain.model.SessionState
import com.example.kairn.ui.auth.AuthUiState
import com.example.kairn.ui.auth.AuthViewModel
import com.example.kairn.ui.auth.LoginScreen
import com.example.kairn.ui.auth.SignUpScreen
import com.example.kairn.ui.auth.onboarding.OnboardingScreen
import com.example.kairn.ui.components.KairnBottomNavBar
import com.example.kairn.ui.components.NavBarItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

private sealed class AuthRoute(val route: String) {
    data object Onboarding : AuthRoute("onboarding")
    data object Login : AuthRoute("login")
    data object SignUp : AuthRoute("signup")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

    when (sessionState) {
        SessionState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is SessionState.Authenticated -> {
            MainScreen(modifier = modifier)
        }

        is SessionState.NotAuthenticated -> {
            val isSignOut = (sessionState as SessionState.NotAuthenticated).isSignOut
            UnauthenticatedNavigation(
                authViewModel = authViewModel,
                isSignOut = isSignOut,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun UnauthenticatedNavigation(
    authViewModel: AuthViewModel,
    isSignOut: Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sharedImageAssetPath = remember {
        com.example.kairn.ui.auth.onboarding.pickRandomOnboardingImage(context)
    }
    val startDestination = if (isSignOut) AuthRoute.Login.route else AuthRoute.Onboarding.route

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.clearError()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authGraph(
            navController = navController,
            imageAssetPath = sharedImageAssetPath,
        )
    }
}

private fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    imageAssetPath: String?,
) {
    composable(AuthRoute.Onboarding.route) {
        OnboardingScreen(
            imageAssetPath = imageAssetPath,
            onNavigateToSignUp = { navController.navigate(AuthRoute.SignUp.route) },
            onNavigateToSignIn = { navController.navigate(AuthRoute.Login.route) },
        )
    }

    composable(AuthRoute.Login.route) {
        LoginScreen(
            imageAssetPath = imageAssetPath,
            onNavigateToSignUp = { navController.navigate(AuthRoute.SignUp.route) },
            onSignInSuccess = {
                // Session flow will switch AppNavigation to authenticated content.
            },
            onBack = {
                navController.popBackStack(AuthRoute.Onboarding.route, inclusive = false)
            },
        )
    }

    composable(AuthRoute.SignUp.route) {
        SignUpScreen(
            imageAssetPath = imageAssetPath,
            onNavigateToSignIn = {
                navController.navigate(AuthRoute.Login.route) {
                    popUpTo(AuthRoute.Onboarding.route)
                }
            },
            onSignUpSuccess = {
                // If email confirmation is required, keep user on auth flow.
                // If auto session is enabled, session flow will switch automatically.
                navController.navigate(AuthRoute.Login.route) {
                    popUpTo(AuthRoute.Onboarding.route)
                    launchSingleTop = true
                }
            },
            onBack = {
                navController.popBackStack(AuthRoute.Onboarding.route, inclusive = false)
            },
        )
    }
}

@Composable
private fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(Screen.HOME.name) }
    val hazeState = remember { HazeState() }
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomNav = currentRoute != NavRoutes.HIKE_DETAIL

    LaunchedEffect(currentRoute) {
        selectedItem = when (currentRoute) {
            Screen.HOME.name -> Screen.HOME.name
            Screen.EXPLORE.name, NavRoutes.HIKE_DETAIL -> Screen.EXPLORE.name
            Screen.CHAT.name -> Screen.CHAT.name
            Screen.PROFILE.name -> Screen.PROFILE.name
            else -> selectedItem
        }
    }

    val navBarItems = listOf(
        NavBarItem(Screen.HOME.name, "Home", Icons.Outlined.Home),
        NavBarItem(Screen.EXPLORE.name, "Explore", Icons.Outlined.Explore),
        NavBarItem(Screen.CHAT.name, "Chat", Icons.AutoMirrored.Outlined.Chat),
        NavBarItem(Screen.PROFILE.name, "Profile", Icons.Outlined.Person),
    )

    Box(modifier = modifier.fillMaxSize()) {
        KairnNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState),
        )

        if (showBottomNav) {
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
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
