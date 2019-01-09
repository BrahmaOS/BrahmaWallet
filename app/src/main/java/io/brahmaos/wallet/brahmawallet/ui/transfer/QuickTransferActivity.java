package io.brahmaos.wallet.brahmawallet.ui.transfer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
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
import com.hwangjr.rxbus.RxBus;

import org.bitcoinj.kits.WalletAppKit;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomStatusView;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.DataCryptoUtils;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QuickTransferActivity extends BaseActivity {

    private ImageView mImageViewClose;
    private LinearLayout mLayoutTransferInfo;
    private ImageView mImageViewCoin;
    private TextView mTvCoinName;
    private EditText mEtTransferAmount;
    private TextView mTvTransferAmount;
    private TextView mTvReceiptAddress;
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

    private LinearLayout mLayoutTransferStatus;
    private CustomStatusView customStatusView;
    private TextView tvTransferStatus;

    private String receiptAddress;
    private int blockchainType;
    private BigInteger intentParamSendValue;
    private BigInteger transferValue;
    private BigInteger accountBalance;
    private String tokenAddress;
    private AccountEntity chosenAccount;
    private TokenEntity chosenToken = new TokenEntity();
    private Observable<Boolean> btcAppkitSetup;
    private Observable<String> btcTxBroadcastComplete;
    private List<AccountEntity> accounts = new ArrayList<>();
    private List<AccountAssets> accountAssets = new ArrayList<>();

    @Override
    protected String tag() {
        return QuickTransferActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // translucent status bar
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_quick_transfer);
        receiptAddress = getIntent().getStringExtra(IntentParam.PARAM_PAY_RECEIPT_ADDRESS);
        blockchainType = getIntent().getIntExtra(IntentParam.PARAM_PAY_BLOCKCHAIN_TYPE, 0);
        intentParamSendValue = (BigInteger)getIntent().getSerializableExtra(IntentParam.PARAM_PAY_SEND_VALUE);
        tokenAddress = getIntent().getStringExtra(IntentParam.PARAM_PAY_TOKEN_ADDRESS);

        if (blockchainType == BrahmaConst.BTC_ACCOUNT_TYPE) {
            chosenToken.setName(BrahmaConst.COIN_BTC);
            chosenToken.setShortName(BrahmaConst.COIN_SYMBOL_BTC);
        } else {
            if (tokenAddress != null && tokenAddress.length() > 0) {
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
        initView();

        // set receipt address
        mTvReceiptAddress.setText(receiptAddress);

        // send value
        if (intentParamSendValue != null && intentParamSendValue.compareTo(BigInteger.ZERO) > 0) {
            mTvTransferAmount.setVisibility(View.VISIBLE);
            mEtTransferAmount.setVisibility(View.GONE);
            if (blockchainType == BrahmaConst.ETH_ACCOUNT_TYPE) {
                mTvTransferAmount.setText(CommonUtil.getAccountFromWei(intentParamSendValue).toString());
            } else if (blockchainType == BrahmaConst.BTC_ACCOUNT_TYPE) {
                mTvTransferAmount.setText(CommonUtil.convertBTCFromSatoshi(intentParamSendValue.longValue()).setScale(4, BigDecimal.ROUND_HALF_UP).toString());
            }
        } else {
            mTvTransferAmount.setVisibility(View.GONE);
            mEtTransferAmount.setVisibility(View.VISIBLE);
        }

        // coin type
        if (blockchainType == BrahmaConst.ETH_ACCOUNT_TYPE) {
            mLayoutGasPrice.setVisibility(View.VISIBLE);
            mLayoutGasLimit.setVisibility(View.VISIBLE);
            mLayoutBtcFee.setVisibility(View.GONE);
            if (tokenAddress != null && tokenAddress.length() > 1) {
                ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_brm);
                mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_BRM);
            } else {
                ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_eth);
                mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_ETH);
            }
        } else if (blockchainType == BrahmaConst.BTC_ACCOUNT_TYPE) {
            ImageManager.showTokenIcon(this, mImageViewCoin, R.drawable.icon_btc);
            mTvCoinName.setText(BrahmaConst.COIN_SYMBOL_BTC);
            mLayoutChooseToken.setVisibility(View.GONE);
            mLayoutGasPrice.setVisibility(View.GONE);
            mLayoutGasLimit.setVisibility(View.GONE);
            mLayoutBtcFee.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent();
            intent.putExtra(IntentParam.PARAM_PAY_ERROR_CODE, BrahmaConst.PAY_CODE_INVALID_BLOCKCHAIN);
            intent.putExtra(IntentParam.PARAM_PAY_MSG, getString(R.string.pay_msg_invalid_blockchain));
            setResult(RESULT_OK, intent);
            finish();
        }

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
                            if (blockchainType == BrahmaConst.ETH_ACCOUNT_TYPE) {
                                getEthereumChainBalance(accounts, tokenAddress);
                            }
                            if (accounts.size() > 1) {
                                mIvShowAccountsArrow.setVisibility(View.VISIBLE);
                                mLayoutAccount.setOnClickListener(v -> {
                                    showAccounts();
                                    mLayoutChooseAccount.setVisibility(View.VISIBLE);
                                });
                            } else {
                                mIvShowAccountsArrow.setVisibility(View.GONE);
                            }
                        }
                    }
                });
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
        mTvReceiptAddress = findViewById(R.id.tv_pay_to_address);
        mLayoutBtcFee = findViewById(R.id.layout_btc_fee);
        mTvBtcFee = findViewById(R.id.tv_btc_fee);
        mLayoutBtcFee.setOnClickListener(v -> {
            etBtcFee.setText(mTvBtcFee.getText());
            mLayoutEditBtcFee.setVisibility(View.VISIBLE);
        });

        mLayoutEditBtcFee = findViewById(R.id.layout_edit_btc_fee);
        etBtcFee = findViewById(R.id.et_btc_fee);
        ivCloseBtcFee = findViewById(R.id.iv_close_btc_fee);
        ivCloseBtcFee.setOnClickListener(v -> {
            mLayoutEditBtcFee.setVisibility(View.GONE);
        });
        btnConfirmBtcFee = findViewById(R.id.btn_commit_btc_fee);
        btnConfirmBtcFee.setOnClickListener(v -> {
            String feePerByte = etBtcFee.getText().toString().trim();
            if (feePerByte.length() >= 1) {
                mTvBtcFee.setText(feePerByte);
            }
            mLayoutEditBtcFee.setVisibility(View.GONE);
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
        });

        mLayoutGasPrice = findViewById(R.id.layout_eth_gas_price);
        mTvGasPrice = findViewById(R.id.tv_eth_gas_price);
        mTvGasLimit = findViewById(R.id.tv_eth_gas_limit);
        mLayoutGasPrice.setOnClickListener(v -> {
            etGasPrice.setText(mTvGasPrice.getText());
            etGasLimit.setText(mTvGasLimit.getText());
            mLayoutEditGasPrice.setVisibility(View.VISIBLE);
        });
        mLayoutGasLimit = findViewById(R.id.layout_eth_gas_limit);
        mLayoutGasLimit.setOnClickListener(v -> {
            etGasPrice.setText(mTvGasPrice.getText());
            etGasLimit.setText(mTvGasLimit.getText());
            mLayoutEditGasPrice.setVisibility(View.VISIBLE);
        });
        mLayoutEditGasPrice = findViewById(R.id.layout_edit_eth_gas_price);
        btnConfirmGas = findViewById(R.id.btn_commit_gas_price);
        etGasLimit = findViewById(R.id.et_gas_limit);
        etGasPrice = findViewById(R.id.et_gas_price);
        ivCloseGasPrice = findViewById(R.id.iv_close_eth_gas_price);
        ivCloseGasPrice.setOnClickListener(v -> {
            mLayoutEditGasPrice.setVisibility(View.GONE);
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
                accountItemView.layoutAccountItem = LayoutInflater.from(QuickTransferActivity.this).inflate(R.layout.list_item_pay_account, null);
                accountItemView.ivAccountAvatar = accountItemView.layoutAccountItem.findViewById(R.id.iv_account_avatar);
                accountItemView.tvAccountName = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_name);
                accountItemView.tvAccountAddress = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_address);
                accountItemView.ivChecked = accountItemView.layoutAccountItem.findViewById(R.id.iv_account_checked);

                accountItemView.tvAccountName.setText(accountEntity.getName());
                ImageManager.showAccountAvatar(QuickTransferActivity.this, accountItemView.ivAccountAvatar, accountEntity);
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
            } else if (mLayoutEditGasPrice.getVisibility() == View.VISIBLE) {
                mLayoutEditGasPrice.setVisibility(View.GONE);
            } else if (mLayoutChooseAccount.getVisibility() == View.VISIBLE) {
                mLayoutChooseAccount.setVisibility(View.GONE);
            } else if (mLayoutTransferStatus.getVisibility() == View.VISIBLE) {
                mLayoutTransferStatus.setVisibility(View.GONE);
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
    private void getEthereumChainBalance(List<AccountEntity> accounts, String tokenAddress) {
        if (tokenAddress == null || tokenAddress.length() <= 0) {
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
        if (intentParamSendValue == null || intentParamSendValue.compareTo(BigInteger.ZERO) <= 0) {
            String sendValueStr = mEtTransferAmount.getText().toString();
            if (sendValueStr.length() < 1) {
                showTipDialog(R.string.tip_invalid_amount);
                return;
            }
            if (blockchainType == BrahmaConst.BTC_ACCOUNT_TYPE) {
                transferValue = CommonUtil.convertSatoshiFromBTC(new BigDecimal(sendValueStr));
            } else {
                transferValue = CommonUtil.convertWeiFromEther(new BigDecimal(sendValueStr));
            }
        } else {
            transferValue = intentParamSendValue;
        }

        if (transferValue.compareTo(accountBalance) > 0) {
            showTipDialog(R.string.tip_insufficient_balance);
            return;
        }

        if (blockchainType == BrahmaConst.ETH_ACCOUNT_TYPE) {
            showEthPasswordDialog();
        } else {
            showBtcPasswordDialog();
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
        AlertDialog passwordDialog = new AlertDialog.Builder(QuickTransferActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
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
                                        new AlertDialog.Builder(QuickTransferActivity.this)
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
        AlertDialog passwordDialog = new AlertDialog.Builder(QuickTransferActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxEventBus.get().unregister(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, btcAppkitSetup);
        RxEventBus.get().unregister(EventTypeDef.BTC_TRANSACTION_BROADCAST_COMPLETE, btcTxBroadcastComplete);
    }
}
