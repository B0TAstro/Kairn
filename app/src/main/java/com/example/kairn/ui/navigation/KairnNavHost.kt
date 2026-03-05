package com.example.kairn.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kairn.domain.model.Hike
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.explore.ExploreScreen
import com.example.kairn.ui.explore.ExploreViewModel
import com.example.kairn.ui.explore.HikeDetailScreenWithCta
import com.example.kairn.ui.home.HomeScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun KairnNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.HOME,
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination.name,
            modifier = modifier,
        ) {
            composable(Screen.HOME.name) {
                HomeScreen()
            }

            composable(Screen.EXPLORE.name) { backStackEntry ->
                val exploreViewModel: ExploreViewModel = hiltViewModel(backStackEntry)
                ExploreScreen(
                    onHikeClick = { hikeId ->
                        navController.navigate(NavRoutes.hikeDetail(hikeId))
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    viewModel = exploreViewModel,
                )
            }

            composable(Screen.CHAT.name) {
                HomeScreen() // TODO: Replace with ChatScreen
            }

            composable(Screen.PROFILE.name) {
                AccountScreen(
                    onSignOut = {
                        navController.navigate(Screen.PROFILE.name)
                    },
                )
            }

            composable(
                route = NavRoutes.HIKE_DETAIL,
                arguments = listOf(navArgument("hikeId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val hikeId = backStackEntry.arguments?.getString("hikeId") ?: return@composable

                val exploreEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.EXPLORE.name)
                }
                val exploreViewModel: ExploreViewModel = hiltViewModel(exploreEntry)

                val hike: Hike = exploreViewModel.selectedHike
                    ?: exploreViewModel.uiState.value.allHikes.find { it.id == hikeId }
                    ?: Hike.preview

                HikeDetailScreenWithCta(
                    hike = hike,
                    onBack = { navController.popBackStack() },
                    onStartTrip = { navController.popBackStack() },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                )
            }
        }
    }
}
