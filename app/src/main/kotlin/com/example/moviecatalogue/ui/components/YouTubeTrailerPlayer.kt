package com.example.moviecatalogue.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Full-screen, landscape trailer player shown on demand.
 *
 * Renders the trailer by hosting a **static `<iframe>`** (the same markup
 * YouTube's "Share → Embed" gives you, including `allow` and `referrerpolicy`)
 * inside a [WebView], loaded via [WebView.loadDataWithBaseURL] with a
 * *third-party* base URL. The base URL becomes the referrer YouTube sees; it
 * must NOT be youtube.com, or YouTube rejects the embed as "unavailable".
 *
 * Why this and not the JS IFrame Player API / a player library: YouTube's
 * `/embed/` endpoint refuses to load as a *top-level* page, and the JS API does
 * an `origin` postMessage handshake that silently stalls on some WebViews
 * (the classic black "stuck loading" screen). A plain `<iframe>` with the right
 * `referrerpolicy` needs neither, so it plays reliably.
 *
 * On show: forces landscape + immersive. On dismiss (close button or system
 * back): restores the previous orientation.
 */
@Composable
fun TrailerFullscreenPlayer(
    videoKeys: List<String>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val videoId = videoKeys.firstOrNull() ?: return

    var isLoading by remember { mutableStateOf(true) }

    // Enter landscape immersive while visible; restore when leaving.
    DisposableEffect(Unit) {
        activity?.setLandscapeFullscreen(true)
        onDispose { activity?.setLandscapeFullscreen(false) }
    }

    BackHandler { onClose() }

    val webView = remember {
        createTrailerWebView(
            context = context,
            videoId = videoId,
            onLoaded = { isLoading = false }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.loadUrl("about:blank")  // stop playback / audio
            webView.destroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close trailer", tint = Color.White)
        }
    }
}

// ─── WebView factory ────────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
private fun createTrailerWebView(
    context: Context,
    videoId: String,
    onLoaded: () -> Unit
): WebView = WebView(context).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    setBackgroundColor(android.graphics.Color.BLACK)

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false  // allow autoplay
        loadWithOverviewMode = true
        useWideViewPort = true
    }

    webChromeClient = WebChromeClient()  // required for HTML5 <video> / fullscreen

    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            onLoaded()
        }

        // Keep the embed in the WebView, but send any outbound link to YouTube.
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val reqUrl = request?.url ?: return false
            val isEmbed = reqUrl.path?.contains("/embed/") == true
            return if (isEmbed) {
                false  // let the iframe load normally
            } else {
                view?.context?.startActivity(Intent(Intent.ACTION_VIEW, reqUrl))
                true
            }
        }
    }

    // The base URL becomes the page origin, which is what YouTube sees as the
    // embed's referrer. It must be a *third-party* domain (NOT youtube.com):
    // YouTube rejects an embed that claims to originate from youtube.com itself.
    loadDataWithBaseURL(
        "https://www.example.com",
        trailerHtml(videoId),
        "text/html",
        "utf-8",
        null
    )
}

/**
 * A page holding a single static YouTube embed `<iframe>` — the same markup as
 * YouTube's "Share → Embed", which is the only form YouTube reliably allows.
 */
private fun trailerHtml(videoId: String): String = """
    <!DOCTYPE html>
    <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          html, body { margin:0; padding:0; background:#000; height:100%; width:100%; overflow:hidden; }
          iframe { position:fixed; inset:0; width:100%; height:100%; border:0; }
        </style>
      </head>
      <body>
        <iframe
          src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&rel=0"
          title="YouTube video player"
          frameborder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          referrerpolicy="strict-origin-when-cross-origin"
          allowfullscreen>
        </iframe>
      </body>
    </html>
""".trimIndent()

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun Activity.setLandscapeFullscreen(enable: Boolean) {
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    if (enable) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    } else {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
