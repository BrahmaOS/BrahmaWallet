package io.brahmaos.wallet.brahmawallet.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

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
        List<String> mnemonicCode = MainService.getInstance().getMnemonicCode();
        if (mnemonicCode == null || mnemonicCode.size() == 0 || mnemonicCode.size() % 3 > 0) {
            finish();
        }
        Button button = findViewById(R.id.btn_backup_now);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(AccountBackupActivity.this,
                    MnemonicBackupActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
