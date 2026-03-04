package com.example.kairn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kairn.ui.components.KairnBottomNavBar
import com.example.kairn.ui.components.NavBarItem
import com.example.kairn.ui.navigation.KairnNavHost
import com.example.kairn.ui.navigation.NavRoutes
import com.example.kairn.ui.navigation.Screen
import com.example.kairn.ui.theme.KairnTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KairnTheme {
                KairnApp()
            }
        }
    }
}

@Composable
fun KairnApp() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(Screen.HOME.name) }
    val hazeState = remember { HazeState() }

    // Hide bottom nav on detail screen
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomNav = currentRoute != NavRoutes.HIKE_DETAIL

    val navBarItems = listOf(
        NavBarItem(Screen.HOME.name, "Home", Icons.Outlined.Home),
        NavBarItem(Screen.DETAILS.name, "Explore", Icons.Outlined.Explore),
        NavBarItem(Screen.SAVED.name, "Saved", Icons.Outlined.BookmarkBorder),
        NavBarItem(Screen.PROFILE.name, "Profile", Icons.Outlined.Person),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
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
}
