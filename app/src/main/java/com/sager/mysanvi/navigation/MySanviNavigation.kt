package com.sager.mysanvi.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sager.mysanvi.ui.screens.DashboardScreen
import com.sager.mysanvi.ui.screens.LoginScreen

@Composable
fun MySanviNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var userPhone by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userStatusResponse ->
                    // Extract phone from the response
                    userPhone = userStatusResponse.phone
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            userPhone?.let { phone ->
                DashboardScreen(
                    userPhone = phone,
                    onLogout = {
                        userPhone = null
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}