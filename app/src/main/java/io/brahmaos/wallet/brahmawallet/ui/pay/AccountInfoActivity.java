package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.os.Bundle;
import android.view.View;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class AccountInfoActivity extends BaseActivity {
    @Override
    protected String tag() {
        return AccountInfoActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        showNavBackBtn();
    }

    public void changeAccountAvatar(View view) {
        showShortToast( "change avatar");
    }

    public void changeAccountName(View view) {
        showShortToast( "change name");

    }
}
