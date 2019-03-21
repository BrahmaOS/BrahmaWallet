package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwangjr.rxbus.RxBus;

import org.web3j.crypto.CipherException;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.FingerprintCore;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.pay.AccountBalance;
import io.brahmaos.wallet.brahmawallet.model.pay.WithdrawConfig;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticEventAgent;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.brahmawallet.ui.transfer.BtcTransferActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.EthTransferActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.view.CustomStatusView;
import io.brahmaos.wallet.brahmawallet.view.PassWordLayout;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.BitcoinPaymentURI;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.ImageUtil;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PayAccountWithdrawActivity extends BaseActivity {
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

    @BindView(R.id.et_receiver_address)
    EditText mEtReceiverAddress;
    @BindView(R.id.iv_scan)
    ImageView mIvScan;
    @BindView(R.id.et_withdraw_amount)
    EditText mEtWithdrawAmount;
    @BindView(R.id.tv_amount_all)
    TextView mTvAmountAll;
    @BindView(R.id.tv_available_amount)
    TextView mTvAvailableAmount;
    @BindView(R.id.tv_withdraw_fee)
    TextView mTvWithdrawFee;
    @BindView(R.id.iv_withdraw_fee_help)
    ImageView mIvFeeHelp;
    @BindView(R.id.tv_withdraw_fee_coin)
    TextView mTvWithdrawFeeCoin;

    @BindView(R.id.btn_withdraw_confirm)
    Button btnWithdrawConfirm;

    private int chosenCoinCode = BrahmaConst.PAY_COIN_CODE_BRM;
    private List<AccountBalance> accountBalances = new ArrayList<>();
    private List<WithdrawConfig> withdrawConfigs = new ArrayList<>();
    private BottomSheetDialog withdrawDialog;
    private TextView tvTransferStatus;
    private CustomStatusView customStatusView;
    private LinearLayout layoutTransferStatus;
    private String withdrawAmountMin;
    private String withdrawFee;

    @Override
    protected String tag() {
        return PayAccountWithdrawActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_withdraw);
        showNavBackBtn();
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

        btnWithdrawConfirm.setOnClickListener(v -> {
            showAccountPassword();
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
        PayService.getInstance().getWithdrawConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<WithdrawConfig>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(List<WithdrawConfig> configs) {
                        if (configs != null) {
                            withdrawConfigs = configs;
                            initWithdrawConfig();
                        }
                    }
                });
    }

    private void initWithdrawConfig() {
        if (withdrawConfigs != null) {
            if (chosenCoinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
                for (WithdrawConfig config : withdrawConfigs) {
                    if (config.getName().equals(BrahmaConst.WITHDRAW_BTC_MIN)) {
                        withdrawAmountMin = config.getValue();
                    }
                    if (config.getName().equals(BrahmaConst.WITHDRAW_BTC_FEE)) {
                        withdrawFee = config.getValue();
                    }
                }
            }
            if (chosenCoinCode == BrahmaConst.PAY_COIN_CODE_ETH) {
                for (WithdrawConfig config : withdrawConfigs) {
                    if (config.getName().equals(BrahmaConst.WITHDRAW_ETH_MIN)) {
                        withdrawAmountMin = config.getValue();
                    }
                    if (config.getName().equals(BrahmaConst.WITHDRAW_ETH_FEE)) {
                        withdrawFee = config.getValue();
                    }
                }
            }
            if (chosenCoinCode == BrahmaConst.PAY_COIN_CODE_BRM) {
                for (WithdrawConfig config : withdrawConfigs) {
                    if (config.getName().equals(BrahmaConst.WITHDRAW_BRM_MIN)) {
                        withdrawAmountMin = config.getValue();
                    }
                    if (config.getName().equals(BrahmaConst.WITHDRAW_BRM_FEE)) {
                        withdrawFee = config.getValue();
                    }
                }
            }
            mTvWithdrawFee.setText(withdrawFee);
        }
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
            mTvWithdrawFeeCoin.setText(BrahmaConst.COIN_SYMBOL_BRM);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_BRM, BrahmaConst.COIN_BRM));
            if (accountBalance != null) {
                mTvAvailableAmount.setText(String.format("%s %s %s",
                        getString(R.string.prompt_available_amount),
                        accountBalance.getBalance(), BrahmaConst.COIN_SYMBOL_BRM));
            }
            initWithdrawConfig();
        } else if (coinCode == BrahmaConst.PAY_COIN_CODE_ETH) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_eth);
            mTvWithdrawFeeCoin.setText(BrahmaConst.COIN_SYMBOL_ETH);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_ETH, BrahmaConst.COIN_ETH));
            if (accountBalance != null) {
                mTvAvailableAmount.setText(String.format("%s %s %s",
                        getString(R.string.prompt_available_amount),
                        accountBalance.getBalance(), BrahmaConst.COIN_SYMBOL_ETH));
            }
            initWithdrawConfig();
        } else if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
            ImageManager.showTokenIcon(this, ivCoinIcon, R.drawable.icon_btc);
            mTvWithdrawFeeCoin.setText(BrahmaConst.COIN_SYMBOL_BTC);
            tvCoinName.setText(String.format("%s (%s)", BrahmaConst.COIN_SYMBOL_BTC, BrahmaConst.COIN_BTC));
            if (accountBalance != null) {
                mTvAvailableAmount.setText(String.format("%s %s %s",
                        getString(R.string.prompt_available_amount),
                        accountBalance.getBalance(), BrahmaConst.COIN_SYMBOL_BTC));
            }
            initWithdrawConfig();
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
                            mEtReceiverAddress.setText(bitcoinUri.getAddress());
                        } else {
                            mEtReceiverAddress.setText(qrCode);
                        }
                    } else {
                        showLongToast(R.string.tip_scan_code_failed);
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void handleCameraScanPermission() {
        scanAddressCode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (withdrawDialog != null && withdrawDialog.isShowing()) {
            withdrawDialog.cancel();
        }
    }

    private void showAccountPassword() {
        String receiverAddress = mEtReceiverAddress.getText().toString().trim();
        String withdrawAmount = mEtWithdrawAmount.getText().toString().trim();
        if (CommonUtil.isNull(withdrawAmount)) {
            showTipDialog(getString(R.string.tip_invalid_amount));
            return;
        }
        String accountBalance = "0";
        for (AccountBalance balance : accountBalances) {
            if (balance.getCoinCode() == chosenCoinCode) {
                accountBalance = balance.getBalance();
                break;
            }
        }

        boolean cancel = false;
        if (new BigDecimal(accountBalance).compareTo(new BigDecimal(withdrawAmount)) <= 0) {
            cancel = true;
        }

        if (cancel) {
            // dialog show tip
            showTipDialog(getString(R.string.tip_insufficient_balance));
            return;
        }

        withdrawDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_withdraw, null);
        withdrawDialog.setContentView(view);
        withdrawDialog.setCancelable(false);
        withdrawDialog.show();

        ImageView ivCloseDialog = view.findViewById(R.id.iv_close_dialog);
        ivCloseDialog.setOnClickListener(v -> withdrawDialog.cancel());

        Button confirmBtn = view.findViewById(R.id.btn_commit_transfer);

        PassWordLayout mLayoutPassword = view.findViewById(R.id.et_quick_account_password);
        mLayoutPassword.removeAllPwd();
        mLayoutPassword.callOnClick();
        mLayoutPassword.setPwdChangeListener(new PassWordLayout.pwdChangeListener() {
            @Override
            public void onChange(String pwd) {
                confirmBtn.setEnabled(false);
            }

            @Override
            public void onNull() {

            }

            @Override
            public void onFinished(String pwd) {
                confirmBtn.setEnabled(true);
            }
        });

        layoutTransferStatus = view.findViewById(R.id.layout_transfer_status);
        customStatusView = view.findViewById(R.id.as_status);
        tvTransferStatus = view.findViewById(R.id.tv_transfer_status);
        confirmBtn.setOnClickListener(v -> {
            // show transfer progress
            layoutTransferStatus.setVisibility(View.VISIBLE);
            customStatusView.loadLoading();
            PayService.getInstance().quickAccountWithdraw(receiverAddress, withdrawAmount, chosenCoinCode,
                    mLayoutPassword.getPassString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ApiRespResult>() {
                        @Override
                        public void onNext(ApiRespResult result) {
                            if (result != null && result.getResult() == 0) {
                                tvTransferStatus.setText(R.string.progress_transfer_success);
                                BLog.i(tag(), "the transfer success");
                                customStatusView.loadSuccess();
                                new Handler().postDelayed(() -> {
                                    withdrawDialog.cancel();
                                    showLongToast(R.string.tip_withdraw_success);
                                    finish();
                                }, 1200);
                            } else  {
                                customStatusView.loadFailure();
                                tvTransferStatus.setText(R.string.progress_transfer_fail);
                                new Handler().postDelayed(() -> {
                                    layoutTransferStatus.setVisibility(View.GONE);
                                    int resId = R.string.tip_error_transfer;
                                    new AlertDialog.Builder(PayAccountWithdrawActivity.this)
                                            .setMessage(resId)
                                            .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                            .create().show();
                                }, 1500);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            customStatusView.loadFailure();
                            tvTransferStatus.setText(R.string.progress_transfer_fail);
                            new Handler().postDelayed(() -> {
                                layoutTransferStatus.setVisibility(View.GONE);
                                int resId = R.string.tip_error_transfer;
                                new AlertDialog.Builder(PayAccountWithdrawActivity.this)
                                        .setMessage(resId)
                                        .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                        .create().show();
                            }, 1500);

                            BLog.i(tag(), "the transfer failed");
                        }

                        @Override
                        public void onCompleted() {
                        }
                    });
        });
    }

    private void showTipDialog(String tips) {
        // dialog show tip
        AlertDialog dialogTip = new AlertDialog.Builder(this)
                .setMessage(tips)
                .setNegativeButton(R.string.ok, (dialog, which) -> dialog.cancel())
                .create();
        dialogTip.show();
    }
}
