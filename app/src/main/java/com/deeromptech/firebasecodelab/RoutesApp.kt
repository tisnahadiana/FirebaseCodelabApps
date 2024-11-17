package com.deeromptech.firebasecodelab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deeromptech.firebasecodelab.feature.auth.login.LoginScreen
import com.deeromptech.firebasecodelab.feature.auth.signup.RegisterScreen
import com.deeromptech.firebasecodelab.feature.home.HomeScreen
import com.deeromptech.firebasecodelab.feature.profile.ProfileScreen
import com.deeromptech.firebasecodelab.utils.Constant
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val start = if (currentUser != null) Constant.HOME else Constant.LOGIN

        Scaffold(
            bottomBar = {
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentRoute != Constant.LOGIN && currentRoute != Constant.REGISTER) {
                    BottomNavigationBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = start,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Constant.LOGIN) {
                    LoginScreen(navController)
                }
                composable(Constant.REGISTER) {
                    RegisterScreen(navController)
                }
                composable(Constant.HOME) {
                    HomeScreen(navController)
                }
                composable(Constant.DATA) {
//                    DataScreen(navController)
                }
                composable(Constant.PROFILE) {
                    ProfileScreen(navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Constant.HOME, Icons.Default.Home, "Home"),
        BottomNavItem(Constant.DATA, Icons.AutoMirrored.Filled.List, "Data"),
        BottomNavItem(Constant.PROFILE, Icons.Default.Person, "Profile")
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                label = {
                    Text(item.label)
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)