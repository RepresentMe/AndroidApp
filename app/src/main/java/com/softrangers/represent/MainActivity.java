package com.softrangers.represent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://test.represent.me";
    private static final String TOKEN_URL = "https://represent.me/api-push/device/gcm/";
    private static final String AUTH = "/auth/login/";
    private static final String FB_AUTH = "/auth/token-only-social-auth/";
    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebChromeClient(new MyWebViewClient());
        mWebView.setWebViewClient(new WebClient());
        EditorInfo editorInfo = new EditorInfo();
        mWebView.onCreateInputConnection(editorInfo);
        MyJavaScriptInterface javaInterface = new MyJavaScriptInterface();
        mWebView.addJavascriptInterface(javaInterface, "HTMLOUT");

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mWebView.loadUrl("javascript:( function () { var resultSrc = localStorage.getItem('auth_token');" +
                    " window.HTMLOUT.onTokenReady(resultSrc); } ) ()");
        }
    }

    private class MyWebViewClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MainActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);

        }
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onTokenReady(String jsResult) {
            if (jsResult != null && !jsResult.equals("")) {
                sendPushTokenToServer(jsResult);
            }
        }
    }

    private void sendPushTokenToServer(String authToken) {
        try {
            final String pushToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PUSH_TOKEN_READY, "");
            if (pushToken.equals("")) return;
            final String deviceId = "Android_" + pushToken;
            JSONObject object = new JSONObject();
            object.put("name", deviceId);
            object.put("registration_id", pushToken);
            object.put("active", true);
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, object.toString());
            Request request = new Request.Builder()
                    .post(body)
                    .addHeader("Authorization", "Token " + authToken)
                    .url(TOKEN_URL)
                    .build();
            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Looper.prepare();
                    try {
                        String body = response.body().string();
                        String message = new JSONObject(body).optString("detail", "no details");
                        if (message.equals("no details")) {
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
                                    .putBoolean(Preferences.SENT_TOKEN_TO_SERVER, true).apply();
                        }
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        Looper.loop();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setValue(int progress) {
        this.mProgressBar.setProgress(progress);
    }
}
