import SwiftUI
import ComposeApp
import EasyLocalGameKmpLibrary

@main
struct iOSApp: App {
    
    init() {
        // Initialize the EasyLocalGame library with Swift bridge
        EasyLocalGameSetup.initialize()
        
        // Initialize Koin for dependency injection
        KoinHelper_iosKt.initKoinIOS()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}