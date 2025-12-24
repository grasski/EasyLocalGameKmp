package com.dabi.easylocalgamekmplibrary.di

import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import com.google.android.gms.nearby.Nearby
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module providing NearbyConnectionsManager.
 *
 * Usage in your app:
 * ```kotlin
 * startKoin {
 *     androidContext(applicationContext)
 *     modules(easyLocalGameModule)
 * }
 * ```
 */
actual val easyLocalGameModule = module {
    single {
        val connectionsClient = Nearby.getConnectionsClient(androidContext())
        NearbyConnectionsManager(connectionsClient)
    }
}
