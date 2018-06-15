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

public class PrivacyPolicyActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvPrivacy;

    @Override
    protected String tag() {
        return PrivacyPolicyActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        showNavBackBtn();
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvPrivacy = findViewById(R.id.privacy_policy_wv);
        wvPrivacy.setVisibility(View.GONE);

        wvPrivacy.setWebViewClient(new PrivacyWebViewClient());
        WebSettings webSettings = wvPrivacy.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvPrivacy.loadUrl(BrahmaConfig.getInstance().getPrivacyUrl());
    }

    public class PrivacyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvPrivacy.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }
}
