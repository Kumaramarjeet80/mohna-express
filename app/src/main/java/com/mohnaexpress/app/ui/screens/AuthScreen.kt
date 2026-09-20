package com.mohnaexpress.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohnaexpress.app.ui.theme.*
import com.mohnaexpress.app.viewmodel.MainViewModel
import com.mohnaexpress.app.viewmodel.PopupType

enum class AuthTab {
    LOGIN, SIGNUP
}

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mohnaColors = LocalMohnaColors.current
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Signup Form State
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    val userLat by viewModel.userLat.collectAsState()
    val userLng by viewModel.userLng.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mohnaColors.backgroundGradient)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = mohnaColors.primaryText
                )
            }
            Text(
                text = "⚡ Mohna Express Account",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = mohnaColors.primaryText
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector: Log In vs Sign Up
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(2.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = CardSurfaceWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val loginBg by animateColorAsState(
                    targetValue = if (selectedTab == AuthTab.LOGIN) mohnaColors.accentPrimary else Color.Transparent,
                    label = "login_tab_bg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(loginBg)
                        .clickable { selectedTab = AuthTab.LOGIN },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log In (Password)",
                        color = if (selectedTab == AuthTab.LOGIN) Color.White else RetailPrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val signupBg by animateColorAsState(
                    targetValue = if (selectedTab == AuthTab.SIGNUP) mohnaColors.accentPrimary else Color.Transparent,
                    label = "signup_tab_bg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(signupBg)
                        .clickable { selectedTab = AuthTab.SIGNUP },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up (OTP)",
                        color = if (selectedTab == AuthTab.SIGNUP) Color.White else RetailPrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Auth Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = CardSurfaceWhite,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == AuthTab.LOGIN) {
                    // LOGIN FLOW
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = mohnaColors.primaryText
                        )
                    )
                    Text(
                        text = "Sign in to access order tracking, wallet credits, and faster delivery.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )

                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email or Phone") },
                        placeholder = { Text("name@example.com") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                viewModel.showPopup(
                                    title = "Required Fields",
                                    message = "Please enter both email and password.",
                                    type = PopupType.WARN
                                )
                            } else {
                                viewModel.loginWithPassword(
                                    email = loginEmail.trim(),
                                    pass = loginPassword.trim(),
                                    onSuccess = onAuthSuccess
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mohnaColors.accentPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Log In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                } else {
                    // SIGNUP FLOW
                    Text(
                        text = "Create New Account",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = mohnaColors.primaryText
                        )
                    )
                    Text(
                        text = "Register with 6-digit OTP verification and lock your delivery GPS.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )

                    OutlinedTextField(
                        value = signupName,
                        onValueChange = { signupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full Name") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = signupEmail,
                        onValueChange = { signupEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email Address") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = signupPhone,
                        onValueChange = { signupPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = signupPassword,
                        onValueChange = { signupPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Create Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Locked GPS Coordinates Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "🔒", fontSize = 12.sp)
                                Text(
                                    text = "Locked Delivery GPS Coordinates",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RetailPrimaryText
                                )
                            }
                            Text(
                                text = "Lat: %.4f, Lng: %.4f".format(userLat, userLng),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = userAddress,
                                fontSize = 10.sp,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }

                    if (!isOtpSent) {
                        Button(
                            onClick = {
                                if (signupEmail.isBlank() || signupPhone.isBlank() || signupName.isBlank()) {
                                    viewModel.showPopup(
                                        title = "Missing Details",
                                        message = "Please complete all fields to receive OTP.",
                                        type = PopupType.WARN
                                    )
                                } else {
                                    viewModel.sendSignupOtp(
                                        email = signupEmail.trim(),
                                        phone = signupPhone.trim(),
                                        onSent = { isOtpSent = true }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mohnaColors.accentPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Send 6-Digit OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        // OTP Input
                        OutlinedTextField(
                            value = signupOtp,
                            onValueChange = { signupOtp = it.take(6) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Enter 6-Digit OTP") },
                            placeholder = { Text("123456") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (signupOtp.length < 6) {
                                    viewModel.showPopup(
                                        title = "Invalid OTP",
                                        message = "Please enter the complete 6-digit OTP.",
                                        type = PopupType.WARN
                                    )
                                } else {
                                    viewModel.completeSignup(
                                        name = signupName.trim(),
                                        email = signupEmail.trim(),
                                        phone = signupPhone.trim(),
                                        pass = signupPassword.trim(),
                                        otp = signupOtp.trim(),
                                        onSuccess = onAuthSuccess
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusDeliverableText,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Verify & Complete Signup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
