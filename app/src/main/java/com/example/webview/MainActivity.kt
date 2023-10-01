package com.example.webview

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val LOCATION_PERMISSION_REQUEST = 1

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        // Enable JavaScript (if needed)
        webView.settings.javaScriptEnabled = true

        // Request location permissions
        requestLocationPermissions()

        // Load a URL
        webView.loadUrl("https://www.google.com/maps")

        // Ensure links open in the WebView
        webView.webViewClient = WebViewClient()

        // Handle geolocation requests
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                callback?.invoke(origin, true, false)
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        // Add a JavaScript interface to the WebView
        webView.addJavascriptInterface(this, "Android")

        // Inject JavaScript to add "Get Directions" button
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val javascript = """
                    var getDirectionsButton = document.createElement("button");
                    getDirectionsButton.textContent = "Get Directions";
                    getDirectionsButton.onclick = function() {
                        Android.getDirections("Origin Address", "Destination Address");
                    };
                    document.body.appendChild(getDirectionsButton);
                """.trimIndent()

                webView.evaluateJavascript(javascript, null)
            }
        }
    }

    private fun requestLocationPermissions() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        val fineLocationPermissionGranted =
            ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED
        val coarseLocationPermissionGranted =
            ContextCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_GRANTED

        val permissionsToRequest = mutableListOf<String>()

        if (!fineLocationPermissionGranted) {
            permissionsToRequest.add(fineLocationPermission)
        }
        if (!coarseLocationPermissionGranted) {
            permissionsToRequest.add(coarseLocationPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            // Check if location permissions were granted
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (granted) {
                // Location permissions granted, reload the WebView
                webView.reload()
            } else {
                // Location permissions denied
                // Handle this situation, e.g., show a message or disable location-related functionality
            }
        }
    }

    override fun onBackPressed() {
        // Check if the WebView can go back
        if (webView.canGoBack()) {
            webView.goBack() // Navigate back in the WebView
        } else {
            super.onBackPressed() // Default behavior (exit the app)
        }
    }

    // This function is called from JavaScript
    @android.webkit.JavascriptInterface
    fun getDirections(origin: String, destination: String) {
        // Handle the "Get Directions" button click here
        // You can open a new activity or show directions using an Intent, etc.
        // For simplicity, let's just display a toast message for now
        runOnUiThread {
            Toast.makeText(
                this,
                "Getting directions from $origin to $destination",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
