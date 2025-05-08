package com.cite012a_cs32s1.ciphertrigger

import android.app.Application
import com.cite012a_cs32s1.ciphertrigger.di.AppModule

/**
 * Application class for CipherTrigger
 */
class CipherTriggerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the dependency injection
        AppModule.initialize(applicationContext)
    }
}
