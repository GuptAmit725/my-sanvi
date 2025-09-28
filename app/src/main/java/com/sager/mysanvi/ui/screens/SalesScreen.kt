package com.sager.mysanvi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.sager.mysanvi.data.api.ApiManager
import com.sager.mysanvi.data.api.TokenManager
import com.sager.mysanvi.data.api.SalesRecord
import com.sager.mysanvi.data.api.SalesRecordRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateBack: () -> Unit
) {
    var salesRecords by remember { mutableStateOf<List<SalesRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Load sales records on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val authHeader = TokenManager.getAuthHeader()
                if (authHeader != null) {
                    salesRecords = ApiManager.saGerApiService.getSalesRecords(authHeader)
                } else {
                    errorMessage = "No authentication token found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load sales: ${e.message}"
            } finally {
                isLoading = false
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
                    text = "Sales Records",
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
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Sale"
                    )
                }
            }
        )

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading sales...")
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading sales",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val authHeader = TokenManager.getAuthHeader()
                                        if (authHeader != null) {
                                            salesRecords = ApiManager.saGerApiService.getSalesRecords(authHeader)
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to load sales: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "No sales records yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Add your first sale using the + button above",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(salesRecords) { sale ->
                        SalesRecordCard(sale = sale)
                    }
                }
            }
        }
    }

    // Add Sales Dialog
    if (showAddDialog) {
        AddSalesDialog(
            onDismiss = { showAddDialog = false },
            onSaleAdded = { newSale ->
                salesRecords = salesRecords + newSale
                showAddDialog = false
            }
        )
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.product_bought,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sale.paid)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Customer: ${sale.customer_id}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        color = if (sale.paid)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Date: ${sale.date}",
                        fontSize = 14.sp,
                        color = if (sale.paid)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${sale.amount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sale.paid)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = if (sale.paid) "PAID" else "UNPAID",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (sale.paid)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (!sale.payment_mode.isNullOrEmpty()) {
                Text(
                    text = "Payment: ${sale.payment_mode}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    color = if (sale.paid)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun AddSalesDialog(
    onDismiss: () -> Unit,
    onSaleAdded: (SalesRecord) -> Unit
) {
    var customerId by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Sale") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = customerId,
                    onValueChange = { customerId = it },
                    label = { Text("Customer Phone") },
                    placeholder = { Text("9876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Product") },
                    placeholder = { Text("Rice 5kg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("250.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = paid,
                        onCheckedChange = { paid = it }
                    )
                    Text("Payment received")
                }

                if (paid) {
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = { paymentMode = it },
                        label = { Text("Payment Mode") },
                        placeholder = { Text("cash, upi, card") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerId.isBlank() || product.isBlank() || amount.isBlank()) {
                        errorMessage = "Please fill all required fields"
                        return@Button
                    }

                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val authHeader = TokenManager.getAuthHeader()
                            if (authHeader != null) {
                                val newSale = ApiManager.saGerApiService.createSalesRecord(
                                    authHeader,
                                    SalesRecordRequest(
                                        customer_id = customerId.trim(),
                                        date = java.time.LocalDate.now().toString(),
                                        product_bought = product.trim(),
                                        amount = amountValue,
                                        paid = paid,
                                        payment_mode = if (paid && paymentMode.isNotBlank()) paymentMode.trim() else null
                                    )
                                )
                                onSaleAdded(newSale)
                            } else {
                                errorMessage = "No authentication token found"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Failed to add sale: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Sale")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}