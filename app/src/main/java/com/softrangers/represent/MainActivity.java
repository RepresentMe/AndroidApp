package com.softrangers.represent;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://test.represent.me";
    private static final String AUTH = "/auth/login/";
    private static final String FB_AUTH = "/auth/token-only-social-auth/";
    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
//        mWebView.setWebChromeClient(new MyWebViewClient());
        mWebView.setWebViewClient(new WebViewVlient());
        mWebView.addJavascriptInterface(new WebViewInterface(), "AndroidErrorReporter");

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDatabaseEnabled(true);
        webSettings.setDatabasePath(getFilesDir().getPath() + this.getPackageName() + "/databases/");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSaveFormData(true);

        mWebView.loadUrl(BASE_URL);
    }

    private class WebViewVlient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url.equalsIgnoreCase(BASE_URL + AUTH)) {

            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equalsIgnoreCase(BASE_URL + AUTH) || url.equalsIgnoreCase(BASE_URL + FB_AUTH)) {
                WebStorage storage = WebStorage.getInstance();
                storage.getOrigins(new ValueCallback<Map>() {
                    @Override
                    public void onReceiveValue(Map value) {

                    }
                });
            }
        }
    }

    private class MyWebViewClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MainActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            //Double the estimated size of the Database
            quotaUpdater.updateQuota(estimatedSize * 2);
        }
    }

    private class WebViewInterface{

        @JavascriptInterface
        public void onError(String error){
            throw new Error(error);
        }
    }

    private void callJavaScript(String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if(param instanceof String){
                stringBuilder.append("'");
                stringBuilder.append(param);
                stringBuilder.append("'");
            }
            if(i < params.length - 1){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")}catch(error){Android.onError(error.message);}");
        mWebView.loadUrl(stringBuilder.toString());
    }

    public void setValue(int progress) {
        this.mProgressBar.setProgress(progress);
    }
}
