# Tawk.to Android SDK

A lightweight Android library that embeds the [Tawk.to](https://www.tawk.to/) chat widget in your app using a WebView and Jetpack Compose. Native-like behavior (no copy/zoom), configurable options, and theme-aware UI.

## Installation

### JitPack (recommended)

Add the JitPack repository and the dependency:

**Kotlin DSL (build.gradle.kts):**

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.codewithtamim:tawk-to-sdk:1.0.0")
}
```

**Groovy (build.gradle):**

```groovy
allprojects {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.codewithtamim:tawk-to-sdk:1.0.0'
}
```

Use a [release or tag](https://github.com/codewithtamim/tawk.to-sdk/releases) (e.g. `1.0.0`) instead of a branch when possible.

## Quick start

1. Get your **Direct Chat Link** from the [Tawk.to dashboard](https://help.tawk.to/article/how-to-add-the-tawkto-widget-to-your-mobile-application) (e.g. `https://tawk.to/chat/PROPERTY_ID/WIDGET_ID`).

2. Use the composable in your Compose UI:

```kotlin
@Composable
fun ChatScreen() {
    TawkToChat(
        chatUrl = "https://tawk.to/chat/YOUR_PROPERTY_ID/YOUR_WIDGET_ID",
        modifier = Modifier.fillMaxSize()
    )
}
```

3. Optional: customize behavior and text with `TawkToChatOptions`:

```kotlin
TawkToChat(
    chatUrl = "https://tawk.to/chat/...",
    options = TawkToChatOptions(
        disableCopy = true,
        showLoadingIndicator = true,
        openLinksExternally = true,
        errorTitle = "Couldn't load chat",
        retryButtonText = "Retry",
        backgroundColor = null  // uses MaterialTheme.colorScheme.surface
    ),
    onLoadFinished = { /* chat ready */ },
    onLoadFailed = { message -> /* handle error */ }
)
```

## Features

- **Compose-only API** — Single `TawkToChat` composable; no activities or fragments required.
- **Configurable** — Control copy behavior, loading indicator, external links, error/retry text, and background color.
- **Native-like** — No zoom, optional copy protection, theme-aware surface; fits in-app chat UX.
- **Callbacks** — `onLoadFinished` and `onLoadFailed` for integration and analytics.

## Requirements

- Min SDK 23  
- Jetpack Compose and Material 3  
- Kotlin 1.9+

## License

See [LICENSE](LICENSE) in this repository.
