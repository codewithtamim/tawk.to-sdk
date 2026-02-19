# Tawk.to SDK

Android and iOS SDKs for embedding [Tawk.to](https://www.tawk.to/) chat in your apps.

**Repository:** [github.com/codewithtamim/tawk.to-sdk](https://github.com/codewithtamim/tawk.to-sdk)

| Platform | Install | Docs |
|----------|--------|------|
| **Android** | JitPack | [android-sdk/README.md](android-sdk/README.md) |
| **iOS** | Swift Package Manager | [ios-sdk/README.md](ios-sdk/README.md) |

---

## Android (JitPack)

Add the JitPack repository and dependency:

```kotlin
// build.gradle.kts (project)
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

// build.gradle.kts (app)
dependencies {
    implementation("com.github.codewithtamim:tawk-to-sdk:1.0.0")
}
```

- **Build / open:** Use the `android-sdk` folder as the project root in Android Studio.
- **Details:** [android-sdk/README.md](android-sdk/README.md).

---

## iOS (Swift Package Manager)

In Xcode: **File → Add Package Dependencies…** and enter:

```
https://github.com/codewithtamim/tawk.to-sdk
```

Add the **TawkToIOSSDK** library to your target. Then:

```swift
import TawkToIOSSDK

TawkToChatView(chatUrl: "https://tawk.to/chat/YOUR_PROPERTY_ID/YOUR_WIDGET_ID")
```

- **Details:** [ios-sdk/README.md](ios-sdk/README.md).

---

## Getting your chat URL

Get your **Direct Chat Link** from the [Tawk.to dashboard](https://help.tawk.to/article/how-to-add-the-tawkto-widget-to-your-mobile-application) (e.g. `https://tawk.to/chat/PROPERTY_ID/WIDGET_ID`).

## License

See [LICENSE](LICENSE) in this repository.
