package io.brahmaos.wallet.brahmawallet.ui.biometric;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Splitter;

import org.bitcoinj.kits.WalletAppKit;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.FingerprintCore;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseWebActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.DataCryptoUtils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TouchIDPayActivity extends BaseActivity implements FingerprintCore.SimpleAuthenticationCallback{
    private int accountId;
    private int accountType;
    private AccountEntity account;
    private AccountViewModel mViewModel;
    private CustomProgressDialog progressDialog;
    private FingerprintCore fingerprintCore;
    private AlertDialog mFingerDialog = null;
    private Button mTouchIdButton;
    private String mAccountAddress;

    @Override
    protected String tag() {
        return TouchIDPayActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_id_pay);
        showNavBackBtn();
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        accountType = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_TYPE, 0);
        if (accountId <= 0 || accountType <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        fingerprintCore = new FingerprintCore(this);
        fingerprintCore.setCallback(this);
        mTouchIdButton = (Button) findViewById(R.id.btn_touch_id);
    }

    public void showTouchIDPaymentNotice(View view) {
        BaseWebActivity.startWeb(this,
                getResources().getString(R.string.touch_id_notice),
                BrahmaConfig.getInstance().getFingerprintTermsUrl());
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        mViewModel.getAccountById(accountId).observe(this, accountEntity -> {
            if (accountEntity != null) {
                account = accountEntity;
                initView(account);
            }
        });
    }
    private void initView(AccountEntity account) {
        if (BrahmaConst.ETH_ACCOUNT_TYPE == accountType) {
            mAccountAddress = account.getAddress();
        } else if (BrahmaConst.BTC_ACCOUNT_TYPE == accountType) {
            WalletAppKit kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
            if (kit != null && kit.wallet() != null) {
                if (kit.wallet().getActiveKeyChain() != null &&
                        kit.wallet().getActiveKeyChain().getIssuedReceiveKeys() != null &&
                        kit.wallet().getActiveKeyChain().getIssuedReceiveKeys().size() > 0) {
                    mAccountAddress = kit.wallet().getActiveKeyChain().getIssuedReceiveKeys().get(0).
                            toAddress(BtcAccountManager.getInstance().getNetworkParams()).toBase58();
                } else {
                    mAccountAddress = kit.wallet().currentChangeAddress().toBase58();
                }
            }
        }
        if (null == mAccountAddress || mAccountAddress.isEmpty()) {
            finish();
            return;
        }

        // If fingerprint in system settings was closed by user, change the button.
        if (1 != fingerprintCore.checkFingerprintAvailable()) {
            closeTouchID(mAccountAddress);
        }
        if (BrahmaConfig.getInstance().getTouchIDPayState(mAccountAddress)) {
            mTouchIdButton.setText(getString(R.string.touch_id_close));
        } else {
            mTouchIdButton.setText(getString(R.string.touch_id_open));
        }
        mTouchIdButton.setOnClickListener(v -> {
            if (!BrahmaConfig.getInstance().getTouchIDPayState(mAccountAddress)) {
                openTouchID(mAccountAddress);
            } else {
                AlertDialog closeDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.prompt_close_touch_id)
                        .setCancelable(true)
                        .setNegativeButton(R.string.prompt_no, (dialog, which) -> {
                            dialog.cancel();
                        })
                        .setPositiveButton(R.string.prompt_sure, (dialog, which) -> {
                            closeTouchID(mAccountAddress);
                            mTouchIdButton.setText(getString(R.string.touch_id_open));
                        })
                        .create();
                closeDialog.show();
            }
        });
    }

    private void openTouchID(String accountAddr) {
        // check whether support fingerprint
        int result = fingerprintCore.checkFingerprintAvailable();
        if (-1 == result) {
            showShortToast(getString(R.string.touch_id_no_hardware));
        } else if (0 == result) {
            showShortToast(getString(R.string.touch_id_no_fingerprint));
        } else if (1 == result) {
            try {
                fingerprintCore.generateKey(accountAddr);
            } catch (Exception e) {
                showShortToast(getString(R.string.touch_id_auth_fail));
                return;
            }
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password_for_touch_id, null);
            AlertDialog passwordDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.cancel();
                        String password = ((EditText) dialogView.findViewById(R.id.et_password)).getText().toString();
                        prepareEncryptPassword(password);
                    })
                    .create();
            passwordDialog.show();
        }
    }

    private void closeTouchID(String accountAddr) {
        // reset shared prference
        BrahmaConfig.getInstance().setTouchIDPayState(accountAddr, false);
        fingerprintCore.clearTouchIDPay(accountAddr);
    }

    private void showFingerprintDialog(String password) {
        mFingerDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .create();
        View fingerView = View.inflate(this, R.layout.fingerdialog, null);
        TextView cancel = fingerView.findViewById(R.id.fingerprint_cancel_tv);
        cancel.setOnClickListener(v -> {
            fingerprintCore.stopListening();
            if (!BrahmaConfig.getInstance().getTouchIDPayState(mAccountAddress)) {
                fingerprintCore.clearTouchIDPay(mAccountAddress);
            }
            mFingerDialog.cancel();
        });
        mFingerDialog.show();
        mFingerDialog.setContentView(fingerView);

        try {
            fingerprintCore.encryptData(mAccountAddress, password);
        } catch (Exception e) {
            mFingerDialog.cancel();
            showShortToast(getString(R.string.touch_id_auth_fail));
        }
    }

    private void prepareEncryptPassword(String password) {
        if (BrahmaConst.ETH_ACCOUNT_TYPE == accountType) {
        progressDialog.show();
        BrahmaWeb3jService.getInstance()
                .getPrivateKeyByPassword(account.getFilename(), password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String privateKey) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        if (privateKey != null && BrahmaWeb3jService.getInstance().isValidPrivateKey(privateKey)) {
                            showFingerprintDialog(password);
                        } else {
                            showPasswordErrorDialog();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        showPasswordErrorDialog();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        } else if(BrahmaConst.BTC_ACCOUNT_TYPE == accountType) {
            String mnemonicsCode = DataCryptoUtils.aes128Decrypt(account.getCryptoMnemonics(), password);
            if (mnemonicsCode != null) {
                List<String> mnemonicsCodes = Splitter.on(" ").splitToList(mnemonicsCode);
                if (mnemonicsCodes.size() == 0 || mnemonicsCodes.size() % 3 > 0) {
                    showPasswordErrorDialog();
                } else {
                    showFingerprintDialog(password);
                }
            } else {
                showPasswordErrorDialog();
            }
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        showShortToast(null == errString ? "" : errString.toString());
        if (7 == errorCode) {
            if (mFingerDialog != null) {
                mFingerDialog.cancel();
            }
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        showShortToast(null == helpString ? "" : helpString.toString());
    }

    @Override
    public void onAuthenticationFail() {
        showShortToast(getString(R.string.fail_fingerprint_verification));
    }

    @Override
    public void onAuthenticationSucceeded(String data) {
        if (mFingerDialog != null) {
            mFingerDialog.cancel();
        }

        BrahmaConfig.getInstance().setTouchIDPayState(mAccountAddress, true);
        mTouchIdButton.setText(getString(R.string.touch_id_close));
        showLongToast(R.string.touch_id_open_succ);
        finish();
    }

    private void showPasswordErrorDialog() {
        AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.error_current_password)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .create();
        errorDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFingerDialog != null) {
            mFingerDialog.cancel();
        }
        fingerprintCore.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fingerprintCore.setCallback(null);
    }
}
