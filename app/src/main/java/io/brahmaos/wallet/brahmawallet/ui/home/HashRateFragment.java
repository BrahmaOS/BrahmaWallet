package io.brahmaos.wallet.brahmawallet.ui.home;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;

public class HashRateFragment extends BaseFragment {
    @Override
    protected String tag() {
        return HashRateFragment.class.getName();
    }

    private ProgressBar pbarLoading;
    private WebView wvHashRate;

    /**
     * instance
     *
     * @param layoutResId  layout resource，e.g. R.layout.fragment_home
     * @return  return fragment
     */
    public static HashRateFragment newInstance(int layoutResId, int titleResId) {
        HashRateFragment fragment = new HashRateFragment();
        fragment.setArguments(newArguments(layoutResId, titleResId));
        return fragment;
    }

    @Override
    protected boolean initView() {
        pbarLoading = parentView.findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        wvHashRate = parentView.findViewById(R.id.hash_rate_wv);
        wvHashRate.setVisibility(View.GONE);

        wvHashRate.setWebViewClient(new PrivacyWebViewClient());
        WebSettings webSettings = wvHashRate.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);;

        wvHashRate.loadUrl(BrahmaConfig.getInstance().getHashRateUrl());
        return true;
    }

    public class PrivacyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            wvHashRate.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }
}

