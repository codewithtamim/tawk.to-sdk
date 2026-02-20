import SwiftUI
import WebKit

// MARK: - Public API

/// Embeds the [Tawk.to](https://www.tawk.to/) chat widget in a full-size SwiftUI view using a WebView.
///
/// Load the Direct Chat Link from your Tawk.to dashboard. The widget is displayed with a native-like
/// experience: no zoom, optional copy protection, and system background. Loading and error states
/// are handled with optional indicators and a retry action.
///
/// - Parameters:
///   - chatUrl: Direct chat link URL (e.g. `https://tawk.to/chat/PROPERTY_ID/WIDGET_ID`).
///   - options: Options for copy behavior, loading UI, links, and error/retry text.
///   - onLoadFailed: Callback when the page fails to load; receives the error message.
///   - onLoadFinished: Callback when the chat page has finished loading successfully.
@available(iOS 15.0, *)
public struct TawkToChatView: View {
    let chatUrl: String
    var options: TawkToChatOptions
    var onLoadFailed: ((String?) -> Void)?
    var onLoadFinished: (() -> Void)?

    @State private var loadProgress: Double = 0
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var reloadTrigger: Int = 0

    public init(
        chatUrl: String,
        options: TawkToChatOptions = TawkToChatOptions(),
        onLoadFailed: ((String?) -> Void)? = nil,
        onLoadFinished: (() -> Void)? = nil
    ) {
        self.chatUrl = chatUrl
        self.options = options
        self.onLoadFailed = onLoadFailed
        self.onLoadFinished = onLoadFinished
    }

    public var body: some View {
        ZStack {
            (options.backgroundColor ?? Color(uiColor: .systemBackground))
                .ignoresSafeArea()

            TawkWebViewRepresentable(
                urlString: chatUrl,
                reloadTrigger: reloadTrigger,
                disableCopy: options.disableCopy,
                openLinksExternally: options.openLinksExternally,
                progress: $loadProgress,
                isLoading: $isLoading,
                errorMessage: $errorMessage,
                onLoadFinished: { onLoadFinished?() },
                onLoadFailed: { msg in onLoadFailed?(msg) }
            )
            .opacity(errorMessage != nil ? 0 : 1)

            if options.showLoadingIndicator, isLoading, errorMessage == nil {
                VStack(spacing: 0) {
                    ProgressView(value: loadProgress)
                        .progressViewStyle(.linear)
                        .frame(height: 2)
                        .tint(.accentColor)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .transition(.opacity)
            }

            if options.showLoadingIndicator, isLoading, errorMessage == nil, loadProgress < 1 {
                ProgressView()
                    .scaleEffect(1.2)
                    .transition(.opacity.combined(with: .scale))
            }

            if let message = errorMessage {
                errorView(message: message)
            }
        }
        .animation(.easeInOut(duration: 0.2), value: isLoading)
        .animation(.easeInOut(duration: 0.2), value: errorMessage != nil)
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: 12) {
            Text(options.errorTitle ?? "Couldn't load chat")
                .font(.headline)
                .multilineTextAlignment(.center)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            Button(options.retryButtonText) {
                errorMessage = nil
                isLoading = true
                loadProgress = 0
                reloadTrigger += 1
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background((options.backgroundColor ?? Color(uiColor: .systemBackground)).opacity(0.95))
    }
}

// MARK: - WebView

private struct TawkWebViewRepresentable: UIViewRepresentable {
    let urlString: String
    let reloadTrigger: Int
    let disableCopy: Bool
    let openLinksExternally: Bool
    @Binding var progress: Double
    @Binding var isLoading: Bool
    @Binding var errorMessage: String?
    let onLoadFinished: () -> Void
    let onLoadFailed: (String?) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(
            chatHost: URL(string: urlString)?.host,
            disableCopy: disableCopy,
            openLinksExternally: openLinksExternally,
            progress: $progress,
            isLoading: $isLoading,
            errorMessage: $errorMessage,
            onLoadFinished: onLoadFinished,
            onLoadFailed: onLoadFailed
        )
    }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.processPool = WKProcessPool()
        config.preferences.javaScriptEnabled = true
        config.defaultWebpagePreferences.allowsContentJavaScript = true

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.bounces = true
        webView.inputAccessoryView = UIView()
        webView.navigationDelegate = context.coordinator
        webView.uiDelegate = context.coordinator
        webView.allowsBackForwardNavigationGestures = false
        webView.scrollView.isScrollEnabled = true

        context.coordinator.observeProgress(webView: webView)

        if let url = URL(string: urlString), !urlString.isEmpty {
            webView.load(URLRequest(url: url))
        }

        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        let coordinator = context.coordinator
        let shouldLoad = (urlString != coordinator.lastLoadedUrl || coordinator.lastReloadTrigger != reloadTrigger)
            && !urlString.isEmpty
        if shouldLoad, let url = URL(string: urlString) {
            coordinator.lastLoadedUrl = urlString
            coordinator.lastReloadTrigger = reloadTrigger
            errorMessage = nil
            isLoading = true
            progress = 0
            webView.load(URLRequest(url: url))
        }
    }

    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        let chatHost: String?
        let disableCopy: Bool
        let openLinksExternally: Bool
        @Binding var progress: Double
        @Binding var isLoading: Bool
        @Binding var errorMessage: String?
        let onLoadFinished: () -> Void
        let onLoadFailed: (String?) -> Void
        var lastLoadedUrl: String = ""
        var lastReloadTrigger: Int = 0
        private var progressObservation: NSKeyValueObservation?

