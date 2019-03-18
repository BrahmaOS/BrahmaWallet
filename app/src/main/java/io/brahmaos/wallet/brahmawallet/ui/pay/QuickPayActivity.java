package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.bumptech.glide.Glide;
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
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.pay.AccountBalance;
import io.brahmaos.wallet.brahmawallet.model.pay.MerchantReceiver;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomStatusView;
import io.brahmaos.wallet.brahmawallet.view.PassWordLayout;
import io.brahmaos.wallet.util.AnimationUtil;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.DataCryptoUtils;
import io.brahmaos.wallet.util.ImageUtil;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QuickPayActivity extends BaseActivity {

    public int passwordLength = 6;
    public static String PARAM_ACCESS_KEY_ID = "access_key_id";
    public static String PARAM_TRADE_TYPE = "trade_type";
    public static String PARAM_AMOUNT = "amount";
    public static String PARAM_COIN_CODE = "coin_code";
    public static String PARAM_PRE_PAY_ID = "prepay_id";
    public static String PARAM_PAY_TYPE = "pay_type";
    public static String PARAM_ORDER_NO = "order_no";
    public static String PARAM_ORDER_DESC = "order_desc";
    public static String PARAM_ORDER_DETAIL = "order_detail";
    public static String PARAM_NOTIFY_URL = "notify_url";
    public static String PARAM_CALLBACK_URL = "callback_url";
    public static String PARAM_ATTACH = "attach";
    public static String PARAM_SIGN_TYPE = "sign_type";
    public static String PARAM_SIGN = "sign";
    public static String PARAM_NONCE = "nonce";
    public static String PARAM_T = "t";
    public static String PARAM_RECEIVER = "receiver";
    public static String PARAM_BALANCE_COIN_CODE = "balance_coin_code";
    public static String PARAM_PAY_PASSWORD = "pay_passwd";
    public static String PARAM_REMARK = "remark";
    public static String PARAM_SENDER = "sender";
    public static String PARAM_TX_HASH = "tx_hash";

    public static int TRADE_TYPE_RECHARGE = 1;
    public static int TRADE_TYPE_PAYMENT = 2;
    public static int PAYMENT_QUICK = 1;
    public static int PAYMENT_ORDINARY = 2;

    private ImageView mImageViewClose;
    private LinearLayout mLayoutTransferInfo;
    private ImageView mImageViewCoin;
    private TextView mTvCoinName;
    private EditText mEtTransferAmount;
    private TextView mTvTransferAmount;
    private TextView mTvCommodityInformation;
    private TextView mTvMerchantName;
    private RelativeLayout mLayoutPaymentMethod;
    private TextView mTvPaymentMethod;
    private ImageView mIvChoosePaymentMethodArrow;

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

    // Choose payment method
    private LinearLayout mLayoutChoosePaymentMethod;
    private ImageView mIvCloseChoosePaymentMethod;
    private RelativeLayout mLayoutQuickPayment;
    private ImageView mIvQuickPayment;
    private RelativeLayout mLayoutOrdinaryPayment;
    private ImageView mIvOrdinaryPayment;

    // input quick account password
    private LinearLayout mLayoutInputQuickAccountPassword;
    private ImageView mIvCloseQuickAccountPassword;
    private PassWordLayout mLayoutPassword;
    private Button mBtnConfirmPassword;

    private LinearLayout mLayoutTransferStatus;
    private CustomStatusView customStatusView;
    private TextView tvTransferStatus;

    // 1: recharge;  2: payment;
    private int tradeType = 0;
    private int paymentMethod = PAYMENT_QUICK;
    // common params
    private int coinCode;
    private String orderId;
    private String intentParamSendValue;
    private BigInteger transferValue;
    private String tradeRemark;

    // pay params
    private String accessKeyId;
    private int payType;
    private String orderNo;
    private String orderDesc;
    private String orderDetail;
    private String notifyUrl;
    private String callbackUrl;
    private String attach;
    private String signType;
    private String sign;
    private int nonce;
    private int timestamp;

    private String receiptAddress;

    private String quickAccount;
    private BigInteger accountBalance;
    private AccountEntity chosenAccount;
    private TokenEntity chosenToken = new TokenEntity();
    private Observable<Boolean> btcAppkitSetup;
    private Observable<String> btcTxBroadcastComplete;
    private List<AccountEntity> accounts = new ArrayList<>();
    private List<AccountAssets> accountAssets = new ArrayList<>();
    private List<AccountBalance> quickAccountBalances = new ArrayList<>();

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

        setContentView(R.layout.activity_pay_quick);
        quickAccount = BrahmaConfig.getInstance().getPayAccount();
        initIntentParam();
        initView();
        initData();
    }

    private void initIntentParam() {
        Uri uri = getIntent().getData();
        String tradeTypeStr = uri.getQueryParameter(PARAM_TRADE_TYPE);
        if (CommonUtil.isNull(tradeTypeStr)) {
            showLongToast(R.string.tip_lack_param_trade_type);
            finish();
            return;
        }
        tradeType = Integer.valueOf(tradeTypeStr);

        intentParamSendValue = uri.getQueryParameter(PARAM_AMOUNT);
        if (CommonUtil.isNull(intentParamSendValue)) {
            showLongToast(R.string.tip_lack_param);
            finish();
            return;
        }
        String coinCodeStr = uri.getQueryParameter(PARAM_COIN_CODE);
        if (CommonUtil.isNull(coinCodeStr)) {
            showLongToast(R.string.tip_lack_param);
            finish();
            return;
        }
        try {
            coinCode = Integer.valueOf(coinCodeStr);
        } catch (NumberFormatException e) {
            showLongToast(R.string.tip_invalid_param);
            finish();
            return;
        }

        if (tradeType == TRADE_TYPE_PAYMENT) {
            accessKeyId = uri.getQueryParameter(PARAM_ACCESS_KEY_ID);
            orderId = uri.getQueryParameter(PARAM_PRE_PAY_ID);
            notifyUrl = uri.getQueryParameter(PARAM_NOTIFY_URL);
            sign = uri.getQueryParameter(PARAM_SIGN);
            orderNo = uri.getQueryParameter(PARAM_ORDER_NO);
            orderDesc = uri.getQueryParameter(PARAM_ORDER_DESC);
            String nonceStr = uri.getQueryParameter(PARAM_NONCE);
            String timestampStr = uri.getQueryParameter(PARAM_T);
            if (accessKeyId == null || orderId == null || notifyUrl == null
                    || sign == null || nonceStr == null || timestampStr == null
                    || orderNo == null || orderDesc == null) {
                showLongToast(R.string.tip_lack_param);
                finish();
            }
            callbackUrl = uri.getQueryParameter(PARAM_CALLBACK_URL);
            attach = uri.getQueryParameter(PARAM_ATTACH);
            nonce = Integer.valueOf(nonceStr);
            timestamp = Integer.valueOf(timestampStr);
            payType = Integer.valueOf(uri.getQueryParameter(PARAM_PAY_TYPE));
            signType = uri.getQueryParameter(PARAM_SIGN_TYPE);
        }

        tradeRemark = uri.getQueryParameter("remark");
        if (tradeRemark == null) {
            tradeRemark = "";
        }
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
        mIvChoosePaymentMethodArrow = findViewById(R.id.iv_choose_payment_method_arrow);

        mLayoutPaymentMethod = findViewById(R.id.layout_payment_method);
        mLayoutChoosePaymentMethod = findViewById(R.id.layout_choose_payment_method);
        mIvCloseChoosePaymentMethod = findViewById(R.id.iv_close_payment_method);
        mIvCloseChoosePaymentMethod.setOnClickListener(v -> {
            mLayoutChoosePaymentMethod.setVisibility(View.GONE);
            mLayoutChoosePaymentMethod.setAnimation(AnimationUtil.makeOutAnimation());
        });
        mLayoutQuickPayment = findViewById(R.id.layout_choose_quick_payment);
        mLayoutQuickPayment.setOnClickListener(v -> {
            choosePaymentMethod(PAYMENT_QUICK);
        });
        mIvQuickPayment = findViewById(R.id.iv_quick_payment_checked);
        mLayoutOrdinaryPayment = findViewById(R.id.layout_choose_ordinary_payment);
        mLayoutOrdinaryPayment.setOnClickListener(v -> {
            choosePaymentMethod(PAYMENT_ORDINARY);
        });
        mIvOrdinaryPayment = findViewById(R.id.iv_ordinary_payment_checked);

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

        mLayoutInputQuickAccountPassword = findViewById(R.id.layout_input_quick_account_password);
        mIvCloseQuickAccountPassword = findViewById(R.id.iv_close_quick_account_password);
        mIvCloseQuickAccountPassword.setOnClickListener(v -> {
            mLayoutInputQuickAccountPassword.setVisibility(View.GONE);
            mLayoutInputQuickAccountPassword.setAnimation(AnimationUtil.makeOutAnimation());
        });
        mLayoutPassword = findViewById(R.id.et_quick_account_password);
        mBtnConfirmPassword = findViewById(R.id.btn_commit_quick_account_password);

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
        if (tradeType == TRADE_TYPE_RECHARGE) {
            mTvCommodityInformation.setText(getString(R.string.label_quick_payment_account_recharge));
            mTvMerchantName.setText(getString(R.string.brm_pay));
            paymentMethod = PAYMENT_ORDINARY;
            mIvChoosePaymentMethodArrow.setVisibility(View.GONE);
        } else if (tradeType == TRADE_TYPE_PAYMENT) {
            mTvCommodityInformation.setText(orderDesc);
            if (quickAccount != null && quickAccount.length() > 0) {
                paymentMethod = PAYMENT_QUICK;
                mIvChoosePaymentMethodArrow.setVisibility(View.VISIBLE);
                mLayoutPaymentMethod.setOnClickListener(v -> {
                    if (paymentMethod == PAYMENT_QUICK) {
                        mIvQuickPayment.setVisibility(View.VISIBLE);
                        mIvOrdinaryPayment.setVisibility(View.GONE);
                    } else {
                        mIvQuickPayment.setVisibility(View.GONE);
                        mIvOrdinaryPayment.setVisibility(View.VISIBLE);
                    }
                    mLayoutChoosePaymentMethod.setVisibility(View.VISIBLE);
                    mLayoutChoosePaymentMethod.setAnimation(AnimationUtil.makeInAnimation());
                });
            } else {
                mIvChoosePaymentMethodArrow.setVisibility(View.GONE);
                paymentMethod = PAYMENT_ORDINARY;
            }
        }

        if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
            chosenToken.setName(BrahmaConst.COIN_BTC);
            chosenToken.setShortName(BrahmaConst.COIN_SYMBOL_BTC);

            ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_btc);
            mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_BTC);
        } else {
            getEthGasPrice();
            if (coinCode == BrahmaConst.PAY_COIN_CODE_BRM) {
                ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_brm);
                mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_BRM);

                // only support brm
                chosenToken.setName(BrahmaConst.COIN_BRM);
                chosenToken.setShortName(BrahmaConst.COIN_SYMBOL_BRM);
                if (BrahmaConfig.debugFlag) {
                    chosenToken.setAddress(BrahmaConst.KNC_ROPSTEN_NETWORK_CONTRACT_ADDRESS);
                } else {
                    chosenToken.setAddress(BrahmaConst.COIN_BRM_ADDRESS);
                }
            } else {
                ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_eth);
                mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_ETH);

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

        showPaymentMethod();
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

        // get quick account balance
        if (tradeType != TRADE_TYPE_RECHARGE) {
            getQuickAccountBalance();
        }

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
                                if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
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
                            // get blockchain balance for ordinary payment
                            if (coinCode != BrahmaConst.PAY_COIN_CODE_BTC) {
                                getEthereumChainBalance(accounts, coinCode);
                            }
                            showChosenAccountInfo(chosenAccount);
                        }
                    }
                });

        // request order according trade type
        createPreOrderId();
    }

    // checked payment method
    private void choosePaymentMethod(int value) {
        paymentMethod = value;
        showPaymentMethod();
        mLayoutChoosePaymentMethod.setVisibility(View.GONE);
        mLayoutChoosePaymentMethod.setAnimation(AnimationUtil.makeOutAnimation());
    }

    // change payment method
    private void showPaymentMethod() {
        showChosenAccountInfo(chosenAccount);
        if (paymentMethod == PAYMENT_ORDINARY) {
            mTvPaymentMethod.setText(R.string.payment_type_ordinary_payment);
            if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
                mLayoutGasPrice.setVisibility(View.GONE);
                mLayoutGasLimit.setVisibility(View.GONE);
                mLayoutBtcFee.setVisibility(View.VISIBLE);
            } else {
                mLayoutGasPrice.setVisibility(View.VISIBLE);
                mLayoutGasLimit.setVisibility(View.VISIBLE);
                mLayoutBtcFee.setVisibility(View.GONE);
            }
        } else {
            mTvPaymentMethod.setText(R.string.payment_type_quick_payment);
            mLayoutGasPrice.setVisibility(View.GONE);
            mLayoutGasLimit.setVisibility(View.GONE);
            mLayoutBtcFee.setVisibility(View.GONE);
        }
    }

    private void createPreOrderId() {
        if (tradeType == TRADE_TYPE_RECHARGE) {
            rechargePreOrderId();
        } else if (tradeType == TRADE_TYPE_PAYMENT) {
            requestOrderFromMerchant();
        }
    }

    private void rechargePreOrderId() {
        PayService.getInstance().createRechargePreOrder(BrahmaConfig.getInstance().getPayAccount(),
                    coinCode, intentParamSendValue, tradeRemark)
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
                                showLongToast(R.string.payment_invalid_create_order);
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

    private void requestOrderFromMerchant() {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_ACCESS_KEY_ID, accessKeyId);
        params.put(PARAM_PRE_PAY_ID, orderId);
        params.put(PARAM_PAY_TYPE, payType);
        params.put(PARAM_ORDER_NO, orderNo);
        params.put(PARAM_ORDER_DESC, orderDesc);
        params.put(PARAM_COIN_CODE, coinCode);
        params.put(PARAM_AMOUNT, intentParamSendValue);
        params.put(PARAM_NOTIFY_URL, notifyUrl);
        params.put(PARAM_SIGN, sign);
        params.put(PARAM_NONCE, nonce);
        params.put(PARAM_T, timestamp);
        if (orderDetail != null && orderDetail.length() > 0) {
            params.put(PARAM_ORDER_DETAIL, orderDetail);
        }
        if (callbackUrl != null && callbackUrl.length() > 0) {
            params.put(PARAM_CALLBACK_URL, callbackUrl);
        }
        if (attach != null && attach.length() > 0) {
            params.put(PARAM_ATTACH, attach);
        }
        if (signType != null && signType.length() > 0) {
            params.put(PARAM_SIGN_TYPE, signType);
        }
        PayService.getInstance().payRequestOrder(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MerchantReceiver>() {
                    @Override
                    public void onNext(MerchantReceiver merchantReceiver) {
                        if (merchantReceiver != null ) {
                            receiptAddress = merchantReceiver.getReceiver().getAddress();
                            mTvMerchantName.setText(merchantReceiver.getMerchant().getName());
                        } else {
                            BLog.d(tag(), "failed pay request order: " + merchantReceiver);
                            showLongToast(R.string.payment_invalid_create_order);
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
        if (paymentMethod == PAYMENT_QUICK) {
            mIvShowAccountsArrow.setVisibility(View.GONE);
            Bitmap quickAccountAvatar = BrahmaConfig.getInstance().getPayAccountAvatar();
            if (quickAccountAvatar != null) {
                Glide.with(this)
                        .load(ImageUtil.getCircleBitmap(quickAccountAvatar))
                        .into(mIvChosenAccountAvatar);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_default_account_avatar)
                        .into(mIvChosenAccountAvatar);
            }
            String coinBalance = null;
            if (quickAccountBalances != null && quickAccountBalances.size() > 0) {
                for (AccountBalance balance : quickAccountBalances) {
                    if (balance.getCoinCode() == coinCode) {
                        coinBalance = balance.getBalance();
                    }
                }
            }
            if (coinBalance != null && coinBalance.length() > 0) {
                mTvAccountInfo.setText(String.format("%s (%s %s)", BrahmaConfig.getInstance().getPayAccountName(), coinBalance, chosenToken.getShortName()));
            } else {
                mTvAccountInfo.setText(String.format("%s", BrahmaConfig.getInstance().getPayAccountName()));
            }
        } else if (account != null) {
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
            } else if (mLayoutChoosePaymentMethod.getVisibility() == View.VISIBLE) {
                mLayoutChoosePaymentMethod.setVisibility(View.GONE);
                mLayoutChoosePaymentMethod.setAnimation(AnimationUtil.makeOutAnimation());
            } else if (mLayoutInputQuickAccountPassword.getVisibility() == View.VISIBLE) {
                mLayoutInputQuickAccountPassword.setVisibility(View.GONE);
                mLayoutInputQuickAccountPassword.setAnimation(AnimationUtil.makeOutAnimation());
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
        if (coinCode == BrahmaConst.PAY_COIN_CODE_ETH) {
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
        if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
            transferValue = CommonUtil.convertSatoshiFromBTC(new BigDecimal(intentParamSendValue));
        } else {
            transferValue = CommonUtil.convertWeiFromEther(new BigDecimal(intentParamSendValue));
        }

        if (paymentMethod == PAYMENT_ORDINARY) {
            // judge blockchain account balance
            if (transferValue.compareTo(accountBalance) > 0) {
                showTipDialog(R.string.tip_insufficient_balance);
                return;
            }
        } else {
            // judge quick account balance
            String quickAccountBalanceStr = "0";
            for (AccountBalance accountBalance : quickAccountBalances) {
                if (accountBalance.getCoinCode() == coinCode) {
                    quickAccountBalanceStr = accountBalance.getBalance();
                    break;
                }
            }
            BigInteger quickAccountBalance;
            if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
                quickAccountBalance = CommonUtil.convertSatoshiFromBTC(new BigDecimal(quickAccountBalanceStr));
            } else {
                quickAccountBalance = CommonUtil.convertWeiFromEther(new BigDecimal(quickAccountBalanceStr));
            }
            if (transferValue.compareTo(quickAccountBalance) > 0) {
                showTipDialog(R.string.tip_insufficient_balance);
                return;
            }
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

        tradeRemark = mTvPaymentRemark.getText().toString().trim();

        if (paymentMethod == PAYMENT_QUICK) {
            showQuickPayPassword();
        } else if (paymentMethod == PAYMENT_ORDINARY) {
            if (coinCode == BrahmaConst.PAY_COIN_CODE_BTC) {
                showBtcPasswordDialog();
            } else {
                showEthPasswordDialog();
            }
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

    private void showQuickPayPassword() {
        mLayoutInputQuickAccountPassword.setVisibility(View.VISIBLE);
        mLayoutInputQuickAccountPassword.setAnimation(AnimationUtil.makeInAnimation());
        mLayoutPassword.removeAllPwd();
        mLayoutPassword.callOnClick();
        mLayoutPassword.setPwdChangeListener(new PassWordLayout.pwdChangeListener() {
            @Override
            public void onChange(String pwd) {
                if (pwd != null && pwd.length() == passwordLength) {
                    mBtnConfirmPassword.setEnabled(true);
                } else {
                    mBtnConfirmPassword.setEnabled(false);
                }
            }

            @Override
            public void onNull() {

            }

            @Override
            public void onFinished(String pwd) {
                mBtnConfirmPassword.setEnabled(true);
            }
        });
        mBtnConfirmPassword.setOnClickListener(v -> {
            mLayoutInputQuickAccountPassword.setVisibility(View.GONE);
            mLayoutTransferStatus.setVisibility(View.VISIBLE);
            customStatusView.loadLoading();
            tvTransferStatus.setText(R.string.progress_transfer);
            quickPayment(mLayoutPassword.getPassString());
        });
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
                    if (tradeType == TRADE_TYPE_RECHARGE) {
                        accountEthereumRecharge(password, gasPrice, gasLimit);
                    } else if (tradeType == TRADE_TYPE_PAYMENT) {
                        if (paymentMethod == PAYMENT_ORDINARY) {
                            ordinaryEthereumPayment(password, gasPrice, gasLimit);
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
                        customStatusView.loadLoading();
                        if (tradeType == TRADE_TYPE_RECHARGE) {
                            accountBitcoinRecharge(feePerKb);
                        } else if (tradeType == TRADE_TYPE_PAYMENT) {
                            if (paymentMethod == PAYMENT_ORDINARY) {
                                ordinaryBitcoinPayment(feePerKb);
                            }
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

    // Quick account ethereum recharge
    private void accountEthereumRecharge(String password, BigDecimal gasPrice, BigInteger gasLimit) {
        BrahmaWeb3jService.getInstance().accountRechargeWithEthereum(chosenAccount, chosenToken, password, receiptAddress,
                CommonUtil.getAccountFromWei(transferValue), gasPrice, gasLimit, "", orderId)
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
                            } else if (flag == -1) {
                                customStatusView.loadFailure();
                                tvTransferStatus.setText(R.string.progress_transfer_fail);
                                new Handler().postDelayed(() -> {
                                    mLayoutTransferStatus.setVisibility(View.GONE);
                                    int resId = R.string.tip_error_transfer;
                                    new AlertDialog.Builder(QuickPayActivity.this)
                                            .setMessage(resId)
                                            .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                            .create().show();
                                }, 1500);
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
    }

    // Quick account Bitcoin recharge
    private void accountBitcoinRecharge(long feePerKb) {
        BtcAccountManager.getInstance().accountRecharge(receiptAddress, CommonUtil.convertBTCFromSatoshi(transferValue),
                feePerKb, chosenAccount.getFilename(), orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String result) {
                        if (result != null && result.length() > 0) {
                            tvTransferStatus.setText(R.string.progress_transfer_success);
                            BLog.i(tag(), "the transfer success");
                            customStatusView.loadSuccess();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent();
                                intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_SUCCESS);
                                intent.putExtra(IntentParam.PARAM_PAY_BLOCKCHAIN_TYPE, BrahmaConst.ETH_ACCOUNT_TYPE);
                                intent.putExtra(IntentParam.PARAM_PAY_HASH, result);
                                setResult(RESULT_OK, intent);
                                finish();
                            }, 1200);
                        } else {
                            customStatusView.loadFailure();
                            tvTransferStatus.setText(R.string.progress_transfer_fail);
                            new Handler().postDelayed(() -> {
                                mLayoutTransferStatus.setVisibility(View.GONE);
                                int resId = R.string.tip_error_transfer;
                                new AlertDialog.Builder(QuickPayActivity.this)
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
    }

    // Quick account quick pay
    private void quickPayment(String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PRE_PAY_ID, orderId);
        params.put(PARAM_RECEIVER, receiptAddress);
        params.put(PARAM_BALANCE_COIN_CODE, coinCode);
        params.put(PARAM_PAY_PASSWORD, DataCryptoUtils.shaEncrypt(password));
        params.put(PARAM_REMARK, tradeRemark);
        PayService.getInstance().paymentOrder(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiRespResult>() {
                    @Override
                    public void onNext(ApiRespResult apr) {
                        if (apr != null && apr.getResult() == 0) {
                            tvTransferStatus.setText(R.string.progress_transfer_success);
                            BLog.i(tag(), "the transfer success");
                            customStatusView.loadSuccess();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent();
                                intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_SUCCESS);
                                intent.putExtra(IntentParam.PARAM_PAY_BLOCKCHAIN_TYPE, BrahmaConst.ETH_ACCOUNT_TYPE);
                                setResult(RESULT_OK, intent);
                                finish();
                            }, 1200);
                        } else {
                            customStatusView.loadFailure();
                            tvTransferStatus.setText(R.string.progress_transfer_fail);
                            new Handler().postDelayed(() -> {
                                mLayoutTransferStatus.setVisibility(View.GONE);
                                new AlertDialog.Builder(QuickPayActivity.this)
                                        .setMessage(R.string.tip_error_transfer)
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
                            mLayoutTransferStatus.setVisibility(View.GONE);
                            new AlertDialog.Builder(QuickPayActivity.this)
                                    .setMessage(R.string.tip_error_transfer)
                                    .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                    .create().show();
                        }, 1500);

                        BLog.i(tag(), "the transfer failed");
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

    // Ordinary Ethereum payment
    private void ordinaryEthereumPayment(String password, BigDecimal gasPrice, BigInteger gasLimit) {
        if (tradeRemark == null || tradeRemark.isEmpty()) {
            tradeRemark = "";
        }
        BrahmaWeb3jService.getInstance().paymentWithEthereum(chosenAccount, chosenToken, password, receiptAddress,
                CommonUtil.getAccountFromWei(transferValue), gasPrice, gasLimit, tradeRemark, orderId)
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
                            } else if (flag == -1) {
                                customStatusView.loadFailure();
                                tvTransferStatus.setText(R.string.progress_transfer_fail);
                                new Handler().postDelayed(() -> {
                                    mLayoutTransferStatus.setVisibility(View.GONE);
                                    int resId = R.string.tip_error_transfer;
                                    new AlertDialog.Builder(QuickPayActivity.this)
                                            .setMessage(resId)
                                            .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                            .create().show();
                                }, 1500);
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
    }

    // Ordinary Bitcoin payment
    private void ordinaryBitcoinPayment(long feePerKb) {
        if (tradeRemark == null || tradeRemark.isEmpty()) {
            tradeRemark = "";
        }
        BtcAccountManager.getInstance().ordinaryPaymentWithBtc(receiptAddress, CommonUtil.convertBTCFromSatoshi(transferValue),
                feePerKb, chosenAccount.getFilename(), orderId, tradeRemark)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String result) {
                        if (result != null && result.length() > 0) {
                            tvTransferStatus.setText(R.string.progress_transfer_success);
                            BLog.i(tag(), "the transfer success");
                            customStatusView.loadSuccess();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent();
                                intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_SUCCESS);
                                intent.putExtra(IntentParam.PARAM_PAY_BLOCKCHAIN_TYPE, BrahmaConst.ETH_ACCOUNT_TYPE);
                                intent.putExtra(IntentParam.PARAM_PAY_HASH, result);
                                setResult(RESULT_OK, intent);
                                finish();
                            }, 1200);
                        } else {
                            customStatusView.loadFailure();
                            tvTransferStatus.setText(R.string.progress_transfer_fail);
                            new Handler().postDelayed(() -> {
                                mLayoutTransferStatus.setVisibility(View.GONE);
                                int resId = R.string.tip_error_transfer;
                                new AlertDialog.Builder(QuickPayActivity.this)
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
                            mLayoutTransferStatus.setVisibility(View.GONE);
                            new AlertDialog.Builder(QuickPayActivity.this)
                                    .setMessage(R.string.tip_error_transfer)
                                    .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                    .create().show();
                        }, 1500);

                        BLog.i(tag(), "the transfer failed");
                    }

                    @Override
                    public void onCompleted() {
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

    private void getQuickAccountBalance() {
        if (BrahmaConfig.getInstance().getPayAccount() != null) {
            PayService.getInstance().getAccountBalance()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<AccountBalance>>() {
                        @Override
                        public void onNext(List<AccountBalance> results) {
                            quickAccountBalances = PayService.getInstance().getAccountBalances();
                            showChosenAccountInfo(chosenAccount);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxEventBus.get().unregister(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, btcAppkitSetup);
        RxEventBus.get().unregister(EventTypeDef.BTC_TRANSACTION_BROADCAST_COMPLETE, btcTxBroadcastComplete);
    }
}
