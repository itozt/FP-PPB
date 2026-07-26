package com.example.moviecatalogue.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.moviecatalogue.domain.MediaType
import org.json.JSONObject

/**
 * Full-screen streaming player using VidKing embed API.
 *
 * Renders the streaming content via an `<iframe>` inside a [WebView],
 * wrapped in a local HTML page that:
 * - Disables double-tap-to-zoom (fixes 300ms tap delay on Android)
 * - Listens for `postMessage` events from the VidKing player for
 *   real watch-progress tracking (currentTime, duration, progress)
 * - Bridges those events to native Kotlin via a `@JavascriptInterface`
 *
 * For movies:  `https://www.vidking.net/embed/movie/{tmdbId}`
 * For TV:      `https://www.vidking.net/embed/tv/{tmdbId}/{season}/{episode}`
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
    duration: Double,
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

// ─── JavaScript ↔ Native bridge ─────────────────────────────────────────────

/**
 * Bridge that receives watch-progress events from the injected JavaScript
 * `postMessage` listener and forwards them to the Compose callback.
 *
 * Methods annotated with [JavascriptInterface] are callable from JS via
 * `window.AndroidBridge.<method>(...)`.
 */
private class PlayerJsBridge(
    private val onProgressUpdate: ((currentTime: Double, duration: Double, progress: Double) -> Unit)?
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Called from JS whenever the VidKing player posts a progress event
     * (`timeupdate`, `pause`, `seeked`, etc.).
     */
    @JavascriptInterface
    fun onPlayerEvent(eventJson: String) {
        try {
            val json = JSONObject(eventJson)
            val data = if (json.has("data")) json.getJSONObject("data") else json

            val currentTime = data.optDouble("currentTime", -1.0)
            val dur = data.optDouble("duration", -1.0)
            val eventProgress = data.optDouble("progress", -1.0)
            val progress = if (eventProgress >= 0) eventProgress else if (dur > 0) (currentTime / dur) * 100.0 else -1.0

            if (currentTime >= 0 && dur > 0) {
                mainHandler.post {
                    onProgressUpdate?.invoke(currentTime, dur, progress)
                }
            }
        } catch (_: Exception) {
            // Silently ignore malformed messages from ads or unrelated frames.
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
        allowFileAccess = false
        setSupportMultipleWindows(false)

        // Force Desktop User-Agent & Viewport mode
        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        useWideViewPort = true
        loadWithOverviewMode = true
        textZoom = 100
    }

    // Register the JS ↔ Native bridge so the injected script can call
    // `window.AndroidBridge.onPlayerEvent(json)`.
    addJavascriptInterface(PlayerJsBridge(onProgressUpdate), "AndroidBridge")

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

    // Build embed URL & load inside high-resolution desktop scaled wrapper
    val embedUrl = buildStreamingUrl(tmdbId, mediaType, season, episode)
    val html = buildPlayerHtml(embedUrl, startProgress)

    loadDataWithBaseURL(
        "https://www.vidking.net",
        html,
        "text/html",
        "UTF-8",
        null
    )
}

/**
 * Builds the VidKing embed URL with appropriate parameters.
 */
private fun buildStreamingUrl(
    tmdbId: Int,
    mediaType: MediaType,
    season: Int?,
    episode: Int?
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

    return "$basePath?${params.joinToString("&")}"
}

/**
 * Builds the local HTML wrapper that scales down the desktop iframe layout.
 */
private fun buildPlayerHtml(embedUrl: String, startProgress: Int?): String {
    val initialTime = startProgress ?: 0
    return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    html, body {
      width: 100%;
      height: 100%;
      overflow: hidden;
      background: #000;
    }
    .player-wrapper {
      width: 140%;
      height: 140%;
      transform: scale(0.7142857);
      transform-origin: 0 0;
    }
    iframe {
      width: 100%;
      height: 100%;
      border: none;
    }
  </style>
</head>
<body>
  <div class="player-wrapper">
    <iframe
      id="vidking-player"
      src="$embedUrl"
      allow="autoplay; fullscreen; encrypted-media"
      allowfullscreen>
    </iframe>
  </div>

  <script>
    var targetTime = $initialTime;
    var hasSeekedInitial = (targetTime <= 0);

    function tryInitialSeek() {
      if (hasSeekedInitial) return;
      try {
        var iframe = document.getElementById('vidking-player');
        if (!iframe || !iframe.contentWindow) return;

        var video = iframe.contentWindow.document.querySelector('video');
        if (video) {
          if (video.readyState >= 1) {
            video.currentTime = targetTime;
            hasSeekedInitial = true;
          } else {
            video.addEventListener('loadedmetadata', function() {
              if (!hasSeekedInitial) {
                video.currentTime = targetTime;
                hasSeekedInitial = true;
              }
            }, { once: true });
          }
        }
      } catch (e) {}
    }

    var seekInterval = setInterval(function() {
      if (hasSeekedInitial) {
        clearInterval(seekInterval);
        return;
      }
      tryInitialSeek();
    }, 250);

    var playerFrame = document.getElementById('vidking-player');
    if (playerFrame) {
      playerFrame.addEventListener('load', function() {
        setTimeout(tryInitialSeek, 300);
      });
    }

    var lastUpdate = 0;
    window.addEventListener("message", function(event) {
      if (!hasSeekedInitial) tryInitialSeek();
      try {
        var msg = (typeof event.data === "string") ? JSON.parse(event.data) : event.data;
        if (msg && msg.type === "PLAYER_EVENT" && msg.data) {
          var d = msg.data;
          if (typeof d.currentTime === "number" && typeof d.duration === "number") {
             var now = Date.now();
             var isImportant = d.event === 'pause' || d.event === 'ended' || d.event === 'seeked';
             if (isImportant || (now - lastUpdate > 5000)) {
                 lastUpdate = now;
                 if (window.AndroidBridge) {
                     window.AndroidBridge.onPlayerEvent(JSON.stringify(msg.data));
                 }
             }
          }
        }
      } catch (e) {}
    });
  </script>
</body>
</html>
""".trimIndent()
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
