// Root build for JitPack: only configures plugins; the library is in android-sdk/tawkto-android.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
