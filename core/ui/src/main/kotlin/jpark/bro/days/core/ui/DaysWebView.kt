package jpark.bro.days.core.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import jpark.bro.days.core.designsystem.AppThemeTokens

/**
 * 외부 웹 페이지 표시 (정책 문서 · 다른 앱 둘러보기). https 한 사이트만 웹뷰 안에서 열고,
 * Play 스토어 링크는 [onStoreLink]로 넘기며, 그 밖의 링크는 막는다.
 * 파일 · 콘텐츠 접근과 JS 인터페이스는 쓰지 않는다. [reloadKey]가 바뀌면 다시 불러온다.
 */
@SuppressLint("SetJavaScriptEnabled") // 페이지가 목록을 fetch로 그린다. 같은 사이트만 열고 JS 인터페이스는 없다
@Composable
fun DaysWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onPageFailed: () -> Unit,
    onStoreLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    reloadKey: Int = 0,
) {
    val loaded by rememberUpdatedState(onPageLoaded)
    val failed by rememberUpdatedState(onPageFailed)
    val storeLink by rememberUpdatedState(onStoreLink)
    val background = AppThemeTokens.colors.bg.toArgb()
    val pageHost = remember(url) { Uri.parse(url).host.orEmpty() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(background)
                settings.javaScriptEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient =
                    PageClient(
                        pageHost = pageHost,
                        onLoaded = { loaded() },
                        onFailed = { failed() },
                        onStoreLink = { storeLink(it) },
                    )
                tag = reloadKey
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.tag != reloadKey) {
                webView.tag = reloadKey
                webView.loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
    )
}

private class PageClient(
    private val pageHost: String,
    private val onLoaded: () -> Unit,
    private val onFailed: () -> Unit,
    private val onStoreLink: (String) -> Unit,
) : WebViewClient() {
    /** 불러오는 중 오류가 나면 onPageFinished가 와도 성공으로 보지 않는다 */
    private var hasError = false

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean =
        when (webLinkTarget(pageHost, request.url.scheme, request.url.host)) {
            WebLinkTarget.IN_PAGE -> {
                false
            }

            WebLinkTarget.PLAY_STORE -> {
                onStoreLink(request.url.toString())
                true
            }

            WebLinkTarget.BLOCKED -> {
                true
            }
        }

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: Bitmap?,
    ) {
        hasError = false
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        if (!hasError) onLoaded()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        if (request.isForMainFrame) fail()
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        if (request.isForMainFrame) fail()
    }

    private fun fail() {
        hasError = true
        onFailed()
    }
}
