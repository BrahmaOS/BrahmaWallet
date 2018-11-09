package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Splitter;

import org.bitcoinj.kits.WalletAppKit;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.DataCryptoUtils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BtcAccountDetailActivity extends BaseActivity {

    @Override
    protected String tag() {
        return BtcAccountDetailActivity.class.getName();
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
    @BindView(R.id.tv_export_mnemonics)
    TextView tvExportMnemonics;

    private int accountId;
    private AccountEntity account;
    private AccountViewModel mViewModel;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc_account_detail);
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

        WalletAppKit kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
        if (kit != null && kit.wallet() != null) {
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(kit.wallet().currentReceiveAddress().toBase58()));
        }

        layoutAccountName.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeAccountNameActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            intent.putExtra(IntentParam.PARAM_ACCOUNT_NAME, account.getName());
            startActivity(intent);
        });

        layoutAccountAddress.setOnClickListener(v -> {
            Intent intent = new Intent(BtcAccountDetailActivity.this, AddressQrcodeBtcActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            startActivity(intent);
        });

        layoutAccountAddressQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(BtcAccountDetailActivity.this, AddressQrcodeBtcActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            startActivity(intent);
        });

        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, BtcAccountChangePasswordActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            startActivity(intent);
        });
        tvExportMnemonics.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
            final EditText etPassword = dialogView.findViewById(R.id.et_password);
            AlertDialog passwordDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.cancel();
                        String password = etPassword.getText().toString();
                        exportMnemonics(password);
                    })
                    .create();
            passwordDialog.setOnShowListener(dialog -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etPassword, InputMethodManager.SHOW_IMPLICIT);
            });
            passwordDialog.show();
        });
    }

    private void exportMnemonics(String password) {
        String mnemonicsCode = DataCryptoUtils.aes128Decrypt(account.getCryptoMnemonics(), password);
        if (mnemonicsCode != null) {
            List<String> mnemonicsCodes = Splitter.on(" ").splitToList(mnemonicsCode);
            if (mnemonicsCodes.size() == 0 || mnemonicsCodes.size() % 3 > 0) {
                showPasswordErrorDialog();
            } else {
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_export_mnemonics, null);
                TextView tvKeystore = dialogView.findViewById(R.id.tv_dialog_mnemonics);
                tvKeystore.setText(mnemonicsCode);
                AlertDialog privateKeyDialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, ((dialog, which) -> {
                            dialog.cancel();
                        }))
                        .setPositiveButton(R.string.copy, (dialog, which) -> {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            cm.setText(mnemonicsCode);
                            showLongToast(R.string.tip_success_copy);
                        })
                        .create();
                privateKeyDialog.show();
            }
        } else {
            showPasswordErrorDialog();
        }
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
