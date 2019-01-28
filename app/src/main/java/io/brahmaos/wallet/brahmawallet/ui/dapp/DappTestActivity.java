package io.brahmaos.wallet.brahmawallet.ui.dapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.Brahma;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.util.BLog;

public class DappTestActivity extends BaseActivity {

    private WebView webView;
    private ProgressBar pbarLoading;
    private Brahma mImToken;

    @Override
    protected String tag() {
        return DappTestActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dapp_test);
        showNavBackBtn();
        initView();
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void initView(){
        pbarLoading = findViewById(R.id.loading_pbar);
        pbarLoading.setVisibility(View.GONE);
        webView = findViewById(R.id.dapp_wv);
        webView.setVisibility(View.VISIBLE);

        webView.setWebViewClient(new DappWebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        mImToken = new Brahma(DappTestActivity.this, webView);
        webView.addJavascriptInterface(mImToken, "brahma");
        webView.loadUrl("http://192.168.1.28/imtoken/backup.html");
    }

    public class DappWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            BLog.d(tag(), url);
            pbarLoading.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Brahma.CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    Log.i(tag(), "select picture: " + uri.toString());
                    mImToken.onPhotoSelected(uri);
                }
                break;
            case Brahma.SHARE:
                Log.i(tag(),"share result: " + resultCode);
                break;
            case ReqCode.SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String qrCode = data.getStringExtra(Intents.Scan.RESULT);
                        if (qrCode != null && qrCode.length() > 0) {
                            mImToken.onQRcodeDetected(true, qrCode);
                            Log.i(tag(), qrCode);
                        } else {
                            mImToken.onQRcodeDetected(false, null);
                            Log.i(tag(), "scan failed");
                        }
                    }else {
                        mImToken.onQRcodeDetected(false, null);
                    }
                }
                break;
            default:
                break;
        }
    }
}
