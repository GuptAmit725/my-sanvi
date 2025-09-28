package com.sager.mysanvi.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sager.mysanvi.ui.screens.DashboardScreen
import com.sager.mysanvi.ui.screens.LoginScreen
import com.sager.mysanvi.ui.screens.SalesScreen
import com.sager.mysanvi.ui.screens.ShopProfileScreen
import com.sager.mysanvi.ui.screens.MandiiWebViewScreen
import java.net.URLDecoder

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
                    onNavigateToSales = {
                        navController.navigate("sales")
                    },
                    onNavigateToShopProfile = {
                        navController.navigate("shop_profile")
                    },
                    onNavigateToMandii = { url ->
                        navController.navigate("mandii_webview/$url")
                    },
                    onLogout = {
                        userPhone = null
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("sales") {
            SalesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("shop_profile") {
            ShopProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Add the Mandii WebView route
        composable(
            route = "mandii_webview/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val decodedUrl = URLDecoder.decode(encodedUrl, "UTF-8")

            MandiiWebViewScreen(
                url = decodedUrl,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}