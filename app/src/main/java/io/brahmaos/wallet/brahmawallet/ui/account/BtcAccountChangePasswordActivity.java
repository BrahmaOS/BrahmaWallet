package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.common.base.Splitter;

import org.bitcoinj.kits.WalletAppKit;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.DataCryptoUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A login screen that offers login via email/password.
 */
public class BtcAccountChangePasswordActivity extends BaseActivity {

    // UI references.
    @BindView(R.id.et_current_password)
    EditText etCurrentPassword;
    @BindView(R.id.et_new_password)
    EditText etNewPassword;
    @BindView(R.id.et_repeat_new_password)
    EditText etRepeatPassword;

    private AccountViewModel mViewModel;
    private int accountId;
    private AccountEntity account;
    private CustomProgressDialog progressDialog;

    @Override
    protected String tag() {
        return BtcAccountChangePasswordActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc_account_change_password);
        showNavBackBtn();
        ButterKnife.bind(this);

        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getAccountById(accountId).observe(this, accountEntity -> {
            account = accountEntity;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_save) {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String repeatPassword = etRepeatPassword.getText().toString();

            if (!CommonUtil.isPasswordValid(newPassword)) {
                etNewPassword.setError(getString(R.string.error_invalid_password));
                etNewPassword.requestFocus();
                return false;
            }

            if (!newPassword.equals(repeatPassword)) {
                etNewPassword.setError(getString(R.string.error_incorrect_password));
                etNewPassword.requestFocus();
                return false;
            }
            String mnemonicsCode = DataCryptoUtils.aes128Decrypt(account.getCryptoMnemonics(), currentPassword);
            if (mnemonicsCode != null) {
                List<String> mnemonicsCodes = Splitter.on(" ").splitToList(mnemonicsCode);
                if (mnemonicsCodes.size() == 0 || mnemonicsCodes.size() % 3 > 0) {
                    etCurrentPassword.setError(getString(R.string.error_current_password));
                    etCurrentPassword.requestFocus();
                } else {
                    changePassword(mnemonicsCode, newPassword);
                }
            } else {
                etCurrentPassword.setError(getString(R.string.error_current_password));
                etCurrentPassword.requestFocus();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void changePassword(String mnemonicsCode, String newPassword) {
        mViewModel.changeBtcAccountPassword(mnemonicsCode, newPassword, accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    showLongToast(R.string.success_change_password);

                    WalletAppKit kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
                    if (kit != null && kit.wallet() != null) {
                        final String mainAddress;
                        if (kit.wallet().getActiveKeyChain() != null &&
                                kit.wallet().getActiveKeyChain().getIssuedReceiveKeys() != null &&
                                kit.wallet().getActiveKeyChain().getIssuedReceiveKeys().size() > 0) {
                            mainAddress = kit.wallet().getActiveKeyChain().getIssuedReceiveKeys().get(0).
                                    toAddress(BtcAccountManager.getInstance().getNetworkParams()).toBase58();
                        } else {
                            mainAddress = kit.wallet().currentChangeAddress().toBase58();
                        }
                        BrahmaConfig.getInstance().setTouchIDPayState(mainAddress, false);
                    }
                    finish();
                },
                throwable -> {
                    BLog.e(tag(), "Unable to change password", throwable);
                    showLongToast(R.string.error_change_password);
                });;
    }
}

