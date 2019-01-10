package io.brahmaos.wallet.brahmawallet.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.ui.base.ImToken;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;

public class DappActivity extends AppCompatActivity {

    public static final String TAG="DappActivity";
    private WebView webView;
    private ImToken mImToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dapp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView(){
        webView = (WebView) findViewById(R.id.dappweb);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        mImToken = new ImToken(DappActivity.this, webView);
        webView.addJavascriptInterface(mImToken, "brahma");
        webView.loadUrl("http://192.168.1.28/imtoken/backup.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ImToken.CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    Log.i(TAG, "select picture: " + uri.toString());
                    mImToken.onPhotoSelected(uri);
                }
                break;
            case ImToken.SHARE:
                Log.i(TAG,"share result: " + resultCode);
                break;
            case ReqCode.SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String qrCode = data.getStringExtra(Intents.Scan.RESULT);
                        if (qrCode != null && qrCode.length() > 0) {
                            mImToken.onQRcodeDetected(true, qrCode);
                            Log.i(TAG, qrCode);
                        } else {
                            mImToken.onQRcodeDetected(false, null);
                            Log.i(TAG, "scan failed");
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
