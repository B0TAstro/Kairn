package com.example.kairn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.home.HomeScreen
import com.example.kairn.ui.navigation.Screen
import com.example.kairn.ui.theme.KairnTheme

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
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavigationItem(Screen.HOME, "Home", R.drawable.ic_home),
        NavigationItem(Screen.CATALOG, "Catalog", R.drawable.ic_catalog),
        NavigationItem(Screen.EDITOR, "Editor", R.drawable.ic_editor),
        NavigationItem(Screen.SOCIAL, "Social", R.drawable.ic_social),
        NavigationItem(Screen.ACCOUNT, "Account", R.drawable.ic_account),
    )

    Scaffold(
        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                navController.navigate(item.screen.name) {
                                    popUpTo(navController.graph.findStartDestination().id) {
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
            navController = navController,
            startDestination = Screen.HOME.name,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.HOME.name) { HomeScreen() }
            composable(Screen.CATALOG.name) { HomeScreen() }
            composable(Screen.EDITOR.name) { HomeScreen() }
            composable(Screen.SOCIAL.name) { HomeScreen() }
            composable(Screen.ACCOUNT.name) { AccountScreen() }
        }
    }
}

data class NavigationItem(
    val screen: Screen,
    val label: String,
    val icon: Int,
)
