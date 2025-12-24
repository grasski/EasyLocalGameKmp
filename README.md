# EasyLocalGame KMP

A Kotlin Multiplatform library for building local multiplayer games using Google Nearby Connections API, supporting both Android and iOS.

Based on the original [EasyLocalGame](https://github.com/grasski/EasyLocalGame) Android library.

## Features

- ✅ **Cross-platform** - Single codebase for Android and iOS
- ✅ **Jetpack Compose & SwiftUI compatible** - ViewModel templates work with Compose Multiplatform
- ✅ **Nearby Connections API** - Peer-to-peer communication without internet
- ✅ **Type-safe serialization** - Uses `kotlinx.serialization`
- ✅ **Dependency Injection** - Built-in Koin support

## Installation

### Android (via JitPack)

[![](https://jitpack.io/v/grasski/EasyLocalGameKmp.svg)](https://jitpack.io/#grasski/EasyLocalGameKmp)

**Step 1:** Add JitPack repository to your root `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2:** Add the dependency to your module's `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.github.grasski:EasyLocalGameKmp:1.0.0")
}
```

> **Note**: Replace `1.0.0` with the latest version or use `main-SNAPSHOT` for the latest development version.

### iOS

1. Add the KMP framework to your Xcode project
2. Add NearbyConnections SDK via CocoaPods:
   ```ruby
   pod 'NearbyConnections'
   ```
3. Copy Swift files from `EasyLocalGameKmpLibrary/src/iosMain/swift/` to your project
4. Initialize the bridge:
   ```swift
   import EasyLocalGameKmpLibraryKit
   
   // In your app initialization
   EasyLocalGameSetup.initialize()
   ```

## Usage

### Server ViewModel

```kotlin
class MyServerViewModel(
    connectionsManager: NearbyConnectionsManager
) : ServerViewModelTemplate(connectionsManager) {

    override fun clientAction(clientAction: ClientAction) {
        when (clientAction) {
            is ClientAction.EstablishConnection -> registerPlayer(clientAction)
            is ClientAction.Disconnect -> removePlayer(clientAction.endpointID)
            is ClientAction.PayloadAction -> handleAction(clientAction)
        }
    }
    
    fun startGame(packageName: String) {
        serverManager.startServer(
            packageName,
            ServerConfiguration(ServerType.IS_TABLE, 4, "GameServer")
        )
    }
}
```

### Player ViewModel

```kotlin
class MyPlayerViewModel(
    connectionsManager: NearbyConnectionsManager
) : PlayerViewModelTemplate(connectionsManager) {

    override fun serverAction(serverAction: ServerAction) {
        when (serverAction) {
            is ServerAction.UpdateGameState -> updateGame(serverAction.payload)
            is ServerAction.UpdatePlayerState -> updatePlayer(serverAction.payload)
            is ServerAction.PayloadAction -> handleAction(serverAction)
        }
    }
    
    fun connect(packageName: String, nickname: String) {
        clientManager.connect(packageName, PlayerConnectionState(nickname))
    }
}
```

### Koin Setup

```kotlin
// Android
startKoin {
    androidContext(applicationContext)
    modules(easyLocalGameModule)
}
```

## Structure

```
EasyLocalGameKmpLibrary/
├── src/
│   ├── commonMain/kotlin/          # Shared Kotlin code
│   │   ├── actions/                # ClientAction, ServerAction
│   │   ├── client/                 # ClientManager, ClientState
│   │   ├── connection/             # NearbyConnections interface
│   │   ├── di/                     # Koin modules
│   │   ├── payload/                # Serialization utilities
│   │   ├── server/                 # ServerManager, ServerState
│   │   └── viewmodel/              # ViewModel templates
│   ├── androidMain/kotlin/         # Android implementation
│   └── iosMain/
│       ├── kotlin/                 # iOS Kotlin implementation
│       └── swift/                  # Swift bridge code (for distribution)
├── Package.swift                   # Swift Package manifest
└── build.gradle.kts
```

## License

Apache 2.0

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)