        init(
            chatHost: String?,
            disableCopy: Bool,
            openLinksExternally: Bool,
            progress: Binding<Double>,
            isLoading: Binding<Bool>,
            errorMessage: Binding<String?>,
            onLoadFinished: @escaping () -> Void,
            onLoadFailed: @escaping (String?) -> Void
        ) {
            self.chatHost = chatHost
            self.disableCopy = disableCopy
            self.openLinksExternally = openLinksExternally
            _progress = progress
            _isLoading = isLoading
            _errorMessage = errorMessage
            self.onLoadFinished = onLoadFinished
            self.onLoadFailed = onLoadFailed
        }

        func observeProgress(webView: WKWebView) {
            progressObservation = webView.observe(\.estimatedProgress) { [weak self] view, _ in
                DispatchQueue.main.async {
                    self?.progress = view.estimatedProgress
                }
            }
        }

        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            DispatchQueue.main.async { [weak self] in
                self?.isLoading = true
                self?.errorMessage = nil
                self?.progress = 0
            }
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            if disableCopy {
                let script = """
                (function() {
                    var s = document.createElement('style');
                    s.textContent = '*:not(input):not(textarea):not([contenteditable]) { -webkit-user-select: none; user-select: none; -webkit-touch-callout: none; }';
                    document.head.appendChild(s);
                })();
                """
                webView.evaluateJavaScript(script, completionHandler: nil)
            }
            DispatchQueue.main.async { [weak self] in
                self?.progress = 1
                self?.isLoading = false
                self?.onLoadFinished()
            }
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            DispatchQueue.main.async { [weak self] in
                self?.errorMessage = error.localizedDescription
                self?.isLoading = false
                self?.onLoadFailed(error.localizedDescription)
            }
        }

        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            DispatchQueue.main.async { [weak self] in
                self?.errorMessage = error.localizedDescription
                self?.isLoading = false
                self?.onLoadFailed(error.localizedDescription)
            }
        }

        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            guard openLinksExternally,
                  let url = navigationAction.request.url,
                  let host = url.host,
                  let chatHost = chatHost,
                  host != chatHost else {
                decisionHandler(.allow)
                return
            }
            if navigationAction.navigationType == .linkActivated {
                UIApplication.shared.open(url)
                decisionHandler(.cancel)
                return
            }
            decisionHandler(.allow)
        }
    }
}
