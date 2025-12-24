package com.dabi.easylocalgame_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import com.dabi.easylocalgame_kmp.ui.ClientScreen
import com.dabi.easylocalgame_kmp.ui.HomeScreen
import com.dabi.easylocalgame_kmp.ui.ServerScreen
import com.dabi.easylocalgame_kmp.viewmodel.DemoClientViewModel
import com.dabi.easylocalgame_kmp.viewmodel.DemoServerViewModel
import org.koin.compose.koinInject

/**
 * Navigation screens in the app.
 */
enum class Screen {
    HOME,
    SERVER,
    CLIENT
}

/**
 * Main App composable.
 * 
 * This is the entry point for the Compose UI.
 * Uses Koin for dependency injection of ViewModels.
 */
@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        
        // Get ViewModels from Koin
        val serverViewModel: DemoServerViewModel = koinInject()
        val clientViewModel: DemoClientViewModel = koinInject()
        
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    onServerClick = { currentScreen = Screen.SERVER },
                    onClientClick = { currentScreen = Screen.CLIENT }
                )
            }
            Screen.SERVER -> {
                ServerScreen(
                    viewModel = serverViewModel,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.CLIENT -> {
                ClientScreen(
                    viewModel = clientViewModel,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
        }
    }
}