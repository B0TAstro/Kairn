package com.example.kairn.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kairn.domain.model.Hike
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.catalogue.CatalogueScreen
import com.example.kairn.ui.catalogue.CatalogueViewModel
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

            composable(Screen.DETAILS.name) { backStackEntry ->
                // ViewModel scoped to this back stack entry so detail can read selectedHike
                val catalogueViewModel: CatalogueViewModel = viewModel(backStackEntry)
                CatalogueScreen(
                    onHikeClick = { hikeId ->
                        navController.navigate(NavRoutes.hikeDetail(hikeId))
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    viewModel = catalogueViewModel,
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

                // Retrieve the catalogue back stack entry to share its ViewModel
                val catalogueEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.DETAILS.name)
                }
                val catalogueViewModel: CatalogueViewModel = viewModel(catalogueEntry)

                // Use the hike stored by the catalogue, fallback to preview
                val hike: Hike = catalogueViewModel.selectedHike
                    ?: catalogueViewModel.uiState.value.allHikes.find { it.id == hikeId }
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
