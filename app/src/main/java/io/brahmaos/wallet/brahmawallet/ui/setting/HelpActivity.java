package io.brahmaos.wallet.brahmawallet.ui.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class HelpActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvHelp;

    @Override
    protected String tag() {
        return HelpActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        showNavBackBtn();
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvHelp = findViewById(R.id.help_wv);
        wvHelp.setVisibility(View.GONE);

        wvHelp.setWebViewClient(new PrivacyWebViewClient());
        WebSettings webSettings = wvHelp.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvHelp.loadUrl(BrahmaConfig.getInstance().getHelpUrl());
    }

    public class PrivacyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvHelp.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (wvHelp.canGoBack()) {
                wvHelp.goBack();
            } else {
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (wvHelp.canGoBack()) {
                wvHelp.goBack();
            } else {
                finish();
            }
        }
        return false;
    }
}
