package com.example.kairn.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kairn.domain.model.Hike
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.catalogue.CatalogueScreen
import com.example.kairn.ui.catalogue.HikeDetailScreenWithCta
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
            composable(Screen.DETAILS.name) {
                CatalogueScreen(
                    onHikeClick = { hikeId ->
                        navController.navigate(NavRoutes.hikeDetail(hikeId))
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                )
            }
            composable(Screen.SAVED.name) {
                HomeScreen() // TODO: Replace with SavedScreen
            }
            composable(Screen.PROFILE.name) {
                AccountScreen()
            }
            composable(
                route = NavRoutes.HIKE_DETAIL,
                arguments = listOf(navArgument("hikeId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val hikeId = backStackEntry.arguments?.getString("hikeId") ?: return@composable
                val hike = Hike.previewList.find { it.id == hikeId }
                    ?: Hike.previewList.firstOrNull()
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
