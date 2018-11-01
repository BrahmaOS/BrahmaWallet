package io.brahmaos.wallet.brahmawallet.ui.account;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletTransaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.transaction.EthTransactionsActivity;
import io.brahmaos.wallet.brahmawallet.ui.transaction.TransactionsActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;

public class BtcAccountAssetsActivity extends BaseActivity {

    @Override
    protected String tag() {
        return BtcAccountAssetsActivity.class.getName();
    }

    public static final int REQ_CODE_TRANSFER = 10;

    // UI references.
    @BindView(R.id.layout_account_info)
    RelativeLayout mLayoutAccountInfo;
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.tv_account_balance)
    TextView tvAccountBalance;
    @BindView(R.id.tv_total_assets)
    TextView tvTotalAssets;
    @BindView(R.id.iv_currency_unit)
    ImageView ivCurrencyUnit;
    @BindView(R.id.layout_app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.layout_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.iv_account_bg)
    ImageView ivAccountBg;
    @BindView(R.id.tv_btc_price)
    TextView tvBtcPrice;
    @BindView(R.id.tv_btc_price_unit)
    TextView tvBtcPriceUnit;
    @BindView(R.id.layout_sync_status)
    RelativeLayout mLayoutSyncStatus;
    @BindView(R.id.layout_receive_btc)
    LinearLayout mLayoutReceiveBtc;
    @BindView(R.id.layout_send_btc)
    LinearLayout mLayoutSendBtc;

    private int accountId;
    private AccountEntity account;
    private AccountViewModel mViewModel;
    private WalletAppKit kit;
    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_btc_assets);
        ButterKnife.bind(this);
        showNavBackBtn();
        RxBus.get().register(this);
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_CNY)) {
            Glide.with(BtcAccountAssetsActivity.this)
                    .load(R.drawable.currency_cny_white)
                    .into(ivCurrencyUnit);
            tvBtcPriceUnit.setText(BrahmaConst.UNIT_PRICE_CNY);
        } else {
            Glide.with(BtcAccountAssetsActivity.this)
                    .load(R.drawable.currency_usd_white)
                    .into(ivCurrencyUnit);
            tvBtcPriceUnit.setText(BrahmaConst.UNIT_PRICE_USD);
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        cryptoCurrencies = MainService.getInstance().getCryptoCurrencies();
        appBarLayout.setExpanded(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getAccountById(accountId)
                .observe(this, (AccountEntity accountEntity) -> {
                    if (accountEntity != null) {
                        account = accountEntity;
                        kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
                        initView();
                        initAssets();
                    } else {
                        finish();
                    }
                });
    }

    private void initView() {
        ImageManager.showAccountAvatar(BtcAccountAssetsActivity.this, ivAccountAvatar, account);
        ImageManager.showAccountBackground(BtcAccountAssetsActivity.this, ivAccountBg, account);
        tvAccountName.setText(account.getName());

        mLayoutAccountInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountDetailActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            startActivity(intent);
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange() / 2) {
                collapsingToolbarLayout.setTitle(account.getName());
            } else {
                collapsingToolbarLayout.setTitle("");
            }
        });

        mLayoutReceiveBtc.setOnClickListener(v -> {
            mLayoutSyncStatus.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(mLayoutSyncStatus, "translationY", mLayoutSyncStatus.getHeight()).start();
        });

        mLayoutSendBtc.setOnClickListener(v -> {
            ObjectAnimator.ofFloat(mLayoutSyncStatus, "translationY", -mLayoutSyncStatus.getHeight()).start();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_qrcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_qrcode) {
            if (account != null) {
                Intent intent = new Intent(BtcAccountAssetsActivity.this, AddressQrcodeActivity.class);
                intent.putExtra(IntentParam.PARAM_ACCOUNT_INFO, account);
                startActivity(intent);
            }
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initAssets() {
        for (CryptoCurrency cryptoCurrency : cryptoCurrencies) {
            if (cryptoCurrency.getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                double tokenPrice = cryptoCurrency.getPriceCny();
                if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                    tokenPrice = cryptoCurrency.getPriceUsd();
                }
                tvBtcPrice.setText(String.valueOf(new BigDecimal(tokenPrice).setScale(3, BigDecimal.ROUND_HALF_UP)));
                break;
            }
        }
        BigDecimal totalAssets = BigDecimal.ZERO;
        long balance = 0;
        if ( kit != null && kit.wallet() != null) {
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(kit.wallet().currentReceiveAddress().toBase58()));
            balance = kit.wallet().getBalance().value;
            if (balance > 0) {
                for (CryptoCurrency currency : cryptoCurrencies) {
                    if (currency.getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                        double tokenPrice = currency.getPriceCny();
                        if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                            tokenPrice = currency.getPriceUsd();
                        }
                        BigDecimal tokenValue = new BigDecimal(tokenPrice).multiply(CommonUtil.convertBTCFromSatoshi(new BigInteger(String.valueOf(balance))));
                        totalAssets = totalAssets.add(tokenValue);
                        break;
                    }
                }
            }
            setBtcTransactionInfo();
        } else {
            BtcAccountManager.getInstance().initExistsWalletAppKit(account);
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
        }
        tvAccountBalance.setText(String.valueOf(CommonUtil.convertUnit(BrahmaConst.BITCOIN, new BigInteger(String.valueOf(balance)))));
        tvTotalAssets.setText(String.valueOf(totalAssets.setScale(2, BigDecimal.ROUND_HALF_UP)));
    }

    private void setBtcTransactionInfo() {
        if (kit != null && kit.wallet() != null) {
            Wallet wallet = kit.wallet();
            Map<Sha256Hash, Transaction> unspent = wallet.getTransactionPool(WalletTransaction.Pool.UNSPENT);
            unspent.size();
            Collection<Transaction> unspentTxns;
            unspentTxns = new TreeSet<Transaction>(Transaction.SORT_TX_BY_UPDATE_TIME);
            unspentTxns.addAll(unspent.values());

            Map<Sha256Hash, Transaction> spent = wallet.getTransactionPool(WalletTransaction.Pool.SPENT);
            spent.size();
            Collection<Transaction> spentTxns;
            spentTxns = new TreeSet<Transaction>(Transaction.SORT_TX_BY_HEIGHT);
            spentTxns.addAll(unspent.values());

            Map<Sha256Hash, Transaction> pending = wallet.getTransactionPool(WalletTransaction.Pool.PENDING);
            pending.size();
            Collection<Transaction> pendingTxns;
            pendingTxns = new TreeSet<Transaction>(Transaction.SORT_TX_BY_HEIGHT);
            pendingTxns.addAll(unspent.values());

            Map<Sha256Hash, Transaction> dead = wallet.getTransactionPool(WalletTransaction.Pool.DEAD);
            dead.size();
            Collection<Transaction> deadTxns;
            deadTxns = new TreeSet<Transaction>(Transaction.SORT_TX_BY_UPDATE_TIME);
            deadTxns.addAll(unspent.values());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(EventTypeDef.ACCOUNT_ASSETS_TRANSFER)
            }
    )
    public void refreshAssets(String status) {
        BLog.i(tag(), "transfer success");
        mViewModel.getAccounts().observe(this, accountEntities -> {
            BLog.i(tag(), "get all accounts for get total account assets");
        });
        customProgressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, getString(R.string.sync));
        customProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        customProgressDialog.setCancelable(false);
        mViewModel.getAssets().observe(this, (List<AccountAssets> accountAssets) -> {
            customProgressDialog.show();
            if (accountAssets != null) {
                BLog.i(tag(), "the assets length is: " + accountAssets.size());
                customProgressDialog.cancel();
                initAssets();

                RxBus.get().post(EventTypeDef.ACCOUNT_ASSETS_CHANGE, "succ");
            }
        });
    }
}
