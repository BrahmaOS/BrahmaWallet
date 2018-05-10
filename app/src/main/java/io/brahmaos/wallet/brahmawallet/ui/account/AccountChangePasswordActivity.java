package io.brahmaos.wallet.brahmawallet.ui.account;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.crypto.WalletUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class AccountChangePasswordActivity extends BaseActivity {

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
        return AccountChangePasswordActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_change_password);
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

            progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

            BrahmaWeb3jService.getInstance()
                    .getPrivateKeyByPassword(account.getFilename(), currentPassword)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onNext(String privateKey) {
                            if (privateKey != null && BrahmaWeb3jService.getInstance().isValidPrivateKey(privateKey)) {
                                changePassword(privateKey, newPassword);
                            } else {
                                progressDialog.cancel();
                                etCurrentPassword.setError(getString(R.string.error_current_password));
                                etCurrentPassword.requestFocus();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            progressDialog.cancel();
                            etCurrentPassword.setError(getString(R.string.error_current_password));
                            etCurrentPassword.requestFocus();
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });

        }
        return super.onOptionsItemSelected(item);
    }

    private void changePassword(String privateKey, String newPassword) {
        mViewModel.changeAccountPassword(privateKey, newPassword, accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String address) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        if (address != null && WalletUtils.isValidAddress(address)) {
                            showLongToast(R.string.success_change_password);
                            finish();
                        } else {
                            showLongToast(R.string.error_change_password);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        e.printStackTrace();
                        showLongToast(R.string.error_change_password);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }
}

