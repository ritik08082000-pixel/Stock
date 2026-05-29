# StockScan ProGuard Rules

# Keep WebView JS interface
-keepclassmembers class com.stockscan.MainActivity$AndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep all JS interface methods
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# AndroidX
-keep class androidx.webkit.** { *; }

# General Android
-dontwarn android.webkit.**
