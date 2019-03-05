package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.Account;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.widget.Switch;
import io.brahmaos.wallet.brahmawallet.FingerprintCore;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;

public class AccountDetailActivity extends BaseActivity implements FingerprintCore.SimpleAuthenticationCallback{

    @Override
    protected String tag() {
        return AccountDetailActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.layout_account_name)
    RelativeLayout layoutAccountName;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.layout_account_address)
    RelativeLayout layoutAccountAddress;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.layout_account_address_qrcode)
    RelativeLayout layoutAccountAddressQRCode;
    @BindView(R.id.tv_change_password)
    TextView tvChangePassword;
    @BindView(R.id.tv_export_private_key)
    TextView tvExportPrivateKey;
    @BindView(R.id.tv_export_keystore)
    TextView tvExportKeystore;
    @BindView(R.id.tv_delete_account)
    TextView tvDeleteAccount;

    private int accountId;
    private AccountEntity account;
    private List<AccountEntity> accounts = new ArrayList<>();

    private AccountViewModel mViewModel;
    private CustomProgressDialog progressDialog;
    private RelativeLayout mLayoutTouchID;
    private Switch mSwitchTouchID;
    private FingerprintCore fingerprintCore;
    private AlertDialog mFingerDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        ButterKnife.bind(this);
        showNavBackBtn();
        mLayoutTouchID = (RelativeLayout) findViewById(R.id.layout_account_touch_id);
        mSwitchTouchID = (Switch) findViewById(R.id.switch_touch_id);
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        fingerprintCore = new FingerprintCore(this);
        fingerprintCore.setCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        mViewModel.getAccountById(accountId)
                .observe(this, (AccountEntity accountEntity) -> {
                    if (accountEntity != null) {
                        account = accountEntity;
                        initAccountInfo(accountEntity);
                    }
                });
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null && accountEntities.size() > 0) {
                List<AccountEntity> accounts = new ArrayList<>();
                for (AccountEntity account : accountEntities) {
                    if (account.getType() == BrahmaConst.ETH_ACCOUNT_TYPE) {
                        accounts.add(account);
                    }
                }
                if (accounts.size() == 1) {
                    tvDeleteAccount.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initAccountInfo(AccountEntity account) {
        ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
        tvAccountName.setText(account.getName());
        tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));

        layoutAccountName.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeAccountNameActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            intent.putExtra(IntentParam.PARAM_ACCOUNT_NAME, account.getName());
            startActivity(intent);
        });

        layoutAccountAddress.setOnClickListener(v -> {
            Intent intent = new Intent(AccountDetailActivity.this, AddressQrcodeActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_INFO, account);
            startActivity(intent);
        });

        layoutAccountAddressQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(AccountDetailActivity.this, AddressQrcodeActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_INFO, account);
            startActivity(intent);
        });

        mSwitchTouchID.setOnCheckedChangeListener(null);
        mSwitchTouchID.setChecked(BrahmaConfig.getInstance().getTouchIDPayState(account.getAddress()));
        mLayoutTouchID.setOnClickListener(v -> {
            if (!BrahmaConfig.getInstance().getTouchIDPayState(account.getAddress())) {
                openTouchID(account.getAddress());
            } else {
                closeTouchID(account.getAddress());
            }
        });
        mSwitchTouchID.setOnCheckedChangeListener((v1, isChecked) -> {
            BrahmaConfig.getInstance().setTouchIDPayState(account.getAddress(), isChecked);
            if (!isChecked) {
                fingerprintCore.clearTouchIDPay(account.getAddress());
            }
        });

        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountChangePasswordActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            startActivity(intent);
        });
        tvExportKeystore.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
            AlertDialog passwordDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.cancel();
                        String password = ((EditText) dialogView.findViewById(R.id.et_password)).getText().toString();
                        exportKeystore(password);
                    })
                    .create();
            passwordDialog.show();
        });
        tvExportPrivateKey.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
            AlertDialog passwordDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.cancel();
                        String password = ((EditText) dialogView.findViewById(R.id.et_password)).getText().toString();
                        exportPrivateKey(password);
                    })
                    .create();
            passwordDialog.show();
        });
        tvDeleteAccount.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
            AlertDialog passwordDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.cancel();
                        String password = ((EditText) dialogView.findViewById(R.id.et_password)).getText().toString();
                        prepareDeleteAccount(password);
                    })
                    .create();
            passwordDialog.show();
        });
    }

    private void openTouchID(String accountAddr) {
        //check whether support fingerprint
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
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
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
        mSwitchTouchID.setChecked(false);
    }

    private void showFingerprintDialog(String password) {
        mFingerDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .create();
        View fingerView = View.inflate(this, R.layout.fingerdialog, null);
        TextView cancel = fingerView.findViewById(R.id.fingerprint_cancel_tv);
        cancel.setOnClickListener(v -> {
            fingerprintCore.stopListening();
            if (!BrahmaConfig.getInstance().getTouchIDPayState(account.getAddress())) {
                fingerprintCore.clearTouchIDPay(account.getAddress());
            }
            mFingerDialog.cancel();
        });
        mFingerDialog.show();
        mFingerDialog.setContentView(fingerView);
//        mFingerDialog.getWindow().setLayout(8 * getResources().getDimensionPixelSize(
//                R.dimen.icon_normal_size), LinearLayout.LayoutParams.WRAP_CONTENT);

        try {
            fingerprintCore.encryptData(account.getAddress(), password);
        } catch (Exception e) {
            mFingerDialog.cancel();
            showShortToast(getString(R.string.touch_id_auth_fail));
        }
    }

    private void prepareEncryptPassword(String password) {
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
        mSwitchTouchID.setChecked(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fingerprintCore.setCallback(null);
    }

    private void exportKeystore(String password) {
        progressDialog.show();
        BrahmaWeb3jService.getInstance()
                .getKeystore(account.getFilename(), password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String keystore) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        if (keystore != null && keystore.length() > 0) {
                            //showKeystoreDialog(keystore);
                            Intent intent = new Intent(AccountDetailActivity.this, BackupKeystoreActivity.class);
                            intent.putExtra(IntentParam.PARAM_OFFICIAL_KEYSTORE, keystore);
                            startActivity(intent);
                        } else {
                            showPasswordErrorDialog();;
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
    }

    private void exportPrivateKey(String password) {
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
                            showPrivateKeyDialog(privateKey);
                        } else {
                            showPasswordErrorDialog();;
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

    private void showKeystoreDialog(String keystore) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_keystore, null);
        TextView tvKeystore = dialogView.findViewById(R.id.tv_dialog_keystore);
        tvKeystore.setText(keystore);
        AlertDialog keystoreDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, ((dialog, which) -> {
                    dialog.cancel();
                }))
                .setPositiveButton(R.string.copy, (dialog, which) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(keystore);
                    showLongToast(R.string.tip_success_copy);
                })
                .create();
        keystoreDialog.show();
    }

    private void showPrivateKeyDialog(String privateKey) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_private_key, null);
        TextView tvKeystore = dialogView.findViewById(R.id.tv_dialog_private_key);
        tvKeystore.setText(privateKey);
        AlertDialog privateKeyDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, ((dialog, which) -> {
                    dialog.cancel();
                }))
                .setPositiveButton(R.string.copy, (dialog, which) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(privateKey);
                    showLongToast(R.string.tip_success_copy);
                })
                .create();
        privateKeyDialog.show();
    }

    /**
     * Verify the correctness of the password and
     * allow the user to confirm again whether to delete the account
     * @param password
     */
    private void prepareDeleteAccount(String password) {
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
                            showConfirmDeleteAccountDialog();
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
    }

    private void showConfirmDeleteAccountDialog() {
        AlertDialog deleteDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.delete_account_tip)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, ((dialog, which) -> {
                    dialog.cancel();
                }))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteAccount();
                })
                .create();
        deleteDialog.show();
    }

    private void deleteAccount() {
        if (progressDialog != null) {
            progressDialog.show();
        }
        mViewModel.deleteAccount(accountId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            progressDialog.cancel();
                            showLongToast(R.string.success_delete_account);
                            finish();
                        },
                        throwable -> {
                            BLog.e(tag(), "Unable to delete account", throwable);
                            progressDialog.cancel();
                            showLongToast(R.string.error_delete_account);
                        });
    }

}
