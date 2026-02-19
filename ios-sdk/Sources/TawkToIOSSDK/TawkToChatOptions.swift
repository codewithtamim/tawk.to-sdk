import SwiftUI

/// Configuration for `TawkToChatView` behavior and appearance.
///
/// Use with `TawkToChatView` to control copy behavior, loading UI,
/// link handling, and error/retry text. All parameters have defaults.
@available(iOS 15.0, *)
public struct TawkToChatOptions {
    /// When `true` (default), prevents copying/selecting chat content; input fields remain editable.
    public var disableCopy: Bool
    /// When `true` (default), shows the progress bar and spinner while the chat page is loading.
    public var showLoadingIndicator: Bool
    /// When `true` (default), links to other domains open in Safari. When `false`, they open in the WebView.
    public var openLinksExternally: Bool
    /// Custom title for the error state; `nil` uses the default "Couldn't load chat".
    public var errorTitle: String?
    /// Label for the retry button (default: "Retry").
    public var retryButtonText: String
    /// Background color behind the chat; `nil` uses the system background.
    public var backgroundColor: Color?

    public init(
        disableCopy: Bool = true,
        showLoadingIndicator: Bool = true,
        openLinksExternally: Bool = true,
        errorTitle: String? = nil,
        retryButtonText: String = "Retry",
        backgroundColor: Color? = nil
    ) {
        self.disableCopy = disableCopy
        self.showLoadingIndicator = showLoadingIndicator
        self.openLinksExternally = openLinksExternally
        self.errorTitle = errorTitle
        self.retryButtonText = retryButtonText
        self.backgroundColor = backgroundColor
    }
}
