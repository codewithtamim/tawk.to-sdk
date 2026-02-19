// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "TawkToIOSSDK",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "TawkToIOSSDK",
            targets: ["TawkToIOSSDK"]
        ),
    ],
    targets: [
        .target(
            name: "TawkToIOSSDK",
            path: "ios-sdk/Sources/TawkToIOSSDK"
        ),
    ]
)
