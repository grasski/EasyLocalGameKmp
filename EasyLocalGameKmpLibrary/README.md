# EasyLocalGame KMP Library

Kotlin Multiplatform library for local multiplayer games using Google Nearby Connections API.

## Module Structure

```
src/
├── commonMain/kotlin/              # Shared Kotlin code (all platforms)
├── androidMain/kotlin/             # Android implementation (Google Play Services)
└── iosMain/
    ├── kotlin/                     # iOS Kotlin implementation
    └── swift/                      # Swift bridge code ⭐
```

## iOS Swift Files

The `src/iosMain/swift/` directory contains Swift source files that must be included in your iOS project:

| File | Purpose |
|------|---------|
| `NearbyConnectionsWrapper.swift` | Bridge to NearbyConnections iOS SDK |
| `EasyLocalGameSetup.swift` | Helper for easy initialization |

### Integration Options

**Option 1: Copy files manually**
Copy all `.swift` files from `src/iosMain/swift/` to your Xcode project.

**Option 2: Swift Package Manager**
Add this library's `Package.swift` as a local Swift Package in Xcode.

### Required Dependencies

Add NearbyConnections SDK via CocoaPods:
```ruby
pod 'NearbyConnections'
```

## Build Commands

```bash
# Build all targets
./gradlew :EasyLocalGameKmpLibrary:assemble

# Build Android AAR only
./gradlew :EasyLocalGameKmpLibrary:bundleReleaseAar

# Build iOS framework
./gradlew :EasyLocalGameKmpLibrary:linkReleaseFrameworkIosArm64

# Copy Swift sources to build directory
./gradlew :EasyLocalGameKmpLibrary:copySwiftSources
```

## JitPack Distribution

This library is designed to be published via JitPack. The Android AAR will be distributed automatically, and iOS users will need to:

1. Add the KMP framework dependency
2. Copy the Swift files from `src/iosMain/swift/`
3. Add NearbyConnections iOS SDK
