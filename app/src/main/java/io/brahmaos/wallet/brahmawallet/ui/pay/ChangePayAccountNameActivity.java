package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class ChangePayAccountNameActivity extends BaseActivity {
    private EditText etAccountName;
    private MenuItem menuSave;
    private String accountName;

    @Override
    protected String tag() {
        return ChangePayAccountNameActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_account_name);
        showNavBackBtn();
        etAccountName = findViewById(R.id.et_account_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        accountName = BrahmaConfig.getInstance().getPayAccountName();
        if(null == accountName || accountName.isEmpty()) {
            accountName = getString(R.string.pay_account_info);
        }
        etAccountName.setText(accountName);
        etAccountName.setSelection(accountName.length());
        etAccountName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(accountName) || accountName.isEmpty()) {
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
            BrahmaConfig.getInstance().setPayAccountName(name);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
