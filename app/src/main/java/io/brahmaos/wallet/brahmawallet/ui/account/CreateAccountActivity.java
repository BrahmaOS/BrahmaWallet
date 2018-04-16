package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.Web3jService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
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
    @BindView(R.id.create_progress)
    View mProgressBar;
    @BindView(R.id.checkbox_read_protocol)
    CheckBox checkBoxReadProtocol;
    @BindView(R.id.layout_create_account_form)
    View formCreateAccount;

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
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void createAccount() {
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
                focusView.requestFocus();
                return;
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
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        try {
            String filename = Web3jService.getInstance().generateLightNewWalletFile(password, getFilesDir());
            String address = Web3jService.getInstance().getWalletAddress(password,
                    getFilesDir() + "/" +  filename);

            AccountEntity account = new AccountEntity();
            account.setName(name);
            account.setAddress(address);
            account.setFilename(filename);
            BLog.i(tag(), getFilesDir() + filename);
            BLog.i(tag(), address);
            mViewModel.createAccount(account)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                mProgressBar.setVisibility(View.GONE);
                                showLongToast(R.string.success_create_account);
                                finish();
                            },
                            throwable -> BLog.e(tag(), "Unable to create account", throwable));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | IOException | CipherException e) {
            e.printStackTrace();
            BLog.e(tag(), e.getMessage());
            mProgressBar.setVisibility(View.GONE);
            showLongToast(R.string.error_create_account);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}

