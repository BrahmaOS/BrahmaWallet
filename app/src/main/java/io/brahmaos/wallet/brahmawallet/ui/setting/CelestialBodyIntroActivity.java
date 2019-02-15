package io.brahmaos.wallet.brahmawallet.ui.setting;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;


public class CelestialBodyIntroActivity extends BaseActivity {

    private ProgressBar pbarLoading;
    private WebView wvContent;

    @Override
    protected String tag() {
        return CelestialBodyIntroActivity.class.getName();
    }

    public static final String PLANET = "uranus";

    // planet -> [language -> wiki]
    private static Map<String, Map<String, String>> planetsWiki;
    static {
        planetsWiki = new HashMap<>();
        Map<String, String> langWiki = new HashMap<>();
        langWiki.put(BrahmaConst.LANGUAGE_ENGLISH, "https://en.wikipedia.org/wiki/Uranus");
        langWiki.put(BrahmaConst.LANGUAGE_CHINESE, "https://baike.baidu.com/item/%E5%A4%A9%E7%8E%8B%E6%98%9F/21805");
        planetsWiki.put("uranus", langWiki);
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

        // Load planet wiki page by language current configured.
        wvContent.loadUrl(planetsWiki.get(PLANET).get(BrahmaConfig.getInstance().getLanguageLocale()));
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
