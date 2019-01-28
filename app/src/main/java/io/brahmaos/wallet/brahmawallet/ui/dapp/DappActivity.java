package io.brahmaos.wallet.brahmawallet.ui.dapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.Brahma;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.HelpActivity;

public class DappActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvDapp;
    private Brahma mImToken;

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
        pbarLoading.setVisibility(View.GONE);

        wvDapp = findViewById(R.id.dapp_wv);
        wvDapp.setVisibility(View.VISIBLE);

        WebSettings webSettings = wvDapp.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String url = getIntent().getStringExtra(IntentParam.PARAM_DAPP_URL);
        mImToken = new Brahma(DappActivity.this, wvDapp);
        wvDapp.addJavascriptInterface(mImToken, "brahma");
        wvDapp.loadUrl(url);
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
