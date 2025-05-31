// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NossdevBluetoothClassic",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "NossdevBluetoothClassic",
            targets: ["BluetoothClassicPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "BluetoothClassicPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/BluetoothClassicPlugin"),
        .testTarget(
            name: "BluetoothClassicPluginTests",
            dependencies: ["BluetoothClassicPlugin"],
            path: "ios/Tests/BluetoothClassicPluginTests")
    ]
)