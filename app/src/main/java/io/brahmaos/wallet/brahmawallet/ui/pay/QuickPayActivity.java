package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Splitter;

import org.bitcoinj.kits.WalletAppKit;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomStatusView;
import io.brahmaos.wallet.util.AnimationUtil;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.DataCryptoUtils;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QuickPayActivity extends BaseActivity {

    public static String PARAM_ACCESS_KEY_ID = "access_key_id";
    public static String PARAM_CREDIT = "credit";
    public static String PARAM_AMOUNT = "amount";
    public static String PARAM_COIN_CODE = "coin_code";

    private ImageView mImageViewClose;
    private LinearLayout mLayoutTransferInfo;
    private ImageView mImageViewCoin;
    private TextView mTvCoinName;
    private EditText mEtTransferAmount;
    private TextView mTvTransferAmount;
    private TextView mTvCommodityInformation;
    private TextView mTvMerchantName;
    private TextView mTvPaymentMethod;
    private LinearLayout mLayoutChooseToken;
    private RelativeLayout mLayoutAccount;
    private ImageView mIvChosenAccountAvatar;
    private TextView mTvAccountInfo;
    private ImageView mIvShowAccountsArrow;

    private LinearLayout mLayoutGasPrice;
    private TextView mTvGasPrice;
    private LinearLayout mLayoutGasLimit;
    private TextView mTvGasLimit;
    private LinearLayout mLayoutBtcFee;
    private TextView mTvBtcFee;
    private Button mBtnConfirmTransfer;

    private LinearLayout mLayoutAccounts;

    private LinearLayout mLayoutEditBtcFee;
    private ImageView ivCloseBtcFee;
    private EditText etBtcFee;
    private Button btnConfirmBtcFee;

    private LinearLayout mLayoutEditGasPrice;
    private EditText etGasPrice;
    private EditText etGasLimit;
    private ImageView ivCloseGasPrice;
    private Button btnConfirmGas;

    private LinearLayout mLayoutChooseAccount;
    private ImageView ivCloseChooseAccount;

    private RelativeLayout mLayoutPayRemark;
    private TextView mTvPaymentRemark;
    private LinearLayout mLayoutEditPayRemark;
    private ImageView mIvClosePayRemark;
    private EditText mEtPayRemark;
    private Button btnConfirmRemark;

    private LinearLayout mLayoutTransferStatus;
    private CustomStatusView customStatusView;
    private TextView tvTransferStatus;

    private int coinCode;
    private boolean isAddCredit = false;
    private String orderId;
    private String receiptAddress;
    private String intentParamSendValue;
    private BigInteger transferValue;
    private String paymentRemark;
    private BigInteger accountBalance;
    private AccountEntity chosenAccount;
    private TokenEntity chosenToken = new TokenEntity();
    private Observable<Boolean> btcAppkitSetup;
    private Observable<String> btcTxBroadcastComplete;
    private List<AccountEntity> accounts = new ArrayList<>();
    private List<AccountAssets> accountAssets = new ArrayList<>();

    @Override
    protected String tag() {
        return QuickPayActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // translucent status bar
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        Uri uri = getIntent().getData();
        String accessKeyId = uri.getQueryParameter(PARAM_ACCESS_KEY_ID);
        if (accessKeyId == null) {
            BLog.d(tag(), "the access key id is null ");
        } else {
            BLog.d(tag(), "the access key id is: " + accessKeyId);
        }
        String addCreditStr = uri.getQueryParameter(PARAM_CREDIT);
        if (addCreditStr != null && Integer.valueOf(addCreditStr) == 1) {
            isAddCredit = true;
        }
        intentParamSendValue = uri.getQueryParameter(PARAM_AMOUNT);
        if (intentParamSendValue == null) {
            showLongToast(R.string.tip_lack_param);
            finish();
        }
        String coinCodeStr = uri.getQueryParameter(PARAM_COIN_CODE);
        if (coinCodeStr == null) {
            showLongToast(R.string.tip_lack_param);
            finish();
        }
        try {
            coinCode = Integer.valueOf(coinCodeStr);
        } catch (NumberFormatException e) {
            showLongToast(R.string.tip_invalid_param);
            finish();
        }
        paymentRemark = uri.getQueryParameter("paymentRemark");
        if (paymentRemark == null) {
            paymentRemark = "";
        }

        setContentView(R.layout.activity_pay_quick);
        initView();
        initData();
    }

    private void initView() {
        mImageViewClose = findViewById(R.id.iv_close_dialog);
        mImageViewClose.setOnClickListener(v -> {
            finish();
        });

        mLayoutTransferInfo = findViewById(R.id.layout_transfer_info);
        mImageViewCoin = findViewById(R.id.iv_transfer_token_icon);
        mTvCoinName = findViewById(R.id.tv_transfer_token_name);
        mTvTransferAmount = findViewById(R.id.tv_transfer_amount);
        mEtTransferAmount = findViewById(R.id.et_transfer_amount);

        mTvMerchantName = findViewById(R.id.tv_quick_pay_merchant_name);
        mTvCommodityInformation = findViewById(R.id.tv_quick_pay_commodity_information);
        mTvPaymentMethod = findViewById(R.id.tv_quick_pay_type);

        mLayoutBtcFee = findViewById(R.id.layout_btc_fee);
        mTvBtcFee = findViewById(R.id.tv_btc_fee);
        mLayoutBtcFee.setOnClickListener(v -> {
            etBtcFee.setText(mTvBtcFee.getText());
            mLayoutEditBtcFee.setVisibility(View.VISIBLE);
            mLayoutEditBtcFee.setAnimation(AnimationUtil.makeInAnimation());
        });

        mLayoutEditBtcFee = findViewById(R.id.layout_edit_btc_fee);
        etBtcFee = findViewById(R.id.et_btc_fee);
        ivCloseBtcFee = findViewById(R.id.iv_close_btc_fee);
        ivCloseBtcFee.setOnClickListener(v -> {
            mLayoutEditBtcFee.setVisibility(View.GONE);
            mLayoutEditBtcFee.setAnimation(AnimationUtil.makeOutAnimation());
        });
        btnConfirmBtcFee = findViewById(R.id.btn_commit_btc_fee);
        btnConfirmBtcFee.setOnClickListener(v -> {
            String feePerByte = etBtcFee.getText().toString().trim();
            if (feePerByte.length() >= 1) {
                mTvBtcFee.setText(feePerByte);
            }
            mLayoutEditBtcFee.setVisibility(View.GONE);
            mLayoutEditBtcFee.setAnimation(AnimationUtil.makeOutAnimation());
        });

        mLayoutChooseToken = findViewById(R.id.layout_choose_token);

        mLayoutAccount = findViewById(R.id.layout_account);
        mIvChosenAccountAvatar = findViewById(R.id.iv_chosen_account_avatar);
        mTvAccountInfo = findViewById(R.id.tv_account_info);
        mIvShowAccountsArrow = findViewById(R.id.iv_show_accounts_arrow);

        mLayoutChooseAccount = findViewById(R.id.layout_choose_account);
        mLayoutAccounts = findViewById(R.id.layout_accounts);
        ivCloseChooseAccount = findViewById(R.id.iv_close_choose_account);
        ivCloseChooseAccount.setOnClickListener(v -> {
            mLayoutChooseAccount.setVisibility(View.GONE);
            mLayoutChooseAccount.setAnimation(AnimationUtil.makeOutAnimation());
        });

        mLayoutGasPrice = findViewById(R.id.layout_eth_gas_price);
        mTvGasPrice = findViewById(R.id.tv_eth_gas_price);
        mTvGasLimit = findViewById(R.id.tv_eth_gas_limit);
        mLayoutGasPrice.setOnClickListener(v -> {
            etGasPrice.setText(mTvGasPrice.getText());
            etGasLimit.setText(mTvGasLimit.getText());
            mLayoutEditGasPrice.setVisibility(View.VISIBLE);
            mLayoutEditGasPrice.setAnimation(AnimationUtil.makeInAnimation());
        });
        mLayoutGasLimit = findViewById(R.id.layout_eth_gas_limit);
        mLayoutGasLimit.setOnClickListener(v -> {
            etGasPrice.setText(mTvGasPrice.getText());
            etGasLimit.setText(mTvGasLimit.getText());
            mLayoutEditGasPrice.setVisibility(View.VISIBLE);
            mLayoutEditGasPrice.setAnimation(AnimationUtil.makeInAnimation());
        });
        mLayoutEditGasPrice = findViewById(R.id.layout_edit_eth_gas_price);
        btnConfirmGas = findViewById(R.id.btn_commit_gas_price);
        etGasLimit = findViewById(R.id.et_gas_limit);
        etGasPrice = findViewById(R.id.et_gas_price);
        ivCloseGasPrice = findViewById(R.id.iv_close_eth_gas_price);
        ivCloseGasPrice.setOnClickListener(v -> {
            mLayoutEditGasPrice.setVisibility(View.GONE);
            mLayoutEditGasPrice.setAnimation(AnimationUtil.makeOutAnimation());
        });
        btnConfirmGas.setOnClickListener(v -> {
            String gasPrice = etGasPrice.getText().toString().trim();
            if (gasPrice.length() >= 1) {
                mTvGasPrice.setText(gasPrice);
            }
            String gasLimit = etGasLimit.getText().toString().trim();
            if (gasLimit.length() >= 1) {
                mTvGasLimit.setText(gasLimit);
            }
            mLayoutEditGasPrice.setVisibility(View.GONE);
            mLayoutEditGasPrice.setAnimation(AnimationUtil.makeOutAnimation());
        });

        mLayoutPayRemark = findViewById(R.id.layout_pay_remark);
        mLayoutPayRemark.setOnClickListener(v -> {
            mEtPayRemark.setText(mTvPaymentRemark.getText());
            mLayoutEditPayRemark.setVisibility(View.VISIBLE);
            mLayoutEditPayRemark.setAnimation(AnimationUtil.makeInAnimation());
        });
        mTvPaymentRemark = findViewById(R.id.tv_remark);
        mLayoutEditPayRemark = findViewById(R.id.layout_edit_pay_remark);
        mIvClosePayRemark = findViewById(R.id.iv_close_pay_remark);
        mIvClosePayRemark.setOnClickListener(v -> {
            mLayoutEditPayRemark.setVisibility(View.GONE);
            mLayoutEditPayRemark.setAnimation(AnimationUtil.makeOutAnimation());
        });
        mEtPayRemark = findViewById(R.id.et_pay_remark);
        btnConfirmRemark = findViewById(R.id.btn_commit_remark);
        btnConfirmRemark.setOnClickListener(v -> {
            String remark = mEtPayRemark.getText().toString().trim();
            if (remark.length() >= 1) {
                mTvPaymentRemark.setText(remark);
            }
            mLayoutEditPayRemark.setVisibility(View.GONE);
            mLayoutEditPayRemark.setAnimation(AnimationUtil.makeOutAnimation());
        });

        mLayoutTransferStatus = findViewById(R.id.layout_transfer_status);
        customStatusView = findViewById(R.id.as_status);
        tvTransferStatus = findViewById(R.id.tv_transfer_status);

        mBtnConfirmTransfer = findViewById(R.id.btn_commit_transfer);
        mBtnConfirmTransfer.setOnClickListener(v -> {
            /*Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();*/
            showPasswordDialog();
        });

        // Initialize values based on parameters
        if (isAddCredit) {
            mTvMerchantName.setText(getString(R.string.label_quick_payment_account_recharge));
            mTvCommodityInformation.setText(getString(R.string.brm_pay));
            mTvPaymentMethod.setText(getString(R.string.payment_type_ordinary_payment));
        }

        if (coinCode == BrahmaConst.COIN_CODE_BTC) {
            chosenToken.setName(BrahmaConst.COIN_BTC);
            chosenToken.setShortName(BrahmaConst.COIN_SYMBOL_BTC);
        } else {
            getEthGasPrice();
            if (coinCode == BrahmaConst.COIN_CODE_BRM) {
                // only support brm
                chosenToken.setName(BrahmaConst.COIN_BRM);
                chosenToken.setShortName(BrahmaConst.COIN_SYMBOL_BRM);
                if (BrahmaConfig.debugFlag) {
                    chosenToken.setAddress(BrahmaConst.KNC_ROPSTEN_NETWORK_CONTRACT_ADDRESS);
                } else {
                    chosenToken.setAddress(BrahmaConst.COIN_BRM_ADDRESS);
                }
            } else {
                chosenToken.setName(BrahmaConst.COIN_ETH);
                chosenToken.setShortName(BrahmaConst.COIN_SYMBOL_ETH);
            }
        }

        // send value
        if (intentParamSendValue != null && intentParamSendValue.length() > 0) {
            mTvTransferAmount.setVisibility(View.VISIBLE);
            mEtTransferAmount.setVisibility(View.GONE);
            mTvTransferAmount.setText(intentParamSendValue);
        } else {
            mTvTransferAmount.setVisibility(View.GONE);
            mEtTransferAmount.setVisibility(View.VISIBLE);
        }

        // coin type
        if (coinCode == BrahmaConst.COIN_CODE_BTC) {
            ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_btc);
            mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_BTC);
            mLayoutChooseToken.setVisibility(View.GONE);
            mLayoutGasPrice.setVisibility(View.GONE);
            mLayoutGasLimit.setVisibility(View.GONE);
            mLayoutBtcFee.setVisibility(View.VISIBLE);
        } else {
            mLayoutGasPrice.setVisibility(View.VISIBLE);
            mLayoutGasLimit.setVisibility(View.VISIBLE);
            mLayoutBtcFee.setVisibility(View.GONE);
            if (coinCode == BrahmaConst.COIN_CODE_BRM) {
                ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_brm);
                mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_BRM);
            } else {
                ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_eth);
                mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_ETH);
            }
        }
    }

    private void initData() {
        // btc wallet app kit init
        btcAppkitSetup = RxEventBus.get().register(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, Boolean.class);
        btcAppkitSetup.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onNext(Boolean flag) {
                        BLog.d(tag(), "the appkit init success");
                        if (flag) {
                            showChosenAccountInfo(chosenAccount);
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i(tag(), e.toString());
                    }
                });

        btcTxBroadcastComplete = RxEventBus.get().register(EventTypeDef.BTC_TRANSACTION_BROADCAST_COMPLETE, String.class);
        btcTxBroadcastComplete.onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String hash) {
                        if (hash != null && hash.length() > 0 && tvTransferStatus != null
                                && customStatusView != null) {
                            tvTransferStatus.setText(R.string.progress_transfer_success);
                            BLog.i(tag(), "the transfer success");
                            customStatusView.loadSuccess();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent();
                                intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_SUCCESS);
                                intent.putExtra(IntentParam.PARAM_PAY_BLOCKCHAIN_TYPE, BrahmaConst.BTC_ACCOUNT_TYPE);
                                intent.putExtra(IntentParam.PARAM_PAY_HASH, hash);
                                setResult(RESULT_OK, intent);
                                finish();
                            }, 1200);
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i(tag(), e.toString());
                    }
                });

        // get accounts
        MainService.getInstance().getAccounts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AccountEntity>>() {
                    @Override
                    public void onCompleted() {
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    @Override
                    public void onNext(List<AccountEntity> accountEntities) {
                        accounts = new ArrayList<>();
                        if (accountEntities != null && accountEntities.size() > 0) {
                            for (AccountEntity accountEntity : accountEntities) {
                                int blockchainType = BrahmaConst.ETH_ACCOUNT_TYPE;
                                if (coinCode == BrahmaConst.COIN_CODE_BTC) {
                                    blockchainType = BrahmaConst.BTC_ACCOUNT_TYPE;
                                }
                                if (accountEntity.getType() == blockchainType) {
                                    accounts.add(accountEntity);
                                }
                            }
                        }
                        if (accounts == null || accounts.size() == 0) {
                            BLog.e(tag(), "the account is null");
                            Intent intent = new Intent();
                            intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_NO_ACCOUNT);
                            intent.putExtra(IntentParam.PARAM_PAY_MSG, getString(R.string.pay_msg_no_account));
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            chosenAccount = accounts.get(0);
                            showChosenAccountInfo(chosenAccount);
                            if (coinCode != BrahmaConst.COIN_CODE_BTC) {
                                getEthereumChainBalance(accounts, coinCode);
                            }
                            if (accounts.size() > 1) {
                                mIvShowAccountsArrow.setVisibility(View.VISIBLE);
                                mLayoutAccount.setOnClickListener(v -> {
                                    showAccounts();
                                    mLayoutChooseAccount.setVisibility(View.VISIBLE);
                                    mLayoutChooseAccount.setAnimation(AnimationUtil.makeInAnimation());
                                });
                            } else {
                                mIvShowAccountsArrow.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        // get pre order id
        createPreOrderId();
    }

    private void createPreOrderId() {
        PayService.getInstance().createCreditPreOrder(BrahmaConfig.getInstance().getPayAccount(),
                    coinCode, intentParamSendValue, paymentRemark)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Map>() {
                        @Override
                        public void onNext(Map orderInfo) {
                            if (orderInfo != null && orderInfo.containsKey(ReqParam.PARAM_ORDER_ID)
                                    && orderInfo.containsKey(ReqParam.PARAM_RECEIVER)) {
                                orderId = (String) orderInfo.get(ReqParam.PARAM_ORDER_ID);
                                receiptAddress = (String) orderInfo.get(ReqParam.PARAM_RECEIVER);
                                BLog.d(tag(), String.format("orderId: %s ; receipt: %s;", orderId, receiptAddress));
                            } else {
                                BLog.d(tag(), "failed create order: " + orderInfo);
                                finish();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });
    }

    private void showChosenAccountInfo(AccountEntity account) {
        ImageManager.showAccountAvatar(this, mIvChosenAccountAvatar, account);
        if (account.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
            WalletAppKit kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
            String accountBalanceStr = "0.00";
            if (kit != null) {
                accountBalance = new BigInteger(String.valueOf(kit.wallet().getBalance().value));
                accountBalanceStr = String.valueOf(CommonUtil.convertUnit(BrahmaConst.BITCOIN, new BigInteger(String.valueOf(kit.wallet().getBalance().value))));
            } else {
                accountBalance = BigInteger.ZERO;
                BtcAccountManager.getInstance().initExistsWalletAppKit(account);
            }
            mTvAccountInfo.setText(String.format("%s (%s BTC)", account.getName(), accountBalanceStr));
        } else {
            BigDecimal balance = BigDecimal.ZERO;
            accountBalance = BigInteger.ZERO;
            if (accountAssets != null && accountAssets.size() > 0) {
                for (AccountAssets assets : accountAssets) {
                    if (assets.getAccountEntity().getId() == chosenAccount.getId()) {
                        accountBalance = assets.getBalance();
                        balance = CommonUtil.convertUnit(BrahmaConst.COIN_SYMBOL_ETH, assets.getBalance());
                    }
                }
            }
            mTvAccountInfo.setText(String.format("%s (%s %s)", account.getName(), String.valueOf(balance.setScale(4, BigDecimal.ROUND_HALF_UP)), chosenToken.getShortName()));
        }
    }

    private void showAccounts() {
        if (accounts != null && accounts.size() > 0) {
            mLayoutAccounts.removeAllViews();
            for (final AccountEntity accountEntity : accounts) {
                final AccountItemView accountItemView = new AccountItemView();
                accountItemView.layoutAccountItem = LayoutInflater.from(QuickPayActivity.this).inflate(R.layout.list_item_pay_account, null);
                accountItemView.ivAccountAvatar = accountItemView.layoutAccountItem.findViewById(R.id.iv_account_avatar);
                accountItemView.tvAccountName = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_name);
                accountItemView.tvAccountAddress = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_address);
                accountItemView.ivChecked = accountItemView.layoutAccountItem.findViewById(R.id.iv_account_checked);

                accountItemView.tvAccountName.setText(accountEntity.getName());
                ImageManager.showAccountAvatar(QuickPayActivity.this, accountItemView.ivAccountAvatar, accountEntity);
                accountItemView.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(accountEntity.getAddress()));
                if (chosenAccount.getId() == accountEntity.getId()) {
                    accountItemView.ivChecked.setVisibility(View.VISIBLE);
                } else {
                    accountItemView.ivChecked.setVisibility(View.GONE);
                }

                accountItemView.layoutAccountItem.setOnClickListener(v1 -> {
                    chosenAccount = accountEntity;
                    showChosenAccountInfo(accountEntity);
                    mLayoutChooseAccount.setVisibility(View.GONE);
                    mLayoutChooseAccount.setAnimation(AnimationUtil.makeOutAnimation());
                });

                mLayoutAccounts.addView(accountItemView.layoutAccountItem);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLayoutEditBtcFee.getVisibility() == View.VISIBLE) {
                mLayoutBtcFee.setVisibility(View.GONE);
                mLayoutBtcFee.setAnimation(AnimationUtil.makeOutAnimation());
            } else if (mLayoutEditGasPrice.getVisibility() == View.VISIBLE) {
                mLayoutEditGasPrice.setVisibility(View.GONE);
                mLayoutEditGasPrice.setAnimation(AnimationUtil.makeOutAnimation());
            } else if (mLayoutChooseAccount.getVisibility() == View.VISIBLE) {
                mLayoutChooseAccount.setVisibility(View.GONE);
                mLayoutChooseAccount.setAnimation(AnimationUtil.makeOutAnimation());
            } else if (mLayoutTransferStatus.getVisibility() == View.VISIBLE) {
                mLayoutTransferStatus.setVisibility(View.GONE);
                mLayoutTransferStatus.setAnimation(AnimationUtil.makeOutAnimation());
            } else if (mLayoutEditPayRemark.getVisibility() == View.VISIBLE) {
                mLayoutEditPayRemark.setVisibility(View.GONE);
                mLayoutEditPayRemark.setAnimation(AnimationUtil.makeOutAnimation());
            } else {
                finish();
            }
        }
        return false;
    }

    private class AccountItemView {
        View layoutAccountItem;
        ImageView ivAccountAvatar;
        TextView tvAccountName;
        TextView tvAccountAddress;
        ImageView ivChecked;
    }

    // get eth/token balance
    private void getEthereumChainBalance(List<AccountEntity> accounts, int coinCode) {
        if (coinCode == BrahmaConst.COIN_CODE_ETH) {
            for (AccountEntity account : accounts) {
                BrahmaWeb3jService.getInstance().getEthBalance(account)
                        .observable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<EthGetBalance>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                BLog.e("get eth balance", e.getMessage());
                            }

                            @Override
                            public void onNext(EthGetBalance ethBalance) {
                                if (ethBalance != null && ethBalance.getBalance() != null) {
                                    BLog.i("view model", "the " + account.getName() + " eth's balance is " + ethBalance.getBalance().toString());
                                    AccountAssets assets = new AccountAssets(account, chosenToken, ethBalance.getBalance());
                                    checkTokenAsset(assets);
                                } else {
                                    BLog.w("view model", "the " + account.getName() + " 's eth balance is " + ethBalance.getBalance().toString());
                                    AccountAssets assets = new AccountAssets(account, chosenToken, BigInteger.ZERO);
                                    checkTokenAsset(assets);
                                }
                            }
                        });
            }
        } else {
            // only support BRM
            for (AccountEntity account : accounts) {
                BrahmaWeb3jService.getInstance().getTokenBalance(account, chosenToken)
                        .observable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<EthCall>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                BLog.e("get erc20 token balance", e.getMessage());
                            }

                            @Override
                            public void onNext(EthCall ethCall) {
                                BigInteger balance = BigInteger.ZERO;
                                if (ethCall != null && ethCall.getValue() != null) {
                                    BLog.i("view model", "the " + account.getName() + "'s " +
                                            chosenToken.getAddress() + " balance is " + ethCall.getValue());
                                    balance = Numeric.decodeQuantity(ethCall.getValue());
                                }
                                AccountAssets assets = new AccountAssets(account, chosenToken, balance);
                                checkTokenAsset(assets);
                            }
                        });
            }
        }
    }

    private void checkTokenAsset(AccountAssets assets) {
        for (AccountAssets localAssets : accountAssets) {
            if (localAssets.getAccountEntity().getType() == assets.getAccountEntity().getType() &&
                    localAssets.getAccountEntity().getType() == BrahmaConst.ETH_ACCOUNT_TYPE &&
                    localAssets.getAccountEntity().getAddress().equals(assets.getAccountEntity().getAddress()) &&
                    localAssets.getTokenEntity().getAddress().equals(assets.getTokenEntity().getAddress())) {
                accountAssets.remove(localAssets);
                break;
            } else if (localAssets.getAccountEntity().getType() == assets.getAccountEntity().getType() &&
                    localAssets.getAccountEntity().getType() == BrahmaConst.BTC_ACCOUNT_TYPE &&
                    localAssets.getAccountEntity().getFilename().equals(assets.getAccountEntity().getFilename()) &&
                    localAssets.getTokenEntity().getName().equals(assets.getTokenEntity().getName()) &&
                    localAssets.getTokenEntity().getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                accountAssets.remove(localAssets);
                break;
            }
        }
        accountAssets.add(assets);
        if (accounts.size() == accountAssets.size()) {
            showChosenAccountInfo(chosenAccount);
            showAccounts();
        }
    }

    private void showPasswordDialog() {
        if (intentParamSendValue == null || intentParamSendValue.length() <= 0) {
            intentParamSendValue = mEtTransferAmount.getText().toString();
            if (intentParamSendValue.length() < 1) {
                showTipDialog(R.string.tip_invalid_amount);
                return;
            }
        }
        if (coinCode == BrahmaConst.COIN_CODE_BTC) {
            transferValue = CommonUtil.convertSatoshiFromBTC(new BigDecimal(intentParamSendValue));
        } else {
            transferValue = CommonUtil.convertWeiFromEther(new BigDecimal(intentParamSendValue));
        }

        if (transferValue.compareTo(accountBalance) > 0) {
            showTipDialog(R.string.tip_insufficient_balance);
            return;
        }

        if (receiptAddress == null || receiptAddress.length() < 1) {
            showTipDialog(R.string.payment_invalid_create_order);
            createPreOrderId();
            return;
        }

        if (orderId == null || orderId.length() < 1) {
            showTipDialog(R.string.payment_invalid_create_order);
            createPreOrderId();
            return;
        }

        if (coinCode == BrahmaConst.COIN_CODE_BTC) {
            showBtcPasswordDialog();
        } else {
            showEthPasswordDialog();
        }
    }

    private void showTipDialog(int messageId) {
        AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(messageId)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .create();
        errorDialog.show();
    }

    private void showEthPasswordDialog() {
        String gasPriceStr = mTvGasPrice.getText().toString().trim();
        String gasLimitStr = mTvGasLimit.getText().toString().trim();
        if (gasPriceStr.length() < 1) {
            showTipDialog(R.string.tip_invalid_gas_price);
            return;
        }

        if (gasLimitStr.length() < 1) {
            showTipDialog(R.string.tip_invalid_gas_limit);
            return;
        }

        BigDecimal gasPrice = new BigDecimal(gasPriceStr);
        BigInteger gasLimit = new BigInteger(gasLimitStr);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
        EditText etPassword = dialogView.findViewById(R.id.et_password);
        AlertDialog passwordDialog = new AlertDialog.Builder(QuickPayActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                    // show transfer progress
                    mLayoutTransferStatus.setVisibility(View.VISIBLE);
                    customStatusView.loadLoading();
                    String password = etPassword.getText().toString();
                    BrahmaWeb3jService.getInstance().sendTransfer(chosenAccount, chosenToken, password, receiptAddress,
                            CommonUtil.getAccountFromWei(transferValue), gasPrice, gasLimit, "")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Object>() {
                                @Override
                                public void onNext(Object result) {
                                    if (result instanceof Integer) {
                                        int flag = (Integer) result;
                                        if (flag == 1) {
                                            tvTransferStatus.setText(R.string.progress_verify_account);
                                        } else if (flag == 2) {
                                            tvTransferStatus.setText(R.string.progress_send_request);
                                        }
                                    } else if (result instanceof String) {
                                        String hash = (String) result;
                                        tvTransferStatus.setText(R.string.progress_transfer_success);
                                        BLog.i(tag(), "the transfer success");
                                        customStatusView.loadSuccess();
                                        new Handler().postDelayed(() -> {
                                            Intent intent = new Intent();
                                            intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_SUCCESS);
                                            intent.putExtra(IntentParam.PARAM_PAY_BLOCKCHAIN_TYPE, BrahmaConst.ETH_ACCOUNT_TYPE);
                                            intent.putExtra(IntentParam.PARAM_PAY_HASH, hash);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }, 1200);
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                    customStatusView.loadFailure();
                                    tvTransferStatus.setText(R.string.progress_transfer_fail);
                                    new Handler().postDelayed(() -> {
                                        mLayoutTransferStatus.setVisibility(View.GONE);
                                        int resId = R.string.tip_error_transfer;
                                        if (e instanceof CipherException) {
                                            resId = R.string.tip_error_password;
                                        } else if (e instanceof TransactionException) {
                                            resId = R.string.tip_error_net;
                                        }
                                        new AlertDialog.Builder(QuickPayActivity.this)
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
                })
                .create();
        passwordDialog.setOnShowListener(dialog -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etPassword, InputMethodManager.SHOW_IMPLICIT);
        });
        passwordDialog.show();
    }

    private void showBtcPasswordDialog() {
        String feePerByte = mTvBtcFee.getText().toString().trim();

        if (feePerByte.length() < 1) {
            showTipDialog(R.string.tip_invalid_fee);
            return;
        }
        BigDecimal feePrice = new BigDecimal(feePerByte);
        long feePerKb = feePrice.multiply(new BigDecimal(BtcAccountManager.BYTES_PER_BTC_KB)).longValue();

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);
        EditText etPassword = dialogView.findViewById(R.id.et_password);
        AlertDialog passwordDialog = new AlertDialog.Builder(QuickPayActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                    String password = etPassword.getText().toString();
                    if (isBtcValidPassword(password)) {
                        // show transfer progress
                        mLayoutTransferStatus.setVisibility(View.VISIBLE);
                        if (BtcAccountManager.getInstance().transfer(receiptAddress, CommonUtil.convertBTCFromSatoshi(transferValue),
                                feePerKb, chosenAccount.getFilename())) {
                            customStatusView.loadLoading();
                        } else {
                            customStatusView.loadFailure();
                            tvTransferStatus.setText(R.string.progress_transfer_fail);
                            new Handler().postDelayed(() -> {
                                mLayoutTransferStatus.setVisibility(View.GONE);
                                int resId = R.string.tip_error_transfer;
                                showTipDialog(R.string.tip_error_transfer);
                            }, 1500);
                        }
                    }
                })
                .create();
        passwordDialog.setOnShowListener(dialog -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etPassword, InputMethodManager.SHOW_IMPLICIT);
        });
        passwordDialog.show();
    }

    private void payAccountRecharge(String txHash) {
        Map<String, Object> params = new HashMap<>();
        params.put(ReqParam.PARAM_ORDER_ID, orderId);
        params.put(ReqParam.PARAM_TX_HASH, txHash);
        Networks.getInstance().getPayApi()
                .payAccountRecharge(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiRespResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(ApiRespResult apr) {
                        if (apr != null) {
                            if (apr.getResult() == 0 && apr.getData() != null) {
                                BLog.i(tag(), apr.toString());
                                Map result = apr.getData();
                                Map<String, Object> orderInfo = new HashMap<>();
                                orderInfo.put(ReqParam.PARAM_ORDER_ID, result.get(ReqParam.PARAM_ORDER_ID));
                                if (result.containsKey(ReqParam.PARAM_RECEIVER_INFO) &&
                                        ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).containsKey(ReqParam.PARAM_RECEIVER)) {
                                    orderInfo.put(ReqParam.PARAM_RECEIVER, ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).get(ReqParam.PARAM_RECEIVER));
                                }
                            } else if (apr.getResult() == ApiConst.INVALID_TOKEN) {
                                /*getPayTokenForQuickPay("createCreditPreOrderByNet", params)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Observer<ApiRespResult>() {
                                            @Override
                                            public void onNext(ApiRespResult apiRespResult) {
                                                if (apr.getResult() == 0 && apr.getData() != null) {
                                                    BLog.i(tag(), apr.toString());
                                                    Map result = apr.getData();
                                                    Map<String, Object> orderInfo = new HashMap<>();
                                                    orderInfo.put(ReqParam.PARAM_ORDER_ID, result.get(ReqParam.PARAM_ORDER_ID));
                                                    if (result.containsKey(ReqParam.PARAM_RECEIVER_INFO) &&
                                                            ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).containsKey(ReqParam.PARAM_RECEIVER)) {
                                                        orderInfo.put(ReqParam.PARAM_RECEIVER, ((Map)result.get(ReqParam.PARAM_RECEIVER_INFO)).get(ReqParam.PARAM_RECEIVER));
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable error) {
                                                error.printStackTrace();
                                            }

                                            @Override
                                            public void onCompleted() {

                                            }
                                        });*/
                            }
                        }
                    }
                });
    }

    private boolean isBtcValidPassword(String password) {
        String mnemonicsCode = DataCryptoUtils.aes128Decrypt(chosenAccount.getCryptoMnemonics(), password);
        if (mnemonicsCode != null) {
            List<String> mnemonicsCodes = Splitter.on(" ").splitToList(mnemonicsCode);
            if (mnemonicsCodes.size() == 0 || mnemonicsCodes.size() % 3 > 0) {
                showTipDialog(R.string.error_current_password);
                return false;
            } else {
                return true;
            }
        } else {
            showTipDialog(R.string.error_current_password);
            return false;
        }
    }

    public void getEthGasPrice() {
        BrahmaWeb3jService.getInstance()
                .getGasPrice()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BigInteger>() {
                    @Override
                    public void onNext(BigInteger gasPrice) {
                        BLog.d(tag(), "the gas price is: " + String.valueOf(gasPrice));
                        BigDecimal gasPriceGwei = Convert.fromWei(new BigDecimal(gasPrice), Convert.Unit.GWEI);
                        mTvGasPrice.setText(String.valueOf(gasPriceGwei));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxEventBus.get().unregister(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, btcAppkitSetup);
        RxEventBus.get().unregister(EventTypeDef.BTC_TRANSACTION_BROADCAST_COMPLETE, btcTxBroadcastComplete);
    }
}
