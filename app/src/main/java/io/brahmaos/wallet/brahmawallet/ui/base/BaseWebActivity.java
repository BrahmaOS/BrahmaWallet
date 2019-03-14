package io.brahmaos.wallet.brahmawallet.ui.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.util.BLog;


/**
 * This activity is for common and simple web display.
 */
public class BaseWebActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView webView;
    private String url;
    private String title;

    @Override
    protected String tag() {
        return BaseWebActivity.class.getName();
    }

    /**
     * Start web activity.
     *
     * @param context  Who need start web activity.
     * @param title  web activity title
     * @param url  web url that will be displayed.
     */
    public static void startWeb(Context context, String title, String url) {

        if (!url.startsWith("https://")) {
            return;
        }

        Intent intent = new Intent();
        intent.setClass(context, BaseWebActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_web);

        url = getIntent().getStringExtra("url");
        if (null == url || !url.startsWith("https://")) {
            finish();
            return;
        }

        title = getIntent().getStringExtra("title");

        showNavBackBtn();
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.VISIBLE);

        webView = findViewById(R.id.web_view);
        webView.setVisibility(View.GONE);

        webView.setWebViewClient(new BaseWebActivity.BaseWebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(url);

        if (title != null) {
            setToolbarTitle(title);
        }
    }

    public class BaseWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            pbarLoading.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }
}
