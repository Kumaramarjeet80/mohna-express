package com.mohnaexpress.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mohnaexpress.app.ui.components.BottomNavBar
import com.mohnaexpress.app.ui.components.MohnaPopup
import com.mohnaexpress.app.ui.components.NavDestination
import com.mohnaexpress.app.ui.screens.*
import com.mohnaexpress.app.ui.theme.MohnaExpressTheme
import com.mohnaexpress.app.viewmodel.MainViewModel
import com.mohnaexpress.app.viewmodel.PopupType
import com.razorpay.PaymentResultListener

class MainActivity : ComponentActivity(), PaymentResultListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val isWholesaleMode by viewModel.isWholesaleMode.collectAsState()
            val popupState by viewModel.popupState.collectAsState()
            val cartItems by viewModel.cartItems.collectAsState()
            var currentNav by remember { mutableStateOf(NavDestination.HOME) }
            var isAuthScreenVisible by remember { mutableStateOf(false) }

            // Location permission request launcher
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                if (fineGranted || coarseGranted) {
                    fetchCurrentLocation()
                }
            }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fetchCurrentLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            MohnaExpressTheme(isWholesaleMode = isWholesaleMode) {
                Scaffold(
                    bottomBar = {
                        if (!isAuthScreenVisible) {
                            BottomNavBar(
                                currentDestination = currentNav,
                                cartItemCount = cartItems.sumOf { it.quantity },
                                onDestinationSelected = { currentNav = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isAuthScreenVisible) {
                            AuthScreen(
                                viewModel = viewModel,
                                onNavigateBack = { isAuthScreenVisible = false },
                                onAuthSuccess = { isAuthScreenVisible = false }
                            )
                        } else {
                            Crossfade(targetState = currentNav, label = "screen_crossfade") { destination ->
                                when (destination) {
                                    NavDestination.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToCheckout = { currentNav = NavDestination.CHECKOUT },
                                        onNavigateToAuth = { isAuthScreenVisible = true }
                                    )
                                    NavDestination.CHECKOUT -> CheckoutScreen(
                                        viewModel = viewModel,
                                        onNavigateToOrders = { currentNav = NavDestination.ORDERS },
                                        onNavigateToHome = { currentNav = NavDestination.HOME }
                                    )
                                    NavDestination.ORDERS -> OrdersScreen(
                                        viewModel = viewModel,
                                        onNavigateToHome = { currentNav = NavDestination.HOME }
                                    )
                                    NavDestination.PROFILE -> ProfileScreen(
                                        viewModel = viewModel,
                                        onNavigateToAuth = { isAuthScreenVisible = true }
                                    )
                                }
                            }
                        }

                        // Central Mohna Custom Popup Modal
                        MohnaPopup(
                            state = popupState,
                            onDismiss = { viewModel.dismissPopup() }
                        )
                    }
                }
            }
        }
    }

    private fun fetchCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.setLocation(location.latitude, location.longitude)
                    } else {
                        // Fallback to Munger/Patna coordinates inside delivery zone
                        viewModel.setLocation(25.3758, 86.4735)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Razorpay Callbacks
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        viewModel.placeOrder(onSuccess = {})
    }

    override fun onPaymentError(code: Int, response: String?) {
        viewModel.showPopup(
            title = "Payment Failed",
            message = response ?: "The payment could not be processed. Please try again.",
            type = PopupType.ERROR,
            primaryButtonText = "Retry / Place Cash Order",
            secondaryButtonText = "Cancel",
            onPrimary = { viewModel.placeOrder(onSuccess = {}) }
        )
    }
}
