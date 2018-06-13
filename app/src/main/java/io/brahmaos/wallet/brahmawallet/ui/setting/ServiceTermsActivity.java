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

public class ServiceTermsActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvService;

    @Override
    protected String tag() {
        return ServiceTermsActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_terms);
        showNavBackBtn();
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvService = findViewById(R.id.service_terms_wv);
        wvService.setVisibility(View.GONE);

        wvService.setWebViewClient(new ServiceWebViewClient());
        WebSettings webSettings = wvService.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvService.loadUrl(BrahmaConfig.getInstance().getServiceTermsUrl());
    }

    public class ServiceWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvService.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }
}
