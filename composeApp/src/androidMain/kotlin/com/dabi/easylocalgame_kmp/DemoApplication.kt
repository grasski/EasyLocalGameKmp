package com.dabi.easylocalgame_kmp

import android.app.Application
import com.dabi.easylocalgame_kmp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class for initializing Koin dependency injection.
 * 
 * IMPORTANT: This must be registered in AndroidManifest.xml:
 * <application android:name=".DemoApplication" ...>
 */
class DemoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            // Use Android context
            androidContext(this@DemoApplication)
            // Optional: Android logger for debugging
            androidLogger()
            // Load modules
            modules(appModule)
        }
    }
}
