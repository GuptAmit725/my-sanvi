package com.sager.mysanvi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.sager.mysanvi.data.api.ApiManager
import com.sager.mysanvi.data.api.TokenManager
import com.sager.mysanvi.data.api.SalesRecord
import com.sager.mysanvi.data.api.DebtSummary
import com.sager.mysanvi.data.api.Prediction
import com.sager.mysanvi.data.api.DailySummaryResponse
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProfileScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Sales", "Debts", "Predictions", "Analytics")

    // Data states
    var salesRecords by remember { mutableStateOf<List<SalesRecord>>(emptyList()) }
    var debts by remember { mutableStateOf<List<DebtSummary>>(emptyList()) }
    var predictions by remember { mutableStateOf<List<Prediction>>(emptyList()) }
    var dailySummary by remember { mutableStateOf<DailySummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Load data based on selected tab
    LaunchedEffect(selectedTab) {
        if (selectedTab > 0) { // Don't load for Overview tab
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val authHeader = TokenManager.getAuthHeader()
                    if (authHeader != null) {
                        when (selectedTab) {
                            1 -> salesRecords = ApiManager.saGerApiService.getSalesRecords(authHeader)
                            2 -> debts = ApiManager.saGerApiService.getDebts(authHeader)
                            3 -> predictions = ApiManager.saGerApiService.getPredictions(authHeader)
                            4 -> dailySummary = ApiManager.saGerApiService.getDailySummary(authHeader)
                        }
                    } else {
                        errorMessage = "Authentication required"
                    }
                } catch (e: Exception) {
                    errorMessage = "Failed to load data: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "SaGer - Sales Manager",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* SSO to Mandii functionality */ }) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open on Mandii"
                    )
                }
            }
        )

        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> OverviewContent()
            1 -> SalesContent(salesRecords, isLoading, errorMessage) {
                // Refresh action
                scope.launch {
                    isLoading = true
                    try {
                        val authHeader = TokenManager.getAuthHeader()
                        if (authHeader != null) {
                            salesRecords = ApiManager.saGerApiService.getSalesRecords(authHeader)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Failed to refresh: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }
            2 -> DebtsContent(debts, isLoading, errorMessage)
            3 -> PredictionsContent(predictions, isLoading, errorMessage)
            4 -> AnalyticsContent(dailySummary, isLoading, errorMessage)
        }
    }
}

