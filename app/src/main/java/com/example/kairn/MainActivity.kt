package com.example.kairn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairn.ui.account.AccountScreen
import com.example.kairn.ui.home.HomeScreen
import com.example.kairn.ui.navigation.Tab
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
        NavigationItem(Tab.HOME, "Home", R.drawable.ic_home),
        NavigationItem(Tab.CATALOG, "Catalog", R.drawable.ic_catalog),
        NavigationItem(Tab.EDITOR, "Editor", R.drawable.ic_editor),
        NavigationItem(Tab.SOCIAL, "Social", R.drawable.ic_social),
        NavigationItem(Tab.ACCOUNT, "Account", R.drawable.ic_account),
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
                                    contentDescription = item.label
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
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.HOME.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Tab.HOME.name) {
                HomeScreen()
            }
            composable(Tab.CATALOG.name) {
                HomeScreen()
            }
            composable(Tab.EDITOR.name) {
                HomeScreen()
            }
            composable(Tab.SOCIAL.name) {
                HomeScreen()
            }
            composable(Tab.ACCOUNT.name) {
                AccountScreen()
            }
        }
    }
}

data class NavigationItem(
    val screen: Tab,
    val label: String,
    val icon: Int,
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KairnTheme {
        KairnApp()
    }
}