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
import com.example.kairn.ui.account.AccountViewModel
import com.example.kairn.ui.account.EditProfileScreen
import com.example.kairn.ui.editor.EditorScreen
import com.example.kairn.ui.explore.ExploreScreen
import com.example.kairn.ui.explore.ExploreViewModel
import com.example.kairn.ui.explore.HikeDetailScreenWithCta
import com.example.kairn.ui.explore.StandaloneHikeDetailScreenWithCta
import com.example.kairn.ui.home.HomeScreen
import com.example.kairn.ui.chat.ChatListScreen
import com.example.kairn.ui.chat.ChatScreen
import com.example.kairn.ui.friends.FriendListScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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

            composable(Screen.EDITOR.name) {
                EditorScreen()
            }

            composable(Screen.CHAT.name) {
                ChatListScreen(
                    onNavigateToChat = { conversationId, conversationName ->
                        navController.navigate(NavRoutes.chat(conversationId, conversationName))
                    },
                    onNavigateToNewChat = {
                        navController.navigate(NavRoutes.FRIEND_LIST)
                    }
                )
            }

            composable(Screen.PROFILE.name) { backStackEntry ->
                val accountViewModel: AccountViewModel = hiltViewModel(backStackEntry)
                AccountScreen(
                    onSignOut = {
                        navController.navigate(Screen.PROFILE.name)
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(NavRoutes.EDIT_PROFILE)
                    },
                    onNavigateToHikeDetail = { hikeId ->
                        navController.navigate(NavRoutes.accountHikeDetail(hikeId))
                    },
                    viewModel = accountViewModel,
                )
            }

            composable(NavRoutes.EDIT_PROFILE) { backStackEntry ->
                val profileEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.PROFILE.name)
                }
                val accountViewModel: AccountViewModel = hiltViewModel(profileEntry)
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = accountViewModel,
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

            // ── Hike detail from Account (standalone, no shared transitions) ──
            composable(
                route = NavRoutes.ACCOUNT_HIKE_DETAIL,
                arguments = listOf(navArgument("hikeId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val hikeId = backStackEntry.arguments?.getString("hikeId") ?: return@composable

                val profileEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.PROFILE.name)
                }
                val accountViewModel: AccountViewModel = hiltViewModel(profileEntry)

                val hike: Hike = accountViewModel.completedHikes.value
                    .find { it.id == hikeId }
                    ?: Hike.preview

                StandaloneHikeDetailScreenWithCta(
                    hike = hike,
                    onBack = { navController.popBackStack() },
                    onStartTrip = { navController.popBackStack() },
                )
            }

            // Chat detail screen
            composable(
                route = NavRoutes.CHAT,
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.StringType },
                    navArgument("conversationName") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                val conversationNameEncoded =
                    backStackEntry.arguments?.getString("conversationName") ?: return@composable
                val conversationName = URLDecoder.decode(conversationNameEncoded, StandardCharsets.UTF_8.toString())

                ChatScreen(
                    conversationId = conversationId,
                    conversationName = conversationName,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // Friend list screen
            composable(NavRoutes.FRIEND_LIST) {
                FriendListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { conversationId, conversationName ->
                        navController.navigate(NavRoutes.chat(conversationId, conversationName)) {
                            popUpTo(Screen.CHAT.name)
                        }
                    },
                )
            }
        }
    }
}