@Composable
private fun OverviewContent() {
    var todaysSales by remember { mutableStateOf(0.0) }
    var todaysTransactions by remember { mutableStateOf(0) }
    var isLoadingTodayData by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Load today's sales data
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val authHeader = TokenManager.getAuthHeader()
                if (authHeader != null) {
                    // Get today's data from daily summary (last day)
                    val dailySummary = ApiManager.saGerApiService.getDailySummary(authHeader, days = 1)
                    val todayData = dailySummary.totals.lastOrNull()
                    if (todayData != null) {
                        todaysSales = todayData.total_amount
                        todaysTransactions = todayData.total_count
                    }
                }
            } catch (e: Exception) {
                // Use default values if API call fails
                todaysSales = 1250.0 // Fallback demo value
                todaysTransactions = 8
            } finally {
                isLoadingTodayData = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Two Cards Row - Profile and Today's Sales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card - Shop Profile
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar (same style as SaGer)
                    Card(
                        modifier = Modifier.size(60.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Demo General Store",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "Demo Shop Owner",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "9876543210",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = androidx.compose.ui.graphics.Color.Green
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified",
                            fontSize = 11.sp,
                            color = androidx.compose.ui.graphics.Color.Green,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right Card - Today's Sales
            Card(
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
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Today's Sales",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    if (isLoadingTodayData) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Text(
                            text = "₹${String.format("%.0f", todaysSales)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "$todaysTransactions transactions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Color.Green,
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats Grid (remaining as before)
        Text(
            text = "Quick Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Total Sales",
                value = "₹12,450",
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending",
                value = "₹2,340",
                icon = Icons.Default.MonetizationOn,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Customers",
                value = "28",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Products",
                value = "156",
                icon = Icons.Default.Inventory,
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Activity
        Text(
            text = "Recent Activity",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ActivityItem("New sale recorded", "Rice 5kg - ₹250", "2 hours ago")
                ActivityItem("Payment received", "Customer 9876543210 - ₹500", "5 hours ago")
                ActivityItem("Debt reminder sent", "Customer 9123456789", "1 day ago")
            }
        }
    }
}
@Composable
private fun SalesContent(
    salesRecords: List<SalesRecord>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sales Records",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage)
                        Button(onClick = onRefresh, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Retry")
                        }
                    }
                }
            }
            salesRecords.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sales records found")
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(salesRecords) { sale ->
                        SalesRecordCard(sale)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtsContent(
    debts: List<DebtSummary>,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Debt Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage)
                }
            }
            debts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No pending debts")
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(debts) { debt ->
                        DebtCard(debt)
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionsContent(
    predictions: List<Prediction>,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Product Predictions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage)
                }
            }
            predictions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No predictions available")
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predictions) { prediction ->
                        PredictionCard(prediction)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    dailySummary: DailySummaryResponse?,
    isLoading: Boolean,
    errorMessage: String?
) {
    var expandedProductId by remember { mutableStateOf<String?>(null) }
    var productComparisons by remember { mutableStateOf<Map<String, ProductComparison>>(emptyMap()) }
    var isLoadingComparisons by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(errorMessage)
            }
        }
        dailySummary == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No analytics data available")
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Analytics Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text(
                        text = "Top Products",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(dailySummary.products) { product ->
                    ProductAnalyticsCardWithCompare(
                        product = product,
                        isExpanded = expandedProductId == product.product,
                        comparison = productComparisons[product.product],
                        isLoadingComparison = isLoadingComparisons,
                        onCompareClick = { productName ->
                            if (expandedProductId == productName) {
                                expandedProductId = null
                            } else {
                                expandedProductId = productName
                                isLoadingComparisons = true

                                scope.launch {
                                    try {
                                        // Simulate API call to get comparison data
                                        // In real implementation, this would call your Django API
                                        kotlinx.coroutines.delay(1000)

                                        val comparison = generateMockComparison(product)
                                        productComparisons = productComparisons + (productName to comparison)
                                    } catch (e: Exception) {
                                        // Handle error
                                    } finally {
                                        isLoadingComparisons = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductAnalyticsCardWithCompare(
    product: com.sager.mysanvi.data.api.ProductSummary,
    isExpanded: Boolean,
    comparison: ProductComparison?,
    isLoadingComparison: Boolean,
    onCompareClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main product info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.product,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${product.sales_count} sales",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${String.format("%.0f", product.total_amount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Compare button
                    Button(
                        onClick = { onCompareClick(product.product) },
                        modifier = Modifier.padding(top = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpanded)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "Hide" else "Compare",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Expanded comparison section
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingComparison) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading comparison...")
                        }
                    }
                } else if (comparison != null) {
                    ProductComparisonSection(comparison)
                }
            }
        }
    }
}

@Composable
private fun ProductComparisonSection(comparison: ProductComparison) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "vs Previous Period",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        // Sales comparison
        ComparisonRow(
            label = "Sales Volume",
            current = "${comparison.currentSales} units",
            previous = "${comparison.previousSales} units",
            change = comparison.salesChange,
            isPositiveGood = true
        )

        // Revenue comparison
        ComparisonRow(
            label = "Revenue",
            current = "₹${String.format("%.0f", comparison.currentRevenue)}",
            previous = "₹${String.format("%.0f", comparison.previousRevenue)}",
            change = comparison.revenueChange,
            isPositiveGood = true
        )

        // Debt comparison (if applicable)
        if (comparison.currentDebt > 0 || comparison.previousDebt > 0) {
            ComparisonRow(
                label = "Outstanding Debt",
                current = "₹${String.format("%.0f", comparison.currentDebt)}",
                previous = "₹${String.format("%.0f", comparison.previousDebt)}",
                change = comparison.debtChange,
                isPositiveGood = false // For debt, decrease is good
            )
        }

        // Performance indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (comparison.overallTrend > 0)
                    androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.1f)
                else
                    androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (comparison.overallTrend > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (comparison.overallTrend > 0) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (comparison.overallTrend > 0) "Performing Well" else "Needs Attention",
                    fontWeight = FontWeight.Medium,
                    color = if (comparison.overallTrend > 0) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    current: String,
    previous: String,
    change: Double,
    isPositiveGood: Boolean
) {
    val isImprovement = if (isPositiveGood) change > 0 else change < 0
    val changeColor = if (isImprovement) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
    val changeIcon = if (isImprovement) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current: $current",
                    fontSize = 13.sp
                )
                Text(
                    text = "Previous: $previous",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = changeIcon,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${if (change > 0) "+" else ""}${String.format("%.1f", change)}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = changeColor
                )
            }
        }
    }
}

