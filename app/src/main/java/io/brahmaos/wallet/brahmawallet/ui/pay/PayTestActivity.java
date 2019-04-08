package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.os.Bundle;
import android.view.View;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class PayTestActivity extends BaseActivity {

    @Override
    protected String tag() {
        return PayTestActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_test);
        showNavBackBtn();
    }

    public void onButtonClick(View v) {
        if (null == v) {
            return;
        }
        switch (v.getId()) {
            case R.id.quick_pay_brm:
                break;
            case R.id.quick_pay_btc:
                break;
            case R.id.quick_pay_btc_eth:
                break;
            case R.id.block_pay_brm:
                break;
            case R.id.block_pay_btc:
                break;
            case R.id.block_pay_btc_eth:
                break;
            default:
                break;
        }
    }
}
