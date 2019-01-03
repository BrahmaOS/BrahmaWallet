package io.brahmaos.wallet.brahmawallet.ui.dapp;

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
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.HelpActivity;

public class DappActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvDapp;

    @Override
    protected String tag() {
        return HelpActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dapp);
        showNavBackBtn();
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvDapp = findViewById(R.id.dapp_wv);
        wvDapp.setVisibility(View.GONE);

        wvDapp.setWebViewClient(new PrivacyWebViewClient());
        WebSettings webSettings = wvDapp.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String url = getIntent().getStringExtra(IntentParam.PARAM_DAPP_URL);
        wvDapp.loadUrl(url);
    }

    public class PrivacyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvDapp.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (wvDapp.canGoBack()) {
                wvDapp.goBack();
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
            if (wvDapp.canGoBack()) {
                wvDapp.goBack();
            } else {
                finish();
            }
        }
        return false;
    }
}
