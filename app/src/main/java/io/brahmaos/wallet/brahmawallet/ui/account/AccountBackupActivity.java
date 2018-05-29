package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.Account;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;

public class AccountBackupActivity extends BaseActivity {

    @Override
    protected String tag() {
        return AccountBackupActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_backup);
        showNavBackBtn();
        String accountName = getIntent().getStringExtra(IntentParam.PARAM_ACCOUNT_NAME);
        if (accountName != null && accountName.length() <= 0) {
            finish();
        }
        Button button = findViewById(R.id.btn_backup_now);

        AccountViewModel mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null && accountEntities.size() > 0) {
                for (AccountEntity account : accountEntities) {
                    if (account.getName().equals(accountName)) {
                        button.setOnClickListener(v -> {
                            Intent intent = new Intent(AccountBackupActivity.this,
                                    AccountDetailActivity.class);
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
                            startActivity(intent);
                            finish();
                        });
                    }
                }
            }
        });
    }

}
