package com.sager.mysanvi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.sager.mysanvi.data.api.ApiManager
import com.sager.mysanvi.data.api.UserStatusResponse
import com.sager.mysanvi.data.api.SendOtpRequest
import com.sager.mysanvi.data.api.VerifyOtpRequest
import com.sager.mysanvi.data.api.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserStatusResponse) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(LoginStep.PHONE_INPUT) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "My Sanvi",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Unified access to SaGer & Mandii",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (currentStep) {
            LoginStep.PHONE_INPUT -> {
                PhoneInputStep(
                    phone = phone,
                    onPhoneChange = { phone = it; errorMessage = null; successMessage = null },
                    onSendOtp = {
                        if (phone.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    // Call real API
                                    val response = ApiManager.saGerApiService.sendOtp(
                                        SendOtpRequest(phone.trim())
                                    )
                                    successMessage = response.detail
                                    errorMessage = null
                                    currentStep = LoginStep.OTP_VERIFICATION

                                    // In debug mode, show OTP if available
                                    response.otp?.let { debugOtp ->
                                        successMessage += " (Debug OTP: $debugOtp)"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to send OTP: ${e.message}"
                                    successMessage = null
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Please enter your phone number"
                        }
                    },
                    isLoading = isLoading,
                    error = errorMessage,
                    success = successMessage
                )
            }
            LoginStep.OTP_VERIFICATION -> {
                OtpVerificationStep(
                    phone = phone,
                    otp = otp,
                    onOtpChange = { otp = it; errorMessage = null; successMessage = null },
                    onVerifyOtp = {
                        if (otp.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    // Call real verification API
                                    val response = ApiManager.saGerApiService.verifyOtp(
                                        VerifyOtpRequest(phone.trim(), otp.trim())
                                    )

                                    // Save tokens
                                    TokenManager.saveTokens(response.access, response.refresh)

                                    // Create user status response
                                    val userStatus = UserStatusResponse(
                                        phone = phone,
                                        sager = com.sager.mysanvi.data.api.SaGerUserInfo(
                                            exists = true,
                                            user_data = com.sager.mysanvi.data.api.SaGerUserData(
                                                id = response.user?.id,
                                                name = response.user?.name,
                                                shop_name = response.user?.shop_name,
                                                is_phone_verified = response.user?.is_phone_verified ?: true
                                            )
                                        ),
                                        mandii = com.sager.mysanvi.data.api.MandiiUserInfo(
                                            exists = false, // Will be checked later
                                            user_data = null
                                        )
                                    )

                                    onLoginSuccess(userStatus)
                                } catch (e: Exception) {
                                    errorMessage = "Verification failed: ${e.message}"
                                    successMessage = null
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Please enter the verification code"
                        }
                    },
                    onResendOtp = {
                        scope.launch {
                            try {
                                isLoading = true
                                val response = ApiManager.saGerApiService.sendOtp(
                                    SendOtpRequest(phone.trim())
                                )
                                successMessage = "OTP resent: ${response.detail}"
                                errorMessage = null

                                response.otp?.let { debugOtp ->
                                    successMessage += " (Debug OTP: $debugOtp)"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Failed to resend OTP: ${e.message}"
                                successMessage = null
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    onChangePhone = {
                        currentStep = LoginStep.PHONE_INPUT
                        otp = ""
                        errorMessage = null
                        successMessage = null
                    },
                    isLoading = isLoading,
                    error = errorMessage,
                    success = successMessage
                )
            }
        }
    }
}

@Composable
private fun PhoneInputStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    isLoading: Boolean,
    error: String?,
    success: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter your phone number",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            placeholder = { Text("+91 9876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (success != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSendOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && phone.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Send OTP")
        }
    }
}

@Composable
private fun OtpVerificationStep(
    phone: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onChangePhone: () -> Unit,
    isLoading: Boolean,
    error: String?,
    success: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter verification code",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We sent a code to $phone",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("OTP") },
            placeholder = { Text("123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (success != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && otp.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Verify & Continue")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onChangePhone,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Change Phone")
            }

            TextButton(
                onClick = onResendOtp,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Resend OTP")
            }
        }
    }
}

enum class LoginStep {
    PHONE_INPUT,
    OTP_VERIFICATION
}