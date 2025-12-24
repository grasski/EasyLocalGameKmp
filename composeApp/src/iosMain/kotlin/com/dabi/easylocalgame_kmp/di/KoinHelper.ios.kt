package com.dabi.easylocalgame_kmp.di

import com.dabi.easylocalgamekmplibrary.di.easyLocalGameModule
import org.koin.core.context.startKoin

/**
 * iOS Koin initialization helper.
 * Call this from Swift before using the Compose UI.
 */
fun initKoinIOS() {
    startKoin {
        modules(appModule)
    }
}
