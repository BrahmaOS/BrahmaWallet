package io.brahmaos.wallet.brahmawallet.ui;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountGuideActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.BLog;


public class FingerActivity extends BaseActivity {
    
    FingerprintManager manager;
    KeyguardManager mKeyManager;
//    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;
    private boolean isSelfCancelled;
    private AlertDialog mFingerDialog = null;
    private CancellationSignal mCancellationSignal;
    private boolean isFirst = false;

    @Override
    protected String tag() {
        return FingerActivity.class.getName();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_finger);
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            isFirst = getIntent().getBooleanExtra("isFirst", false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        mKeyManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        ImageView ivStartVerification = findViewById(R.id.iv_fingerprint);
        ivStartVerification.setOnClickListener(v -> {
            if (isFinger()) {
                showFingerprintDialog();
            }
        });
        if (isFinger()) {
            showFingerprintDialog();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isFinger() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            BLog.d(tag(), "no permission");
            return false;
        }
        BLog.d(tag(), "have fingerprint permission");
        // Is hardware detected fingerprint
        if (!manager.isHardwareDetected()) {
            BLog.d(tag(), "no hardware detected fingerprint");
            return false;
        }
        BLog.d(tag(), "have hardware detected fingerprint");

        //is open PIN
        if (!mKeyManager.isKeyguardSecure()) {
            BLog.d(tag(), "no PIN");
            return false;
        }
        BLog.d(tag(), "have PIN");

        //Is enrolled fingerprint
        if (!manager.hasEnrolledFingerprints()) {
            BLog.d(tag(), "no fingerprint");
            return false;
        }
        BLog.i(tag(), "have fingerprint");
        return true;
    }

    private void showFingerprintDialog() {
        if (null == mFingerDialog || !mFingerDialog.isShowing()) {
            mFingerDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .create();
            View fingerView = View.inflate(this, R.layout.fingerdialog, null);
            TextView cancel = fingerView.findViewById(R.id.fingerprint_cancel_tv);
            cancel.setOnClickListener(v -> {
                stopListening();
                mFingerDialog.cancel();
            });
            mFingerDialog.show();
            mFingerDialog.setContentView(fingerView);
            startListening(null);
        }
    }

    private void stopListening() {
        isSelfCancelled = true;
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
        if (mFingerDialog != null) {
            mFingerDialog.cancel();
        }
    }

    // callback
    FingerprintManager.AuthenticationCallback mSelfCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            if (!isSelfCancelled) {
                Toast.makeText(FingerActivity.this, errString, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            Toast.makeText(FingerActivity.this, helpString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            if (isFirst) {
                Intent intent = new Intent();
                if (MainService.getInstance().isHaveAccount()) {
                    intent.setClass(FingerActivity.this, MainActivity.class);
                } else {
                    intent.setClass(FingerActivity.this, AccountGuideActivity.class);
                }
                startActivity(intent);
            }
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(FingerActivity.this, R.string.fail_fingerprint_verification, Toast.LENGTH_SHORT).show();
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "no fingerprint permission", Toast.LENGTH_SHORT).show();
            return;
        }
        BLog.i(tag(), "start listening fingerprint");
        if (null == mCancellationSignal || mCancellationSignal.isCanceled()) {
            mCancellationSignal = new CancellationSignal();
        }
        isSelfCancelled = false;
        manager.authenticate(cryptoObject, mCancellationSignal, 0, mSelfCallback, null);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
//            // Challenge completed, proceed with using cipher
//            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "check success", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "check failed", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            finish();
//            timerExit.schedule(timerTask, 500);
//        }
//        return false;
//    }
//    private Timer timerExit = new Timer();
//    private TimerTask timerTask = new TimerTask() {
//        @Override
//        public void run() {
//            System.exit(0);
//        }
//    };
}
