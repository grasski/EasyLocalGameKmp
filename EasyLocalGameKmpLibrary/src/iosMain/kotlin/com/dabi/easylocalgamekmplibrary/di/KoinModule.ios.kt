package com.dabi.easylocalgamekmplibrary.di

import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import org.koin.dsl.module

/**
 * iOS-specific Koin module providing NearbyConnectionsManager.
 *
 * Note: On iOS, you must call NearbyConnectionsManager.setSwiftBridge()
 * with your Swift implementation before using the manager.
 *
 * Usage in your iOS app:
 * ```swift
 * // In AppDelegate or similar
 * let koin = KoinModule_iosKt.easyLocalGameModule
 * // Initialize Koin with the module
 * 
 * // Then set the Swift bridge
 * let manager = koin.get(NearbyConnectionsManager.self)
 * manager.setSwiftBridge(mySwiftBridge)
 * ```
 */
actual val easyLocalGameModule = module {
    single { NearbyConnectionsManager() }
}
