package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.bitcoinj.kits.WalletAppKit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.BitcoinDownloadProgress;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

public class AccountsActivity extends BaseActivity {
    @Override
    protected String tag() {
        return AccountsActivity.class.getName();
    }

    public static final int REQ_IMPORT_ACCOUNT = 20;

    // UI references.
    @BindView(R.id.btc_accounts_recycler)
    RecyclerView recyclerViewBtcAccounts;
    @BindView(R.id.eth_accounts_recycler)
    RecyclerView recyclerViewEthAccounts;

    private AccountViewModel mViewModel;
    private List<AccountEntity> btcAccounts = new ArrayList<>();
    private List<AccountEntity> ethAccounts = new ArrayList<>();
    private List<AccountAssets> accountAssetsList = new ArrayList<>();
    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private BitcoinDownloadProgress bitcoinDownloadProgress;
    private Observable<BitcoinDownloadProgress> btcSyncStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        ButterKnife.bind(this);
        showNavBackBtn();

        LinearLayoutManager btcLayoutManager = new LinearLayoutManager(this);
        recyclerViewBtcAccounts.setLayoutManager(btcLayoutManager);
        recyclerViewBtcAccounts.setAdapter(new BtcAccountRecyclerAdapter());
        // Solve the sliding lag problem
        recyclerViewBtcAccounts.setHasFixedSize(true);
        recyclerViewBtcAccounts.setNestedScrollingEnabled(false);

        LinearLayoutManager ethLayoutManager = new LinearLayoutManager(this);
        recyclerViewEthAccounts.setLayoutManager(ethLayoutManager);
        recyclerViewEthAccounts.setAdapter(new EthAccountRecyclerAdapter());
        // Solve the sliding lag problem
        recyclerViewEthAccounts.setHasFixedSize(true);
        recyclerViewEthAccounts.setNestedScrollingEnabled(false);

