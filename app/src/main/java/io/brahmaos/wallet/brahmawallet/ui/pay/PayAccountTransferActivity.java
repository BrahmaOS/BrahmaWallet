package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.model.pay.AccountBalance;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.BitcoinPaymentURI;
import io.brahmaos.wallet.util.ImageUtil;

public class PayAccountTransferActivity extends BaseActivity {
    // UI references.
    @BindView(R.id.iv_pay_account_avatar)
    ImageView ivPayAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_id)
    TextView tvAccountAddress;
    @BindView(R.id.layout_choose_token)
    RelativeLayout layoutChooseToken;
    @BindView(R.id.tv_coin_name)
    TextView tvCoinName;
    @BindView(R.id.iv_coin_icon)
    ImageView ivCoinIcon;

    @BindView(R.id.et_receiver_account)
    EditText mEtReceiverAccount;
    @BindView(R.id.iv_scan)
    ImageView mIvScan;
    @BindView(R.id.et_transfer_amount)
    EditText mEtTransferAmount;
    @BindView(R.id.tv_amount_all)
    TextView mTvAmountAll;
    @BindView(R.id.tv_available_amount)
    TextView mTvAvailableAmount;

    @BindView(R.id.btn_transfer_confirm)
    Button btnTransferConfirm;

    private int chosenCoinCode = BrahmaConst.PAY_COIN_CODE_BRM;
    private List<AccountBalance> accountBalances = new ArrayList<>();

    @Override
    protected String tag() {
        return PayAccountTransferActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_transfer);showNavBackBtn();
        ButterKnife.bind(this);
        initView();
        initData();
        initCoinType(chosenCoinCode);
    }

    private void initView() {
        layoutChooseToken.setOnClickListener(v -> {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
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
        mIvScan.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraScanPermission();
            } else {
                scanAddressCode();
            }
        });
        btnTransferConfirm.setOnClickListener(v -> {
            String sendValueStr = mEtTransferAmount.getText().toString();
            if (sendValueStr.length() < 1) {
                showLongToast(R.string.tip_invalid_amount);
                return;
            }
            if (null == mEtReceiverAccount.getText() || !mEtReceiverAccount.getText().toString().startsWith("0x")) {
                showLongToast(R.string.tip_invalid_receiver);
                return;
            }
            Uri uri = Uri.parse(String.format("brahmaos://wallet/pay?trade_type=3&amount=%s&coin_code=%d&sender=%s&receiver=%s",
                    sendValueStr, chosenCoinCode, BrahmaConfig.getInstance().getPayAccount(), mEtReceiverAccount.getText().toString()));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivityForResult(intent, ReqCode.QUICK_PAYMENT_TRANSFER);
        });
    }

    private void initData() {
        String accountAddress = BrahmaConfig.getInstance().getPayAccount();
        tvAccountAddress.setText(accountAddress);
        tvAccountName.setText(BrahmaConfig.getInstance().getPayAccountName());
        Bitmap quickAccountAvatar = BrahmaConfig.getInstance().getPayAccountAvatar();
        if (quickAccountAvatar != null) {
            Glide.with(this)
                    .load(ImageUtil.getCircleBitmap(quickAccountAvatar))
                    .into(ivPayAccountAvatar);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_default_account_avatar)
                    .into(ivPayAccountAvatar);
        }
        accountBalances = PayService.getInstance().getAccountBalances();
    }

    private void initCoinType(int coinCode) {
        chosenCoinCode = coinCode;
        AccountBalance accountBalance = null;
        for (AccountBalance balance : accountBalances) {
            if (balance.getCoinCode() == coinCode) {
                accountBalance = balance;
                break;
            }
        }
        if (coinCode == BrahmaConst.PAY_COIN_CODE_BRM) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_brm);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_BRM, BrahmaConst.COIN_BRM));
            if (accountBalance != null) {
                mTvAvailableAmount.setText(String.format("%s %s %s",
                        getString(R.string.prompt_available_amount),
                        accountBalance.getBalance(), BrahmaConst.COIN_SYMBOL_BRM));
            }
        } else if (coinCode == BrahmaConst.PAY_COIN_CODE_ETH) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_eth);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_ETH, BrahmaConst.COIN_ETH));
            if (accountBalance != null) {
                mTvAvailableAmount.setText(String.format("%s %s %s",
                        getString(R.string.prompt_available_amount),
                        accountBalance.getBalance(), BrahmaConst.COIN_SYMBOL_ETH));
            }
        } else if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_btc);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_BTC, BrahmaConst.COIN_BTC));
            if (accountBalance != null) {
                mTvAvailableAmount.setText(String.format("%s %s %s",
                        getString(R.string.prompt_available_amount),
                        accountBalance.getBalance(), BrahmaConst.COIN_SYMBOL_BTC));
            }
        }
    }
    private void scanAddressCode() {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "");
        startActivityForResult(intent, ReqCode.SCAN_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BLog.d(tag(), "requestCode: " + requestCode + "  ;resultCode" + resultCode);
        if (requestCode == ReqCode.SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String qrCode = data.getStringExtra(Intents.Scan.RESULT);
                    if (qrCode != null && qrCode.length() > 0) {
                        if (chosenCoinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
                            BitcoinPaymentURI bitcoinUri = BitcoinPaymentURI.parse(qrCode);
                            if (bitcoinUri == null) {
                                showLongToast(R.string.invalid_btc_address);
                                return;
                            }
                            mEtReceiverAccount.setText(bitcoinUri.getAddress());
                        } else {
                            mEtReceiverAccount.setText(qrCode);
                        }
                    } else {
                        showLongToast(R.string.tip_scan_code_failed);
                    }
                }
            }
        } else if (ReqCode.QUICK_PAYMENT_TRANSFER == requestCode) {
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

    @Override
    public void handleCameraScanPermission() {
        scanAddressCode();
    }
}
