package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.PrivacyPolicyActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.ServiceTermsActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A create account screen
 */
public class CreateAccountActivity extends BaseActivity {

    // UI references.
    @BindView(R.id.et_account_name)
    EditText etAccountName;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_repeat_password)
    EditText etRepeatPassword;
    @BindView(R.id.btn_create_account)
    Button btnCreateAccount;
    @BindView(R.id.checkbox_read_protocol)
    CheckBox checkBoxReadProtocol;
    @BindView(R.id.layout_create_account_form)
    View formCreateAccount;
    @BindView(R.id.service_tv)
    TextView tvServiceAgreement;
    @BindView(R.id.privacy_policy_tv)
    TextView tvPrivacyPolicy;

    private CustomProgressDialog customProgressDialog;
    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts;

    @Override
    protected String tag() {
        return CreateAccountActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);
        showNavBackBtn();
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities == null) {
                accounts = null;
            } else {
                accounts = accountEntities;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnCreateAccount.setOnClickListener(view -> createAccount());

        checkBoxReadProtocol.setOnCheckedChangeListener((buttonView, isChecked) -> btnCreateAccount.setEnabled(isChecked));

        tvServiceAgreement.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, ServiceTermsActivity.class);
            startActivity(intent);
        });

        tvPrivacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid name, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void createAccount() {
        btnCreateAccount.setEnabled(false);
        // Reset errors.
        etAccountName.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        // Store values at the time of the create account.
        String name = etAccountName.getText().toString();
        String password = etPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid account name.
        if (TextUtils.isEmpty(name)) {
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
        if (!cancel && (TextUtils.isEmpty(password) || !isPasswordValid(password))) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }

        if (!cancel && !password.equals(repeatPassword)) {
            etPassword.setError(getString(R.string.error_incorrect_password));
            focusView = etPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            btnCreateAccount.setEnabled(true);
            return;
        }
        customProgressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, getString(R.string.progress_create_account));
        customProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        customProgressDialog.setCancelable(false);
        customProgressDialog.show();
        try {
            mViewModel.createAccountWithMnemonic(name, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                customProgressDialog.cancel();
                                showLongToast(R.string.success_create_account);
                                Intent intent = new Intent(CreateAccountActivity.this,
                                        AccountBackupActivity.class);
                                intent.putExtra(IntentParam.PARAM_ACCOUNT_NAME, name);
                                startActivity(intent);
                                finish();
                            },
                            throwable -> {
                                BLog.e(tag(), "Unable to create account", throwable);
                                customProgressDialog.cancel();
                                btnCreateAccount.setEnabled(true);
                                showLongToast(R.string.error_create_account);
                            });
        } catch (Exception e) {
            e.printStackTrace();
            BLog.e(tag(), e.getMessage());
            customProgressDialog.cancel();
            btnCreateAccount.setEnabled(true);
            showLongToast(R.string.error_create_account);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}

