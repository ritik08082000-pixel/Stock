package com.stockscan

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.stockscan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupImmersiveMode()
        setupWebView()
    }

    private fun setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val wv = binding.webView

        wv.settings.apply {
            javaScriptEnabled          = true
            domStorageEnabled          = true
            allowFileAccess            = true
            allowContentAccess         = true
            loadWithOverviewMode       = true
            useWideViewPort            = true
            setSupportZoom(false)
            displayZoomControls        = false
            builtInZoomControls        = false
            cacheMode                  = WebSettings.LOAD_DEFAULT
            mixedContentMode           = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            // Smooth scrolling
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        // JavaScript → Android bridge
        wv.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                // Inject Android-specific tweaks
                view?.evaluateJavascript("""
                    // Tell JS it's running in native Android
                    window.isAndroidApp = true;
                    // Disable text selection
                    document.body.style.webkitUserSelect = 'none';
                    document.body.style.userSelect = 'none';
                    console.log('StockScan Android loaded');
                """.trimIndent(), null)
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress   = newProgress
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                // Forward console.log to Android logcat (debug builds)
                msg?.let {
                    android.util.Log.d("StockScan_JS", "${it.message()} — ${it.sourceId()}:${it.lineNumber()}")
                }
                return true
            }
        }

        // Load the local HTML
        wv.loadUrl("file:///android_asset/index.html")

        // Pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            wv.reload()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.accent_green, theme)
        )
    }

    // Handle back button (WebView history)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // ── JavaScript ↔ Kotlin Bridge ──────────────────────────────────────────
    inner class AndroidBridge(private val ctx: Context) {

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun shareStock(symbol: String, price: String) {
            runOnUiThread {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT,
                        "Check out $symbol at ₹$price on StockScan!")
                }
                ctx.startActivity(android.content.Intent.createChooser(intent, "Share via"))
            }
        }

        @JavascriptInterface
        fun isNetworkAvailable(): Boolean {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        @JavascriptInterface
        fun getDeviceInfo(): String {
            return """{"brand":"${Build.BRAND}","model":"${Build.MODEL}","sdk":${Build.VERSION.SDK_INT}}"""
        }
    }
}
