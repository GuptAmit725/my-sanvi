// Add these imports at the top of your DashboardScreen.kt file

package com.sager.mysanvi.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.sager.mysanvi.data.api.TokenManager
import com.sager.mysanvi.data.api.ApiManager

// Add these missing imports:
import java.net.URLEncoder
import java.nio.charset.StandardCharsets



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userPhone: String,
    onNavigateToSales: () -> Unit = {},
    onNavigateToShopProfile: () -> Unit = {},
    onNavigateToMandii: (String) -> Unit = {}, // Add this parameter if missing
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var isLoadingSager by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isLoadingMandii by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "My Sanvi",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Shop Profile Icon
                IconButton(onClick = onNavigateToShopProfile) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Shop Profile"
                    )
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userPhone,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (TokenManager.hasValidTokens())
                            "Authenticated with SaGer backend"
                        else
                            "Demo Mode - Authentication successful!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // App Cards Section
            Text(
                text = "Your Apps",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // SaGer Card - MODIFIED TO OPEN SHOP PROFILE
            AppCard(
                appName = "SaGer",
                description = "Sales Management & Analytics",
                accessType = if (TokenManager.hasValidTokens()) "AUTHENTICATED" else "LOGIN REQUIRED",
                cardColor = if (TokenManager.hasValidTokens())
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                textColor = if (TokenManager.hasValidTokens())
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                icon = if (TokenManager.hasValidTokens()) Icons.Default.Store else Icons.Default.Lock,
                isLoading = isLoadingSager,
                onClick = {
                    if (TokenManager.hasValidTokens()) {
                        // Navigate to Shop Profile instead of Sales
                        onNavigateToShopProfile()
                    } else {
                        // Try to open web interface for non-authenticated users
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://10.0.2.2:8000/"))
                        context.startActivity(intent)
                    }
                }
            )

            // Mandii Card
            AppCard(
                appName = "Mandii",
                description = "Connect with Customers & Community",
                accessType = "COMMUNITY ACCESS",
                cardColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                icon = Icons.Default.Store,
                isLoading = isLoadingMandii,
                onClick = {
                    isLoadingMandii = true
                    scope.launch {
                        try {
                            // Check if user exists in Mandii database
                            val userStatus = ApiManager.checkUserStatus(userPhone)

                            val mandiiUrl = if (userStatus.mandii.exists && userStatus.mandii.user_data?.shop_id != null) {
                                // User exists and has a shop - open their shop profile
                                "http://10.0.2.2:8001/api/feed/page"
                                //"http://10.0.2.2:8001/v1/shop/${userStatus.mandii.user_data.shop_id}/"
                            } else {
                                // User doesn't exist in Mandii or has no shop - open community feed
                                "http://10.0.2.2:8001/v1/feed/page/"
                            }

                            // Navigate to native WebView screen
                            val encodedUrl = URLEncoder.encode(mandiiUrl, StandardCharsets.UTF_8.toString())
                            onNavigateToMandii(encodedUrl)

                        } catch (e: Exception) {
                            // Fallback to community feed if API call fails
                            val fallbackUrl = URLEncoder.encode("http://10.0.2.2:8001/v1/feed/page/", StandardCharsets.UTF_8.toString())
                            onNavigateToMandii(fallbackUrl)
                        } finally {
                            isLoadingMandii = false
                        }
                    }
                }
            )

            // Quick Actions (if authenticated)
            if (TokenManager.hasValidTokens()) {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        onClick = onNavigateToSales,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sales",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Card(
                        onClick = onNavigateToShopProfile,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Profile",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Card(
                        onClick = { /* Add debt management navigation later */ },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Debts",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Status Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (TokenManager.hasValidTokens()) "Live Mode" else "Demo Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (TokenManager.hasValidTokens())
                            "Connected to live backend. Click SaGer to access your shop profile and manage your business."
                        else
                            "Start your Django backend and use real OTP to access live features.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppCard(
    appName: String,
    description: String,
    accessType: String,
    cardColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = textColor
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = accessType,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}