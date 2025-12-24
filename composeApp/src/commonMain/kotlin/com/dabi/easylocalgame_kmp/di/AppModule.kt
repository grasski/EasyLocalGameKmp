package com.dabi.easylocalgame_kmp.di

import com.dabi.easylocalgame_kmp.viewmodel.DemoClientViewModel
import com.dabi.easylocalgame_kmp.viewmodel.DemoServerViewModel
import com.dabi.easylocalgamekmplibrary.di.easyLocalGameModule
import org.koin.dsl.module

/**
 * App-level Koin module.
 * 
 * Includes:
 * - easyLocalGameModule (from library) - provides NearbyConnectionsManager
 * - ViewModels that extend library templates
 */
val appModule = module {
    // Include the library's module which provides NearbyConnectionsManager
    includes(easyLocalGameModule)
    
    // Demo ViewModels - use factory for multiplatform compatibility
    // (viewModel{} is Android-only)
    factory { DemoServerViewModel(get()) }   // extends ServerViewModelTemplate
    factory { DemoClientViewModel(get()) }   // extends PlayerViewModelTemplate
}
