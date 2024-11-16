package com.deeromptech.firebasecodelab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deeromptech.firebasecodelab.feature.auth.login.LoginScreen
import com.deeromptech.firebasecodelab.feature.auth.signup.RegisterScreen
import com.deeromptech.firebasecodelab.feature.home.HomeScreen
import com.deeromptech.firebasecodelab.utils.Constant
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainApp() {
    Surface(modifier = Modifier.fillMaxSize()) {

        val navController = rememberNavController()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val start = if (currentUser != null) Constant.HOME else Constant.LOGIN

        NavHost(navController = navController, startDestination = start) {
            composable(Constant.LOGIN) {
                LoginScreen(navController)
            }
            composable(Constant.SIGNUP) {
                RegisterScreen(navController)
            }
            composable(Constant.HOME) {
                HomeScreen(navController)
            }
        }

    }
}