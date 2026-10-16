package com.scooltools.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://myscool.vercel.app/";
    private static final int FILE_CHOOSER_REQUEST = 4102;
    private WebView webView;
    private LinearLayout root;
    private AdView adView;
    private ValueCallback<Uri[]> filePathCallback;
    private ConsentInformation consentInformation;
    private boolean mobileAdsInitialized = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(18, 28, 55));
        getWindow().setNavigationBarColor(Color.BLACK);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);
        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1f));
        TextView adLabel = new TextView(this);
        adLabel.setText("إعلان");
        adLabel.setTextColor(Color.rgb(102, 112, 133));
        adLabel.setTextSize(10);
        adLabel.setGravity(Gravity.CENTER);
        root.addView(adLabel, new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));
        configureWebView();
        webView.loadUrl(HOME_URL);
        configureAdsAndConsent();
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return routeUrl(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return routeUrl(Uri.parse(url));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "text/*", "application/pdf"});
                try {
                    startActivityForResult(Intent.createChooser(intent, "اختَر ملفاً"), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "تعذر فتح مدير الملفات", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }

    private boolean routeUrl(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        if ((scheme.equals("https") || scheme.equals("http"))
                && (host.equals("myscool.vercel.app") || host.endsWith(".myscool.vercel.app"))) return false;
        if (scheme.equals("https") || scheme.equals("http") || scheme.equals("mailto") || scheme.equals("tel")) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
            catch (ActivityNotFoundException e) { Toast.makeText(this, "ما قدرناش نفتح الرابط", Toast.LENGTH_SHORT).show(); }
            return true;
        }
        return true;
    }

    private void configureAdsAndConsent() {
        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        ConsentRequestParameters parameters = new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(this, parameters,
            () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, error -> initializeAdsIfAllowed()),
            error -> initializeAdsIfAllowed());
    }

    private void initializeAdsIfAllowed() {
        if (isFinishing() || consentInformation == null || !consentInformation.canRequestAds()
                || mobileAdsInitialized) return;
        mobileAdsInitialized = true;
        MobileAds.initialize(this, status -> runOnUiThread(this::loadBannerIfAllowed));
    }

    private void loadBannerIfAllowed() {
        if (isFinishing() || consentInformation == null || !consentInformation.canRequestAds() || adView != null) return;
        adView = new AdView(this);
        adView.setAdUnitId(BuildConfig.ADMOB_BANNER_ID);
        int widthDp = (int) (getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, widthDp));
        adView.setAdListener(new AdListener() {
            @Override public void onAdFailedToLoad(@NonNull LoadAdError error) { /* App remains usable without ads. */ }
        });
        root.addView(adView, new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] results = resultCode == Activity.RESULT_OK && data != null && data.getData() != null
                    ? new Uri[]{data.getData()} : null;
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }
    @Override protected void onResume() { super.onResume(); if (adView != null) adView.resume(); }
    @Override protected void onPause() { if (adView != null) adView.pause(); super.onPause(); }
    @Override protected void onDestroy() {
        if (filePathCallback != null) filePathCallback.onReceiveValue(null);
        if (adView != null) { adView.destroy(); adView = null; }
        if (webView != null) { webView.stopLoading(); webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