        btcSyncStatus = RxEventBus.get().register(EventTypeDef.BTC_ACCOUNT_SYNC, BitcoinDownloadProgress.class);
        btcSyncStatus.onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BitcoinDownloadProgress>() {
                    @Override
                    public void onNext(BitcoinDownloadProgress progress) {
                        bitcoinDownloadProgress = progress;
                        if ((int)progress.getProgressPercentage() >= 100 ) {
                            bitcoinDownloadProgress.setDownloaded(true);
                        }
                        recyclerViewBtcAccounts.getAdapter().notifyDataSetChanged();
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

        initData();
    }

    private void initData() {
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            btcAccounts = new ArrayList<>();
            ethAccounts = new ArrayList<>();
            if (accountEntities != null && accountEntities.size() > 0) {
                for (AccountEntity accountEntity : accountEntities) {
                    if (accountEntity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                        btcAccounts.add(accountEntity);
                    } else {
                        ethAccounts.add(accountEntity);
                    }
                }
                recyclerViewBtcAccounts.getAdapter().notifyDataSetChanged();
                recyclerViewEthAccounts.getAdapter().notifyDataSetChanged();
            }
        });
        accountAssetsList = MainService.getInstance().getAccountAssetsList();
        cryptoCurrencies = MainService.getInstance().getCryptoCurrencies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_create_account) {
            Intent intent = new Intent(this, CreateEthAccountActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_import_account) {
            Intent intent = new Intent(this, ImportEthereumAccountActivity.class);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_IMPORT_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                //progressDialog.show();
                BLog.i(tag(), "import account success");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxEventBus.get().unregister(EventTypeDef.BTC_ACCOUNT_SYNC, btcSyncStatus);
    }

    /**
     * list item account
     */
    private class BtcAccountRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_account_btc, parent, false);
            rootView.setOnClickListener(v -> {
                int position = recyclerViewBtcAccounts.getChildAdapterPosition(v);
                AccountEntity account = btcAccounts.get(position);
                Intent intent = new Intent(AccountsActivity.this, BtcAccountAssetsActivity.class);
                intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
                startActivity(intent);
            });
            return new BtcAccountRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BtcAccountRecyclerAdapter.ItemViewHolder) {
                BtcAccountRecyclerAdapter.ItemViewHolder itemViewHolder = (BtcAccountRecyclerAdapter.ItemViewHolder) holder;
                AccountEntity accountEntity = btcAccounts.get(position);
                setData(itemViewHolder, accountEntity);
            }
        }

        /**
         * set account view
         */
        private void setData(BtcAccountRecyclerAdapter.ItemViewHolder holder, final AccountEntity account) {
            if (account == null) {
                return ;
            }
            ImageManager.showAccountAvatar(AccountsActivity.this, holder.ivAccountAvatar, account);
            ImageManager.showAccountBackground(AccountsActivity.this, holder.ivAccountBg, account);
            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_CNY)) {
                Glide.with(AccountsActivity.this)
                        .load(R.drawable.currency_cny_white)
                        .into(holder.ivCurrencyUnit);
            } else {
                Glide.with(AccountsActivity.this)
                        .load(R.drawable.currency_usd_white)
                        .into(holder.ivCurrencyUnit);
            }
            holder.tvAccountName.setText(account.getName());
            // get current receive address and balance through walletAppKit
            BigDecimal totalAssets = BigDecimal.ZERO;
            long balance = 0;
            WalletAppKit kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
            if ( kit != null && kit.wallet() != null) {
                holder.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(kit.wallet().currentReceiveAddress().toBase58()));
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
            } else {
                BtcAccountManager.getInstance().initExistsWalletAppKit(account);
                holder.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
            }
            if (bitcoinDownloadProgress != null && !bitcoinDownloadProgress.isDownloaded()) {
                holder.ivSyncStatus.setVisibility(View.VISIBLE);
                Animation rotate = AnimationUtils.loadAnimation(AccountsActivity.this, R.anim.sync_rotate);
                if (rotate != null) {
                    holder.ivSyncStatus.startAnimation(rotate);
                }
            } else {
                holder.ivSyncStatus.setVisibility(View.GONE);
            }

            holder.tvAccountBalance.setText(String.valueOf(CommonUtil.convertUnit(BrahmaConst.BITCOIN, new BigInteger(String.valueOf(balance)))));
            holder.tvTotalAssets.setText(String.valueOf(totalAssets.setScale(2, BigDecimal.ROUND_HALF_UP)));
        }

        @Override
        public int getItemCount() {
            return btcAccounts.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivAccountBg;
            ImageView ivAccountAvatar;
            TextView tvAccountName;
            TextView tvAccountAddress;
            TextView tvAccountBalance;
            TextView tvTotalAssetsDesc;
            TextView tvTotalAssets;
            ImageView ivCurrencyUnit;
            ImageView ivSyncStatus;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivAccountBg = itemView.findViewById(R.id.iv_account_bg);
                ivAccountAvatar = itemView.findViewById(R.id.iv_account_avatar);
                tvAccountName = itemView.findViewById(R.id.tv_account_name);
                tvAccountAddress = itemView.findViewById(R.id.tv_account_address);
                tvTotalAssetsDesc = itemView.findViewById(R.id.tv_total_assets_desc);
                tvAccountBalance = itemView.findViewById(R.id.tv_account_balance);
                tvTotalAssets = itemView.findViewById(R.id.tv_total_assets);
                ivCurrencyUnit = itemView.findViewById(R.id.iv_currency_amount);
                ivSyncStatus = itemView.findViewById(R.id.iv_btc_sync_status);
            }
        }
    }

    /**
     * list item eth account
     */
    private class EthAccountRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_account_eth, parent, false);
            rootView.setOnClickListener(v -> {
                int position = recyclerViewEthAccounts.getChildAdapterPosition(v);
                AccountEntity account = ethAccounts.get(position);
                Intent intent = new Intent(AccountsActivity.this, EthAccountAssetsActivity.class);
                intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
                startActivity(intent);
            });
            return new EthAccountRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof EthAccountRecyclerAdapter.ItemViewHolder) {
                EthAccountRecyclerAdapter.ItemViewHolder itemViewHolder = (EthAccountRecyclerAdapter.ItemViewHolder) holder;
                AccountEntity accountEntity = ethAccounts.get(position);
                setData(itemViewHolder, accountEntity);
            }
        }

        /**
         * set account view
         */
        private void setData(EthAccountRecyclerAdapter.ItemViewHolder holder, final AccountEntity account) {
            if (account == null) {
                return ;
            }
            ImageManager.showAccountAvatar(AccountsActivity.this, holder.ivAccountAvatar, account);
            ImageManager.showAccountBackground(AccountsActivity.this, holder.ivAccountBg, account);

            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_CNY)) {
                Glide.with(AccountsActivity.this)
                        .load(R.drawable.currency_cny_white)
                        .into(holder.ivCurrencyUnit);
            } else {
                Glide.with(AccountsActivity.this)
                        .load(R.drawable.currency_usd_white)
                        .into(holder.ivCurrencyUnit);
            }

            holder.tvAccountName.setText(account.getName());
            holder.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
            BigDecimal totalAssets = BigDecimal.ZERO;
            for (AccountAssets assets : accountAssetsList) {
                if (assets.getAccountEntity().getAddress().equals(account.getAddress()) &&
                        assets.getBalance().compareTo(BigInteger.ZERO) > 0) {
                    for (CryptoCurrency currency : cryptoCurrencies) {
                        if (CommonUtil.cryptoCurrencyCompareToken(currency, assets.getTokenEntity())) {
                            double tokenPrice = currency.getPriceCny();
                            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                                tokenPrice = currency.getPriceUsd();
                            }
                            BigDecimal tokenValue = new BigDecimal(tokenPrice).multiply(CommonUtil.getAccountFromWei(assets.getBalance()));
                            totalAssets = totalAssets.add(tokenValue);
                        }
                    }
                }
            }
            holder.tvTotalAssets.setText(String.valueOf(totalAssets.setScale(2, BigDecimal.ROUND_HALF_UP)));
        }

        @Override
        public int getItemCount() {
            return ethAccounts.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivAccountBg;
            ImageView ivAccountAvatar;
            TextView tvAccountName;
            TextView tvAccountAddress;
            TextView tvTotalAssetsDesc;
            TextView tvTotalAssets;
            ImageView ivCurrencyUnit;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivAccountBg = itemView.findViewById(R.id.iv_account_bg);
                ivAccountAvatar = itemView.findViewById(R.id.iv_account_avatar);
                tvAccountName = itemView.findViewById(R.id.tv_account_name);
                tvAccountAddress = itemView.findViewById(R.id.tv_account_address);
                tvTotalAssetsDesc = itemView.findViewById(R.id.tv_total_assets_desc);
                tvTotalAssets = itemView.findViewById(R.id.tv_total_assets);
                ivCurrencyUnit = itemView.findViewById(R.id.iv_currency_amount);
            }
        }
    }
}
