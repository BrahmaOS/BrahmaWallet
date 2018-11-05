package io.brahmaos.wallet.brahmawallet.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class ConfirmMnemonicActivity extends BaseActivity {

    String mnemonics;
    private LinearLayout backupSuccessLayout;

    @Override
    protected String tag() {
        return ConfirmMnemonicActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_mnemonic);
        showNavBackBtn();

        ArrayList<String> mnemonicCode = getIntent().getStringArrayListExtra(IntentParam.PARAM_MNEMONIC_CODE);
        if (mnemonicCode == null || mnemonicCode.size() == 0 || mnemonicCode.size() % 3 > 0) {
            finish();
            return;
        }
        StringBuilder mnemonicStringBuilder = new StringBuilder();
        for (String mnemonic : mnemonicCode) {
            mnemonicStringBuilder.append(mnemonic).append(" ");
        }
        mnemonics = mnemonicStringBuilder.toString().trim();

        backupSuccessLayout = findViewById(R.id.layout_backup_success);
        Button finishButton = findViewById(R.id.btn_finish);
        finishButton.setOnClickListener(v -> {
            Intent intent = this.getIntent();
            setResult(RESULT_OK, intent);
            finish();
        });

        EditText etMnemonic = findViewById(R.id.et_account_mnemonic);
        etMnemonic.addTextChangedListener(watcher);
    }

    TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().equals(mnemonics)) {
                backupSuccessLayout.setVisibility(View.VISIBLE);
            } else {
                backupSuccessLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
