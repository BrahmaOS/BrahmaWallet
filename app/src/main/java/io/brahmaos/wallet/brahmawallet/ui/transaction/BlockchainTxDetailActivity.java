package io.brahmaos.wallet.brahmawallet.ui.transaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class BlockchainTxDetailActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvHelp;

    @Override
    protected String tag() {
        return BlockchainTxDetailActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etherscan_txs);
        showNavBackBtn();

        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        String txHash = getIntent().getStringExtra(IntentParam.PARAM_TX_HASH);
        if (txHash == null || txHash.length() <= 0) {
            finish();
        }

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvHelp = findViewById(R.id.txs_wv);
        wvHelp.setVisibility(View.GONE);

        wvHelp.setWebViewClient(new TxsWebViewClient());
        WebSettings webSettings = wvHelp.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvHelp.loadUrl(BrahmaConfig.getInstance().getBlochchainTxDetailUrl(txHash));
    }

    public class TxsWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvHelp.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);

            String title = view.getTitle();
            if (!TextUtils.isEmpty(title)) {
                Toolbar toolbar = findViewById(R.id.toolbar);
                if (toolbar != null) {
                    toolbar.setTitle(title);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
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
