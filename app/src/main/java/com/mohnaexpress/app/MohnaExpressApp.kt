package com.mohnaexpress.app

import android.app.Application
import com.razorpay.Checkout

class MohnaExpressApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Preload Razorpay checkout resources for instant opening
        try {
            Checkout.preload(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
