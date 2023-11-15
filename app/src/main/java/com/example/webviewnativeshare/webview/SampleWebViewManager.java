package com.example.webviewnativeshare.webview;

import android.content.res.AssetManager;
import android.util.Log;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.example.webviewnativeshare.webview.share.InnerShareFilesPrepare;
import com.example.webviewnativeshare.webview.share.ShareBroadcastReceiver;
import com.example.webviewnativeshare.webview.share.ShareFilesPrepareCallback;
import com.example.webviewnativeshare.webview.share.ShareManager;

// Inspired by https://github.com/react-native-webview/react-native-webview
public class SampleWebViewManager {

    protected static final String JAVASCRIPT_INTERFACE = "AndroidWebView";

    // Use `webView.loadUrl("about:blank")` to reliably reset the view
    // state and release page resources (including any running JavaScript).
    protected static final String BLANK_URL = "about:blank";

    protected SampleWebChromeClient mWebChromeClient = null;

    protected Integer currentShareId = null;
    protected String currentShareCallbackName = null;

    public SampleWebViewManager() {
        INSTANCE = this;
    }

    private static SampleWebViewManager INSTANCE;

    public static SampleWebViewManager getInstance() {
        return INSTANCE;
    }

    public Integer getCurrentShareId() {
        return currentShareId;
    }

    public void shareComplete(Integer id) {
        SampleWebView webView = (SampleWebView)mWebChromeClient.mWebView;
        webView.evaluateJavascriptWithFallback(currentShareCallbackName + "(" + id + ", undefined)");

        currentShareId = null;
        currentShareCallbackName = null;
    }


    protected SampleWebView createSampleWebViewInstance(Context context) {
        return new SampleWebView(context);
    }

    String getAsset(Context context, String fileName) throws IOException {
        AssetManager am = context.getResources().getAssets();
        InputStream is = am.open(fileName, AssetManager.ACCESS_BUFFER);
        return new Scanner(is).useDelimiter("\\Z").next();
    }

    public WebView createViewInstance(Context context) {
        SampleWebView webView = createSampleWebViewInstance(context);
        setupWebChromeClient(context, webView);
        webView.setWebViewClient(new SampleWebViewClient());

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);

        webView.setMessagingEnabled(true);

        setMediaPlaybackRequiresUserAction(webView, false);

        try {
            String sharePolyfill = getAsset(context, "sharePolyfill.js");
            setInjectedJavaScriptBeforeContentLoaded(webView, sharePolyfill);
        } catch (Exception e) {
            Log.e("webviewTag", e.toString());
        }

        // Fixes broken full-screen modals/galleries due to body height being 0.
        webView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // TODO - implement DownloadListener
//        webView.setDownloadListener(new DownloadListener() {
//            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//                webView.setIgnoreErrFailedForThisURL(url);
//
//                RNCWebViewModule module = getModule(reactContext);
//
//                DownloadManager.Request request;
//                try {
//                    request = new DownloadManager.Request(Uri.parse(url));
//                } catch (IllegalArgumentException e) {
//                    Log.w(TAG, "Unsupported URI, aborting download", e);
//                    return;
//                }
//
//                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
//                String downloadMessage = "Downloading " + fileName;
//
//                //Attempt to add cookie, if it exists
//                URL urlObj = null;
//                try {
//                    urlObj = new URL(url);
//                    String baseUrl = urlObj.getProtocol() + "://" + urlObj.getHost();
//                    String cookie = CookieManager.getInstance().getCookie(baseUrl);
//                    request.addRequestHeader("Cookie", cookie);
//                } catch (MalformedURLException e) {
//                    Log.w(TAG, "Error getting cookie for DownloadManager", e);
//                }
//
//                //Finish setting up request
//                request.addRequestHeader("User-Agent", userAgent);
//                request.setTitle(fileName);
//                request.setDescription(downloadMessage);
//                request.allowScanningByMediaScanner();
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//
//                module.setDownloadRequest(request);
//
//                if (module.grantFileDownloaderPermissions()) {
//                    module.downloadFile();
//                }
//            }
//        });

        return webView;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setMediaPlaybackRequiresUserAction(WebView view, boolean requires) {
        view.getSettings().setMediaPlaybackRequiresUserGesture(requires);
    }

    public void setInjectedJavaScriptBeforeContentLoaded(WebView view, @Nullable String injectedJavaScriptBeforeContentLoaded) {
        ((SampleWebView) view).setInjectedJavaScriptBeforeContentLoaded(injectedJavaScriptBeforeContentLoaded);
    }

    public void setSource(WebView view, @Nullable String source) {
        if (source != null) {
            String previousUrl = view.getUrl();
            if (previousUrl != null && previousUrl.equals(source)) {
                return;
            }
            view.loadUrl(source);
            return;
        }
        view.loadUrl(BLANK_URL);
    }

    public void onDropViewInstance(WebView webView) {
        ((SampleWebView) webView).cleanupCallbacksAndDestroy();
        mWebChromeClient = null;
    }


    protected void setupWebChromeClient(Context context, WebView webView) {
        if (mWebChromeClient != null) {
            mWebChromeClient.onHideCustomView();
        }

        mWebChromeClient = new SampleWebChromeClient(context, webView);

        webView.setWebChromeClient(mWebChromeClient);
    }

    protected static class SampleWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {

