package com.example.moviecatalogue.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
import com.example.moviecatalogue.domain.MediaType
import org.json.JSONObject

/**
 * Full-screen streaming player using VidKing embed API.
 *
 * Renders the streaming content via an `<iframe>` inside a [WebView].
 *
 * For movies:  `https://www.vidking.net/embed/movie/{tmdbId}`
 * For TV:      `https://www.vidking.net/embed/tv/{tmdbId}/{season}/{episode}`
 *
 * Supports:
 * - Auto-play, themed controls, episode navigation (TV)
 * - Watch progress tracking via postMessage events from the player
 * - Resume from a specific timestamp via `progress` parameter
 *
 * On show: forces landscape + immersive. On dismiss: restores orientation.
 */
@Composable
fun StreamingFullscreenPlayer(
    tmdbId: Int,
    mediaType: MediaType,
    season: Int? = null,
    episode: Int? = null,
    startProgress: Int? = null,
    onClose: () -> Unit,
    onProgressUpdate: ((currentTime: Double, duration: Double, progress: Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findStreamingActivity() }

    var isLoading by remember { mutableStateOf(true) }

    // Enter landscape immersive while visible; restore when leaving.
    DisposableEffect(Unit) {
        activity?.setStreamingFullscreen(true)
        onDispose { activity?.setStreamingFullscreen(false) }
    }

    var backPressedTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            onClose()
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Tekan sekali lagi untuk kembali", Toast.LENGTH_SHORT).show()
        }
    }

    val webView = remember {
        createStreamingWebView(
            context = context,
            tmdbId = tmdbId,
            mediaType = mediaType,
            season = season,
            episode = episode,
            startProgress = startProgress,
            onLoaded = { isLoading = false },
            onProgressUpdate = onProgressUpdate
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2f)
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
    }
}

// ─── WebView factory ────────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
private fun createStreamingWebView(
    context: Context,
    tmdbId: Int,
    mediaType: MediaType,
    season: Int?,
    episode: Int?,
    startProgress: Int?,
    onLoaded: () -> Unit,
    onProgressUpdate: ((currentTime: Double, duration: Double, progress: Double) -> Unit)?
): WebView = WebView(context).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    setBackgroundColor(android.graphics.Color.BLACK)

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        loadWithOverviewMode = true
        useWideViewPort = true
        allowFileAccess = false
    }

    webChromeClient = WebChromeClient()

    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            onLoaded()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val reqUrl = request?.url ?: return false
            val host = reqUrl.host ?: return false
            
            // Do not intercept iframes or background requests
            if (request.isForMainFrame == false) return false
            
            // Keep vidking.net main frame loads inside the WebView
            if (host.contains("vidking.net", ignoreCase = true)) return false
            
            // Block everything else (like Shopee redirects or popunder ads)
            // by returning true without loading anything.
            return true
        }
    }

    // Build the embed URL
    val embedUrl = buildStreamingUrl(tmdbId, mediaType, season, episode, startProgress)

    loadDataWithBaseURL(
        "https://www.example.com",
        streamingHtml(embedUrl),
        "text/html",
        "utf-8",
        null
    )

    // Listen for postMessage progress events from the player
    if (onProgressUpdate != null) {
        addJavascriptInterface(
            ProgressBridge(onProgressUpdate),
            "AndroidBridge"
        )
    }
}

/**
 * Builds the VidKing embed URL with appropriate parameters.
 */
private fun buildStreamingUrl(
    tmdbId: Int,
    mediaType: MediaType,
    season: Int?,
    episode: Int?,
    startProgress: Int?
): String {
    val basePath = when (mediaType) {
        MediaType.MOVIE -> "https://www.vidking.net/embed/movie/$tmdbId"
        MediaType.TV -> "https://www.vidking.net/embed/tv/$tmdbId/${season ?: 1}/${episode ?: 1}"
    }

    val params = mutableListOf<String>()
    params.add("color=e50914")
    params.add("autoPlay=true")

    if (mediaType == MediaType.TV) {
        params.add("nextEpisode=true")
        params.add("episodeSelector=true")
    }

    startProgress?.let {
        if (it > 0) params.add("progress=$it")
    }

    return "$basePath?${params.joinToString("&")}"
}

/**
 * HTML page that hosts the VidKing streaming iframe and relays
 * postMessage events to the Android bridge.
 */
private fun streamingHtml(embedUrl: String): String = """
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
          src="$embedUrl"
          title="Streaming player"
          frameborder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          referrerpolicy="strict-origin-when-cross-origin"
          >
        </iframe>
        <script>
          window.addEventListener("message", function(event) {
            try {
              var data = typeof event.data === "string" ? JSON.parse(event.data) : event.data;
              if (data && data.type === "PLAYER_EVENT" && data.data) {
                var d = data.data;
                if (typeof AndroidBridge !== 'undefined') {
                  AndroidBridge.onProgress(
                    d.currentTime || 0,
                    d.duration || 0,
                    d.progress || 0,
                    d.event || "",
                    d.id || "",
                    d.mediaType || "",
                    d.season || 0,
                    d.episode || 0
                  );
                }
              }
            } catch(e) {}
          });
        </script>
      </body>
    </html>
""".trimIndent()

/**
 * JavaScript interface that receives progress events from the player's postMessage.
 */
private class ProgressBridge(
    private val onProgressUpdate: (currentTime: Double, duration: Double, progress: Double) -> Unit
) {
    @JavascriptInterface
    fun onProgress(
        currentTime: Double,
        duration: Double,
        progress: Double,
        event: String,
        id: String,
        mediaType: String,
        season: Int,
        episode: Int
    ) {
        // Only relay meaningful events (not every single timeupdate to avoid spam)
        if (event == "timeupdate" || event == "pause" || event == "ended" || event == "seeked") {
            onProgressUpdate(currentTime, duration, progress)
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun Context.findStreamingActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun Activity.setStreamingFullscreen(enable: Boolean) {
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
