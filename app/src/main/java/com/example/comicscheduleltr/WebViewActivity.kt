package com.example.comicscheduleltr

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Displays the webView for a given link
 */
class WebViewActivity: AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        setContentView(R.layout.web_layout)
        val url: String = intent.getStringExtra("url") as String
        val wv: WebView = findViewById(R.id.FreshView)
        wv.loadUrl(url)
        wv.webViewClient = WebViewClient()
    }
}