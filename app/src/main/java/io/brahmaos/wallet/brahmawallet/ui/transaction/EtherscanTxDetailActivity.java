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

public class EtherscanTxDetailActivity extends BaseActivity {

    @Override
    protected String tag() {
        return EtherscanTxDetailActivity.class.getName();
    }

    private ProgressBar pbarLoading;
    private WebView wvDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etherscan_tx_detail);
        showNavBackBtn();

        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        String txHash = (String) getIntent().getSerializableExtra(IntentParam.PARAM_TX_HASH);
        if (txHash == null || txHash.length() <= 0) {
            finish();
        }

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvDetail = findViewById(R.id.tx_detail_wv);
        wvDetail.setVisibility(View.GONE);

        wvDetail.setWebViewClient(new TxsWebViewClient());
        WebSettings webSettings = wvDetail.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvDetail.loadUrl(BrahmaConfig.getInstance().getEtherscanTxDetailUrl(txHash));
    }

    public class TxsWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvDetail.setVisibility(View.VISIBLE);
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
            if (wvDetail.canGoBack()) {
                wvDetail.goBack();
            } else {
                finish();
            }
        }
        return false;
    }
}
