package io.brahmaos.wallet.brahmawallet.ui.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;


public class CelestialBodyIntroActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvContent;

    @Override
    protected String tag() {
        return CelestialBodyIntroActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_celestial_body_intro);
        showNavBackBtn();
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvContent = findViewById(R.id.content_wv);
        wvContent.setVisibility(View.GONE);

        wvContent.setWebViewClient(new CelestialBodyIntroActivity.ContentWebViewClient());
        WebSettings webSettings = wvContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvContent.loadUrl("https://en.wikipedia.org/wiki/Saturn");
    }

    public class ContentWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvContent.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }
}
