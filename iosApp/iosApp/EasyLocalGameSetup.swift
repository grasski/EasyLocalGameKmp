import Foundation
import EasyLocalGameKmpLibraryKit

/**
 * Helper class for initializing the EasyLocalGame library on iOS.
 *
 * Usage in your AppDelegate or main entry point:
 * ```swift
 * import EasyLocalGameKmpLibraryKit
 *
 * @main
 * struct MyApp: App {
 *     init() {
 *         EasyLocalGameSetup.initialize()
 *     }
 *     
 *     var body: some Scene {
 *         WindowGroup {
 *             ContentView()
 *         }
 *     }
 * }
 * ```
 */
public class EasyLocalGameSetup {
    
    private static var wrapper: NearbyConnectionsWrapper?
    private static var manager: NearbyConnectionsManager?
    
    /**
     * Initialize the EasyLocalGame library with the default NearbyConnections wrapper.
     *
     * Call this once during app initialization before using any EasyLocalGame functionality.
     */
    public static func initialize() {
        // Create the wrapper
        wrapper = NearbyConnectionsWrapper()
        
        // The manager will be obtained from Koin when ViewModels are created
        // For manual access, use getManager()
    }
    
    /**
     * Get a pre-configured NearbyConnectionsManager with the Swift bridge already set up.
     *
     * @return A configured NearbyConnectionsManager instance
     */
    public static func getConfiguredManager() -> NearbyConnectionsManager {
        if manager == nil {
            manager = NearbyConnectionsManager()
            if let w = wrapper {
                manager?.setSwiftBridge(bridge: w)
            } else {
                wrapper = NearbyConnectionsWrapper()
                manager?.setSwiftBridge(bridge: wrapper!)
            }
        }
        return manager!
    }
    
    /**
     * Configure an existing NearbyConnectionsManager with the Swift bridge.
     * Use this if the manager is obtained from Koin.
     *
     * @param manager The NearbyConnectionsManager to configure
     */
    public static func configure(manager: NearbyConnectionsManager) {
        if wrapper == nil {
            wrapper = NearbyConnectionsWrapper()
        }
        manager.setSwiftBridge(bridge: wrapper!)
    }
}
