package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChangeAccountNameActivity extends BaseActivity {

    @Override
    protected String tag() {
        return ChangeAccountNameActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.et_account_name)
    EditText etAccountName;

    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts;
    private int accountId;
    private String accountName;
    private MenuItem menuSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_account_name);
        ButterKnife.bind(this);
        showNavBackBtn();
        accountName = getIntent().getStringExtra(IntentParam.PARAM_ACCOUNT_NAME);
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initView();
        initData();
    }

    private void initView() {
        etAccountName.setText(accountName);
        etAccountName.setSelection(accountName.length());
        etAccountName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BLog.i(tag(), s.toString());
                if (s.toString().equals(accountName)) {
                    menuSave.setEnabled(false);
                } else {
                    menuSave.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        menuSave = menu.findItem(R.id.action_save);
        menuSave.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_save) {
            String name = etAccountName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                etAccountName.setError(getString(R.string.error_field_required));
                return false;
            }

            if (accounts != null && accounts.size() > 0) {
                boolean cancel = false;
                for (AccountEntity accountEntity : accounts) {
                    if (accountEntity.getName().equals(name)) {
                        cancel = true;
                        break;
                    }
                }
                if (cancel) {
                    etAccountName.setError(getString(R.string.error_incorrect_name));
                    return false;
                }
            }

            CustomProgressDialog progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            mViewModel.changeAccountName(accountId, name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                progressDialog.cancel();
                                showLongToast(R.string.success_change_account_name);
                                finish();
                            },
                            throwable -> {
                                BLog.e(tag(), "Unable to create account", throwable);
                                progressDialog.cancel();
                                showLongToast(R.string.error_change_account_name);
                            });;
        }
        return super.onOptionsItemSelected(item);
    }

}
