package io.brahmaos.wallet.brahmawallet.ui.account;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.BlockingDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
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
    @BindView(R.id.layout_create_account_form)
    View formCreateAccount;

    private AccountViewModel mViewModel;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnCreateAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
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

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(name)) {
            etAccountName.setError(getString(R.string.error_field_required));
            focusView = etAccountName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
        }

        try {
            mProgressBar.setVisibility(View.VISIBLE);
            String filename = WalletUtils.generateLightNewWalletFile(password, getFilesDir());

            AccountEntity account = new AccountEntity();
            account.setName(name);
            account.setAddress("address");
            account.setFilename(filename);

            mViewModel.createCount(account)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                mProgressBar.setVisibility(View.GONE);
                                showLongToast("create account success!");
                                BLog.i(tag(), "the file name is: " + filename);
                                finish();
                            },
                            throwable -> {
                                BLog.e(tag(), "Unable to create account", throwable);
                            });
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | IOException | CipherException e) {
            e.printStackTrace();
            BLog.e(tag(), e.getMessage());
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formCreateAccount.setVisibility(show ? View.GONE : View.VISIBLE);
            formCreateAccount.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formCreateAccount.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            formCreateAccount.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

