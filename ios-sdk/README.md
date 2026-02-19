# Tawk.to iOS SDK

A lightweight Swift package that embeds the [Tawk.to](https://www.tawk.to/) chat widget in your iOS app using a WebView and SwiftUI. Native-like behavior (no copy/zoom), configurable options, and system styling.

## Installation

### Swift Package Manager (SPM)

1. In Xcode: **File → Add Package Dependencies…**
2. Enter the repository URL:
   ```
   https://github.com/codewithtamim/tawk.to-sdk
   ```
   The repo has a root `Package.swift` so SPM will offer **TawkToIOSSDK**.
3. Add the **TawkToIOSSDK** library to your app target.

Or add to your own `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/codewithtamim/tawk.to-sdk.git", from: "1.0.0")
],
targets: [
    .target(
        name: "YourApp",
        dependencies: ["TawkToIOSSDK"]
    )
]
```

For local development you can add by path (point to the repo root, not `ios-sdk`):

```swift
.package(path: "/path/to/tawk.to-sdk")
```

## Quick start

1. Get your **Direct Chat Link** from the [Tawk.to dashboard](https://help.tawk.to/article/how-to-add-the-tawkto-widget-to-your-mobile-application) (e.g. `https://tawk.to/chat/PROPERTY_ID/WIDGET_ID`).

2. Use the view in SwiftUI:

```swift
import SwiftUI
import TawkToIOSSDK

struct ChatView: View {
    var body: some View {
        TawkToChatView(
            chatUrl: "https://tawk.to/chat/YOUR_PROPERTY_ID/YOUR_WIDGET_ID"
        )
        .ignoresSafeArea(.container)
    }
}
```

3. Optional: customize with `TawkToChatOptions`:

```swift
TawkToChatView(
    chatUrl: "https://tawk.to/chat/...",
    options: TawkToChatOptions(
        disableCopy: true,
        showLoadingIndicator: true,
        openLinksExternally: true,
        errorTitle: nil,
        retryButtonText: "Retry",
        backgroundColor: nil
    ),
    onLoadFinished: { /* chat ready */ },
    onLoadFailed: { message in /* handle error */ }
)
```

## API

- **`TawkToChatView(chatUrl:options:onLoadFailed:onLoadFinished:)`**  
  Full-screen SwiftUI view that loads the Tawk.to Direct Chat Link in a WebView.

- **`TawkToChatOptions`**  
  - `disableCopy` — Prevent copying/selecting chat content (default: `true`).  
  - `showLoadingIndicator` — Show progress and spinner (default: `true`).  
  - `openLinksExternally` — Open other-domain links in Safari (default: `true`).  
  - `errorTitle` — Custom error title; `nil` = "Couldn't load chat".  
  - `retryButtonText` — Retry button label (default: "Retry").  
  - `backgroundColor` — Background color; `nil` = system background.

## Requirements

- iOS 15.0+
- Swift 5.9+
- SwiftUI

## License

See the repository root LICENSE.