// Data class for comparison
data class ProductComparison(
    val productName: String,
    val currentSales: Int,
    val previousSales: Int,
    val currentRevenue: Double,
    val previousRevenue: Double,
    val currentDebt: Double,
    val previousDebt: Double,
    val salesChange: Double,
    val revenueChange: Double,
    val debtChange: Double,
    val overallTrend: Double
)

// Mock function to generate comparison data
// Replace the random() calls with Random.nextDouble()
private fun generateMockComparison(product: com.sager.mysanvi.data.api.ProductSummary): ProductComparison {
    val previousSales = (product.sales_count * Random.nextDouble(0.8, 1.2)).toInt()
    val previousRevenue = product.total_amount * Random.nextDouble(0.7, 1.3)
    val currentDebt = product.total_amount * Random.nextDouble(0.1, 0.3)
    val previousDebt = currentDebt * Random.nextDouble(0.8, 1.5)

    val salesChange = if (previousSales > 0) ((product.sales_count - previousSales).toDouble() / previousSales * 100) else 0.0
    val revenueChange = if (previousRevenue > 0) ((product.total_amount - previousRevenue) / previousRevenue * 100) else 0.0
    val debtChange = if (previousDebt > 0) ((currentDebt - previousDebt) / previousDebt * 100) else 0.0

    val overallTrend = (salesChange + revenueChange - debtChange) / 3

    return ProductComparison(
        productName = product.product,
        currentSales = product.sales_count,
        previousSales = previousSales,
        currentRevenue = product.total_amount,
        previousRevenue = previousRevenue,
        currentDebt = currentDebt,
        previousDebt = previousDebt,
        salesChange = salesChange,
        revenueChange = revenueChange,
        debtChange = debtChange,
        overallTrend = overallTrend
    )
}
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ActivityItem(title: String, subtitle: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Text(time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun SalesRecordCard(sale: SalesRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (sale.paid)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sale.product_bought, fontWeight = FontWeight.Bold)
                Text("Customer: ${sale.customer_id}", fontSize = 14.sp)
                Text("Date: ${sale.date}", fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${sale.amount}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(if (sale.paid) "PAID" else "UNPAID", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DebtCard(debt: DebtSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Customer: ${debt.customer_id}", fontWeight = FontWeight.Bold)
                Text("Oldest: ${debt.oldest_unpaid_date}", fontSize = 14.sp)
                Text("${debt.unpaid_count} unpaid transactions", fontSize = 12.sp)
            }
            Text(
                "₹${debt.total_unpaid_amount}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun PredictionCard(prediction: Prediction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Customer: ${prediction.customer_id}", fontWeight = FontWeight.Bold)
                Text("Predicted: ${prediction.predicted_product}", fontSize = 14.sp)
            }
            Text(
                "${(prediction.score * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProductAnalyticsCard(product: com.sager.mysanvi.data.api.ProductSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.product, fontWeight = FontWeight.Bold)
                Text("${product.sales_count} sales", fontSize = 14.sp)
            }
            Text(
                "₹${product.total_amount}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}