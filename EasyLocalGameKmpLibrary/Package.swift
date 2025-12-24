// swift-tools-version:5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "EasyLocalGameSwift",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "EasyLocalGameSwift",
            targets: ["EasyLocalGameSwift"]
        ),
    ],
    dependencies: [
        // NearbyConnections iOS SDK
        // Note: Users need to add this dependency manually as it's not available via SPM
        // Use CocoaPods: pod 'NearbyConnections'
    ],
    targets: [
        .target(
            name: "EasyLocalGameSwift",
            dependencies: [],
            path: "src/iosMain/swift"
        ),
    ]
)

// IMPORTANT: This Swift Package contains the Swift bridge code for EasyLocalGame KMP Library.
// 
// To use this library in your iOS project:
// 1. Add the KMP framework (EasyLocalGameKmpLibraryKit) to your project
// 2. Add NearbyConnections SDK via CocoaPods: pod 'NearbyConnections'
// 3. Add this Swift package or copy the Swift files from src/iosMain/swift/
// 4. Initialize the bridge in your app startup:
//
//    let manager = // get NearbyConnectionsManager from Koin
//    let wrapper = NearbyConnectionsWrapper()
//    manager.setSwiftBridge(wrapper)
