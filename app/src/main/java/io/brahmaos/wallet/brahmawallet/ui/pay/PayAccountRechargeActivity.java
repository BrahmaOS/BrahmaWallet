package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;

public class PayAccountRechargeActivity extends BaseActivity {
    @Override
    protected String tag() {
        return PayAccountRechargeActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.iv_pay_account_avatar)
    ImageView ivPayAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.layout_choose_token)
    RelativeLayout layoutChooseToken;
    @BindView(R.id.tv_coin_name)
    TextView tvCoinName;
    @BindView(R.id.iv_coin_icon)
    ImageView ivCoinIcon;
    @BindView(R.id.iv_credit_coin_icon)
    ImageView ivCreditCoinIcon;
    @BindView(R.id.et_credit_amount)
    EditText etCreditAmount;
    @BindView(R.id.btn_credit)
    Button btnCredit;

    private int chosenCoinCode = BrahmaConst.PAY_COIN_CODE_BRM;
    private AccountEntity account;
    private AccountViewModel mViewModel;

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_recharge);
        showNavBackBtn();
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initView();
        initData();
        initCoinType(chosenCoinCode);
    }

    private void initView() {
        layoutChooseToken.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_recharge_coins, null);
            builder.setView(dialogView);
            builder.setCancelable(true);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            RelativeLayout layoutCoinBrm = dialogView.findViewById(R.id.layout_coin_brm);
            layoutCoinBrm.setOnClickListener(v1 -> {
                initCoinType(BrahmaConst.PAY_COIN_CODE_BRM);
                alertDialog.cancel();
            });
            RelativeLayout layoutCoinEth = dialogView.findViewById(R.id.layout_coin_eth);
            layoutCoinEth.setOnClickListener(v1 -> {
                initCoinType(BrahmaConst.PAY_COIN_CODE_ETH);
                alertDialog.cancel();
            });
            RelativeLayout layoutCoinBtc = dialogView.findViewById(R.id.layout_coin_btc);
            layoutCoinBtc.setOnClickListener(v1 -> {
                initCoinType(BrahmaConst.PAY_COIN_CODE_BTC);
                alertDialog.cancel();
            });
        });

        btnCredit.setOnClickListener(v -> {
            String sendValueStr = etCreditAmount.getText().toString();
            if (sendValueStr.length() < 1) {
                showLongToast(R.string.tip_invalid_amount);
                return;
            }
            Uri uri = Uri.parse(String.format("brahmaos://wallet/pay?pay_type=1&amount=%s&coin_code=%d&sender=%s",
                    sendValueStr, chosenCoinCode, BrahmaConfig.getInstance().getPayAccount()));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivityForResult(intent, ReqCode.QUICK_PAYMENT_RECHARGE);
        });
    }

    private void initData() {
        String accountAddress = BrahmaConfig.getInstance().getPayAccount();
        tvAccountAddress.setText(accountAddress);
    }

    private void initCoinType(int coinCode) {
        chosenCoinCode = coinCode;
        if (coinCode == BrahmaConst.PAY_COIN_CODE_BRM) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_brm);
            ImageManager.showTokenIcon(this, ivCreditCoinIcon, R.drawable.icon_brm);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_BRM, BrahmaConst.COIN_BRM));
        } else if (coinCode == BrahmaConst.PAY_COIN_CODE_ETH) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_eth);
            ImageManager.showTokenIcon(this, ivCreditCoinIcon, R.drawable.icon_eth);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_ETH, BrahmaConst.COIN_ETH));
        } else if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_btc);
            ImageManager.showTokenIcon(this, ivCreditCoinIcon, R.drawable.icon_btc);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_BTC, BrahmaConst.COIN_BTC));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ReqCode.QUICK_PAYMENT_RECHARGE) {
            if (resultCode == RESULT_OK) {
                String hash = data.getStringExtra(IntentParam.PARAM_PAY_HASH);
                if (hash != null && hash.length() > 0) {
                    showLongToast(R.string.tip_recharge_success);
                    finish();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
