package com.softrangers.represent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
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
    private static final String TOKEN_URL = "https://test.represent.me/api-push/device/gcm/";
    public static final String USER_EXTRAS = "USER EXTRAS";
    public static final String NOTIFICATION_ACTION = "NOTIFICATION ACTION";
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private AlarmManager mAlarmManager;
    private Intent mIntent;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter intentFilter = new IntentFilter("check auth token");
        registerReceiver(mAuthTokenReceiver, intentFilter);
        startCheckingAuthToken();
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

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(NOTIFICATION_ACTION)) {
                User user = intent.getExtras().getParcelable(USER_EXTRAS);
                String url = BASE_URL + user.getUrl();
                mWebView.loadUrl(url);
                return;
            }
        }
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

        }
    }

    private class MyWebViewClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MainActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);

        }
    }

    private BroadcastReceiver mAuthTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("check auth token")) {
                mWebView.loadUrl("javascript:( function () { var resultSrc = localStorage.getItem('auth_token');" +
                        " window.HTMLOUT.onTokenReady(resultSrc); } ) ()");
            }
        }
    };

    private void startCheckingAuthToken() {
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mIntent = new Intent();
        mIntent.setAction("check auth token");
        mPendingIntent = PendingIntent.getBroadcast(this, 0, mIntent, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, mPendingIntent);
    }

    private static String authToken;
    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onTokenReady(String jsResult) {
            if (jsResult != null && !jsResult.equals("")) {
                boolean isTokenSent = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .getBoolean(Preferences.SENT_TOKEN_TO_SERVER, false);

                if (isTokenSent) return;
                authToken = jsResult;
                sendPushTokenToServer(jsResult);
            } else {
                deletePushTokenFromServer();
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, false).apply();
            }
        }
    }

    private void deletePushTokenFromServer() {
        Request request = new Request.Builder()
                .url("https://test.represent.me/api-push/device/gcm/" + PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PUSH_TOKEN_READY, ""))
                .delete()
                .addHeader("Authorization", "Token " + authToken)
                .build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void sendPushTokenToServer(String authToken) {
        try {
            final String pushToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PUSH_TOKEN_READY, "");
            if (pushToken.equals("")) return;
            final String deviceId = "android_" + pushToken;
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
                    startCheckingAuthToken();
                    Looper.prepare();
                    try {
                        String body = response.body().string();
                        String message = new JSONObject(body).optString("detail", "no details");
                        if (message.equals("no details")) {
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
                                    .putBoolean(Preferences.SENT_TOKEN_TO_SERVER, true).apply();
                        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAuthTokenReceiver);
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, false).apply();
        if (mAlarmManager != null && mPendingIntent != null) {
            mAlarmManager.cancel(mPendingIntent);
        }
    }
}