            Log.d("webviewTag", "onPageStarted");

            super.onPageStarted(webView, url, favicon);

            SampleWebView reactWebView = (SampleWebView) webView;
            reactWebView.callInjectedJavaScriptBeforeContentLoaded();
        }
    }

    protected static class SampleWebChromeClient extends WebChromeClient {
        protected Context mContext;
        protected WebView mWebView;


        public SampleWebChromeClient(Context context, WebView webView) {
            this.mContext = context;
            this.mWebView = webView;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            final WebView newWebView = new WebView(view.getContext());
            final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();

            return true;
        }
    }


    protected static class SampleWebView extends WebView {
        protected @Nullable String injectedJS;
        protected @Nullable String injectedJSBeforeContentLoaded;

        protected boolean messagingEnabled = false;

        protected @Nullable SampleWebViewClient mSampleWebViewClient;

        /**
         * WebView must be created with an context of the current activity
         * <p>
         * Activity Context is required for creation of dialogs internally by WebView
         * Reactive Native needed for access to ReactNative internal system functionality
         */
        public SampleWebView(Context context) {
            super(context);
        }


        @Override
        public void setWebViewClient(WebViewClient client) {
            super.setWebViewClient(client);
            if (client instanceof SampleWebViewClient) {
                mSampleWebViewClient = (SampleWebViewClient) client;
            }
        }

        WebChromeClient mWebChromeClient;

        @Override
        public void setWebChromeClient(WebChromeClient client) {
            this.mWebChromeClient = client;
            super.setWebChromeClient(client);
        }

        public @Nullable
        SampleWebViewClient getSampleWebViewClient() {
            return mSampleWebViewClient;
        }

        public void setInjectedJavaScript(@Nullable String js) {
            injectedJS = js;
        }

        public void setInjectedJavaScriptBeforeContentLoaded(@Nullable String js) {
            injectedJSBeforeContentLoaded = js;
        }


        public void evaluateJavascriptWithFallback(String script) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                evaluateJavascript(script, null);
                return;
            }

            try {
                loadUrl("javascript:" + URLEncoder.encode(script, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // UTF-8 should always be supported
                throw new RuntimeException(e);
            }
        }

        public void callInjectedJavaScript() {
            if (getSettings().getJavaScriptEnabled() &&
                    injectedJS != null &&
                    !TextUtils.isEmpty(injectedJS)) {
                evaluateJavascriptWithFallback("(function() {\n" + injectedJS + ";\n})();");
            }
        }

        public void callInjectedJavaScriptBeforeContentLoaded() {
            if (getSettings().getJavaScriptEnabled() &&
                    injectedJSBeforeContentLoaded != null &&
                    !TextUtils.isEmpty(injectedJSBeforeContentLoaded)) {
                evaluateJavascriptWithFallback("(function() {\n" + injectedJSBeforeContentLoaded + ";\n})();");
            }
        }

        protected void setMessagingEnabled(boolean enabled) {
            if (messagingEnabled == enabled) {
                return;
            }

            messagingEnabled = enabled;

            if (enabled) {
                addJavascriptInterface(createSampleWebViewBridge(this, SampleWebViewManager.getInstance()), JAVASCRIPT_INTERFACE);
            } else {
                removeJavascriptInterface(JAVASCRIPT_INTERFACE);
            }
        }

        protected SampleWebViewBridge createSampleWebViewBridge(SampleWebView webView, SampleWebViewManager sampleWebViewManager) {
            return new SampleWebViewBridge(webView, sampleWebViewManager);
        }

        protected void onShare(String url, String text, String title, String[] files) {
            if (files.length > 0) {
                new InnerShareFilesPrepare().prepareFiles(this.getContext(), new ShareFilesPrepareCallback() {
                    @Override
                    public void onPrepared(List<String> files) {
                        shareDefault(url, text, title, files);
                    }
                }, files);
            } else {
                shareDefault(url, text, title, Collections.emptyList());
            }

        }

        protected void shareDefault(String url, String text, String title, List<String> files) {
            new ShareManager().shareDefault(
                    ShareBroadcastReceiver.class,
                    this.getContext(),
                    url, text, title, files
            );
        }

        protected void cleanupCallbacksAndDestroy() {
            setWebViewClient(null);
            destroy();
        }

        @Override
        public void destroy() {
            if (mWebChromeClient != null) {
                mWebChromeClient.onHideCustomView();
            }
            super.destroy();
        }

    }

    protected static class SampleWebViewBridge {
        SampleWebView mContext;
        SampleWebViewManager mSampleWebViewManager;

        SampleWebViewBridge(SampleWebView c, SampleWebViewManager sampleWebViewManager) {
            mContext = c;
            mSampleWebViewManager = sampleWebViewManager;
        }

        /**
         * This method is called whenever JavaScript running within the web view calls:
         * - window[JAVASCRIPT_INTERFACE].share
         */
        @JavascriptInterface
        public void share(String callbackName, int id, String url, String text, String title, String[] files) {

            Log.d("webViewTag", callbackName);
            Log.d("webViewTag", Integer.toString(id));

            if (mSampleWebViewManager != null) {
                mSampleWebViewManager.currentShareId = id;
                mSampleWebViewManager.currentShareCallbackName = callbackName;
            }
            mContext.onShare(url, text, title, files);
        }


    }

}

