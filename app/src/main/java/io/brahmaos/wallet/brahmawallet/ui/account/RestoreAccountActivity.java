package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.crypto.WalletUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.PrivacyPolicyActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.ServiceTermsActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RestoreAccountActivity extends BaseActivity {
    @Override
    protected String tag() {
        return RestoreAccountActivity.class.getName();
    }

    public static final int REQ_IMPORT_ACCOUNT = 20;
    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts;

    // UI references.
    @BindView(R.id.et_mnemonic)
    EditText etMnemonic;
    @BindView(R.id.et_account_name)
    EditText etAccountName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_repeat_password)
    EditText etRepeatPassword;
    @BindView(R.id.btn_import_mnemonics)
    Button btnImportAccount;
    @BindView(R.id.checkbox_read_protocol)
    CheckBox checkBoxReadProtocol;
    @BindView(R.id.service_tv)
    TextView tvService;
    @BindView(R.id.privacy_policy_tv)
    TextView tvPrivacyPolicy;

    private CustomProgressDialog customProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_account);
        ButterKnife.bind(this);
        showNavBackBtn();
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initView();
        initData();
    }

    private void initView() {
        checkBoxReadProtocol.setOnCheckedChangeListener((buttonView, isChecked) -> btnImportAccount.setEnabled(isChecked));

        tvService.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServiceTermsActivity.class);
            startActivity(intent);
        });

        tvPrivacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        btnImportAccount.setOnClickListener(view -> restoreAccount());
    }

    private void initData() {
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities == null) {
                accounts = null;
            } else {
                accounts = accountEntities;
            }
        });
    }

    private void restoreAccount() {
        btnImportAccount.setEnabled(false);
        // Reset errors.
        etAccountName.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        // Store values at the time of the create account.
        String mnemonics = etMnemonic.getText().toString().trim();
        String name = etAccountName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid mnemonics.
        if (TextUtils.isEmpty(mnemonics)) {
            focusView = etMnemonic;
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_LONG).show();
            cancel = true;
        }

        // Check for a valid account name.
        if (!cancel && TextUtils.isEmpty(name)) {
            etAccountName.setError(getString(R.string.error_field_required));
            focusView = etAccountName;
            cancel = true;
        }

        if (!cancel && accounts != null && accounts.size() > 0) {
            for (AccountEntity accountEntity : accounts) {
                if (accountEntity.getName().equals(name)) {
                    cancel = true;
                    break;
                }
            }
            if (cancel) {
                etAccountName.setError(getString(R.string.error_incorrect_name));
                focusView = etAccountName;
            }
        }

        // Check for a valid password, if the user entered one.
        if (!cancel && !password.equals(repeatPassword)) {
            etPassword.setError(getString(R.string.error_incorrect_password));
            focusView = etPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            btnImportAccount.setEnabled(true);
            return;
        }

        // check the private key valid
        if (CommonUtil.isValidMnemonics(mnemonics)) {
            customProgressDialog = new CustomProgressDialog(this,
                    R.style.CustomProgressDialogStyle,
                    getString(R.string.progress_import_account));
            customProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            customProgressDialog.setCancelable(false);
            customProgressDialog.show();
            mViewModel.restoreAccountWithMnemonics(mnemonics, password, name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onNext(String address) {
                            customProgressDialog.cancel();
                            if (address != null && WalletUtils.isValidAddress(address)) {
                                showShortToast(R.string.success_restore_account);
                                // hide soft input board
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                                finish();
                            } else if (address != null && address.length() == 0) {
                                AlertDialog dialogTip = new AlertDialog.Builder(RestoreAccountActivity.this)
                                        .setMessage(R.string.error_account_exists)
                                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                                        .create();
                                dialogTip.show();
                                etMnemonic.requestFocus();
                                btnImportAccount.setEnabled(true);
                            } else {
                                showShortToast(R.string.error_mnemonics);
                                etMnemonic.requestFocus();
                                btnImportAccount.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            AlertDialog dialogTip = new AlertDialog.Builder(RestoreAccountActivity.this)
                                    .setMessage(R.string.error_mnemonics)
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                                    .create();
                            dialogTip.show();
                            //Toast.makeText(getContext(), R.string.error_mnemonics, Toast.LENGTH_LONG).show();
                            customProgressDialog.cancel();
                            etMnemonic.requestFocus();
                            btnImportAccount.setEnabled(true);
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });

        } else {
            showLongToast(R.string.error_mnemonics_length);
            etMnemonic.requestFocus();
            btnImportAccount.setEnabled(true);
        }
    }
}
