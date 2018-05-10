package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AccountDetailActivity extends BaseActivity {

    @Override
    protected String tag() {
        return AccountDetailActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.layout_account_address)
    LinearLayout layoutAccountAddress;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.tv_change_account_name)
    TextView tvChangeAccountName;
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
    private AccountViewModel mViewModel;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        ButterKnife.bind(this);
        showNavBackBtn();
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
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
    }

    private void initAccountInfo(AccountEntity account) {
        ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
        tvAccountName.setText(account.getName());
        tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
        tvChangeAccountName.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeAccountNameActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            intent.putExtra(IntentParam.PARAM_ACCOUNT_NAME, account.getName());
            startActivity(intent);
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
                            showKeystoreDialog(keystore);
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
                        });;
    }

}
