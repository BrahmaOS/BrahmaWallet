package io.brahmaos.wallet.brahmawallet.ui.home;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.BitcoinDownloadProgress;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateBtcAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.RestoreAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.token.TokensActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.BtcTransferActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.EthTransferActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.InstantExchangeActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class WalletFragment extends BaseFragment {
    @Override
    protected String tag() {
        return WalletFragment.class.getName();
    }

    public static int REQ_CODE_TRANSFER = 10;

    private LinearLayout layoutHeader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvApproEqual;
    private TextView tvTestNetwork;
    private TextView tvCurrencyUnit;
    private TextView tvTotalAssets;
    private ImageView ivAssetsVisible;
    private RecyclerView recyclerViewAssets;

    private AccountViewModel mViewModel;
    private List<AccountEntity> cacheAccounts = new ArrayList<>();
    private List<TokenEntity> cacheTokens = new ArrayList<>();
    private List<AccountAssets> cacheAssets = new ArrayList<>();
    private List<CryptoCurrency> cacheCryptoCurrencies = new ArrayList<>();

    private BitcoinDownloadProgress bitcoinDownloadProgress;
    private Observable<BitcoinDownloadProgress> btcSyncStatus;
    private Observable<Boolean> btcAppkitSetup;

    /**
     * instance
     *
     * @param layoutResId  layout resource，e.g. R.layout.fragment_home
     * @return  return fragment
     */
    public static WalletFragment newInstance(int layoutResId, int titleResId) {
        WalletFragment fragment = new WalletFragment();
        fragment.setArguments(newArguments(layoutResId, titleResId));
        return fragment;
    }

    @Override
    protected boolean initView() {
        RxBus.get().register(this);

        layoutHeader = parentView.findViewById(R.id.layout_header);
        swipeRefreshLayout = parentView.findViewById(R.id.swipe_refresh_layout);
        tvApproEqual = parentView.findViewById(R.id.tv_appro_equal);
        tvTestNetwork = parentView.findViewById(R.id.tv_test_network);
        tvCurrencyUnit = parentView.findViewById(R.id.tv_money_unit);
        tvTotalAssets = parentView.findViewById(R.id.tv_total_assets);
        ivAssetsVisible = parentView.findViewById(R.id.iv_assets_visibility);
        recyclerViewAssets = parentView.findViewById(R.id.assets_recycler);

        DisplayMetrics display = this.getResources().getDisplayMetrics();

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.height_toolbar);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutHeader.getLayoutParams();
        params.width = display.widthPixels;
        params.height = ((int) (display.heightPixels * BrahmaConst.MAIN_PAGE_HEADER_RATIO) - statusBarHeight - toolbarHeight);
        layoutHeader.setLayoutParams(params);

        swipeRefreshLayout.setColorSchemeResources(R.color.master);
        swipeRefreshLayout.setRefreshing(true);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // get the latest assets
            mViewModel.getTotalAssets();
            // get Currencies
            getCryptoCurrents();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewAssets.setLayoutManager(layoutManager);
        recyclerViewAssets.setAdapter(new AssetsRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewAssets.setHasFixedSize(true);
        recyclerViewAssets.setNestedScrollingEnabled(false);

        tvCurrencyUnit.setText(BrahmaConfig.getInstance().getCurrencyUnit());

        TextView tvAddAssets = parentView.findViewById(R.id.tv_add_assets);
        tvAddAssets.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TokensActivity.class);
            startActivity(intent);
        });

        TextView tvInstantExchange = parentView.findViewById(R.id.tv_instant_exchange);
        tvInstantExchange.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InstantExchangeActivity.class);
            startActivity(intent);
        });

        ivAssetsVisible.setOnClickListener(v -> {
            if (BrahmaConfig.getInstance().isAssetsVisible()) {
                BrahmaConfig.getInstance().setAssetsVisible(false);
            } else {
                BrahmaConfig.getInstance().setAssetsVisible(true);
            }
            showAssetsCurrency();
        });

        changeNetwork();

        // used to receive btc blocks sync progress
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
                        if (bitcoinDownloadProgress.isDownloaded()) {
                            mViewModel.getBtcAssets();
                        } else {
                            recyclerViewAssets.getAdapter().notifyDataSetChanged();
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

        btcAppkitSetup = RxEventBus.get().register(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, Boolean.class);
        btcAppkitSetup.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onNext(Boolean flag) {
                        if (flag) {
                            // get the latest assets
                            mViewModel.getTotalAssets();
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
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        initData();

        return true;
    }

    private void changeNetwork() {
        String networkUrl = BrahmaConfig.getInstance().getNetworkUrl();
        String networkName = CommonUtil.generateNetworkName(networkUrl);
        if (networkName.equals("Mainnet") || networkName.length() < 1) {
            tvTestNetwork.setVisibility(View.GONE);
        } else {
            tvTestNetwork.setVisibility(View.VISIBLE);
            tvTestNetwork.setText(networkName);
        }
    }

    private void initData() {
        mViewModel.getAllTokensCount().observe(this, count -> {
            if (count == null || count <= 0) {
                MainService.getInstance().getAccountAssetsList();
            }
        });
        mViewModel.getAccounts().observe(this, accountEntities -> {
            cacheAccounts = accountEntities;
        });

        mViewModel.getTokens().observe(this, tokenEntities -> {
            if (tokenEntities != null) {
                swipeRefreshLayout.setRefreshing(true);
                cacheTokens = tokenEntities;
                recyclerViewAssets.getAdapter().notifyDataSetChanged();
                // fetch crypto currents
                getCryptoCurrents();
            }
        });

        mViewModel.getAssets().observe(this, (List<AccountAssets> accountAssets) -> {
            BLog.i(tag(), "get home account assets");
            if (accountAssets != null) {
                cacheAssets = accountAssets;
                showAssetsCurrency();
            }
        });
    }

    private void getCryptoCurrents() {
        String symbols;
        if (cacheTokens != null && cacheTokens.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (TokenEntity token : cacheTokens) {
                stringBuilder.append(token.getCode()).append(",");
            }
            symbols = stringBuilder.toString();
        } else {
            symbols = String.format("%d,%d,%d", BrahmaConst.COIN_CODE_BRM,
                    BrahmaConst.COIN_CODE_ETH, BrahmaConst.COIN_CODE_BTC);
        }
        MainService.getInstance().fetchCurrenciesFromNet(symbols)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CryptoCurrency>>() {

                    @Override
                    public void onCompleted() {
                        cacheCryptoCurrencies = MainService.getInstance().getCryptoCurrencies();
                        showAssetsCurrency();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(List<CryptoCurrency> apr) {

                    }
                });
    }

    /**
     * Display the number of tokens and the corresponding legal currency value
     */
    private void showAssetsCurrency() {
        int ethAccountCount = 0;
        int btcAccountCount = 0;
        for (AccountEntity account : cacheAccounts) {
            if (account.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                btcAccountCount++;
            } else if (account.getType() == BrahmaConst.ETH_ACCOUNT_TYPE) {
                ethAccountCount++;
            }
        }
        int ethTokenCount = cacheTokens.size() - 1;
        int totalCount = ethAccountCount * ethTokenCount + btcAccountCount;
        if (cacheAssets.size() == totalCount) {
            recyclerViewAssets.getAdapter().notifyDataSetChanged();

            BigDecimal totalValue = BigDecimal.ZERO;
            for (AccountAssets accountAssets : cacheAssets) {
                if (accountAssets.getBalance().compareTo(BigInteger.ZERO) > 0 && cacheCryptoCurrencies != null) {
                    for (CryptoCurrency cryptoCurrency : cacheCryptoCurrencies) {
                        if (CommonUtil.cryptoCurrencyCompareToken(cryptoCurrency, accountAssets.getTokenEntity())) {
                            double tokenPrice = cryptoCurrency.getPriceCny();
                            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                                tokenPrice = cryptoCurrency.getPriceUsd();
                            }
                            BigDecimal value = new BigDecimal(tokenPrice)
                                    .multiply(CommonUtil.convertUnit(accountAssets.getTokenEntity().getName(),
                                            accountAssets.getBalance()));
                            totalValue = totalValue.add(value);
                            break;
                        }
                    }
                }
            }
            swipeRefreshLayout.setRefreshing(false);
            if (BrahmaConfig.getInstance().isAssetsVisible()) {
                tvTotalAssets.setText(String.valueOf(totalValue.setScale(2, BigDecimal.ROUND_HALF_UP)));
                tvApproEqual.setText(R.string.asymptotic);
                ivAssetsVisible.setImageResource(R.drawable.ic_open_eye);
            } else {
                tvTotalAssets.setText("******");
                tvApproEqual.setText("");
                ivAssetsVisible.setImageResource(R.drawable.ic_close_eye);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_CODE_TRANSFER) {
            if (resultCode == RESULT_OK) {
                BLog.i(tag(), "transfer success");
                // get the latest assets
                mViewModel.getTotalAssets();
                swipeRefreshLayout.setRefreshing(true);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            RxBus.get().unregister(this);
        } catch (Exception e) {
            BLog.e(tag(), e.getMessage());
        }
        RxEventBus.get().unregister(EventTypeDef.BTC_ACCOUNT_SYNC, btcSyncStatus);
        RxEventBus.get().unregister(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, btcAppkitSetup);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(EventTypeDef.ACCOUNT_ASSETS_CHANGE)
            }
    )
    public void refreshAssets(String status) {
        BLog.d(tag(), "account assetst change");
        cacheAssets = MainService.getInstance().getAccountAssetsList();
        BLog.d(tag(), cacheAssets.toString());
        showAssetsCurrency();
    }

    /**
     * list item account
     */
    private class AssetsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_assets, parent, false);
            return new AssetsRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AssetsRecyclerAdapter.ItemViewHolder) {
                AssetsRecyclerAdapter.ItemViewHolder itemViewHolder = (AssetsRecyclerAdapter.ItemViewHolder) holder;
                TokenEntity tokenEntity = cacheTokens.get(position);
                setAssetsData(itemViewHolder, tokenEntity);
            }
        }

        /**
         * set assets view
         */
        private void setAssetsData(AssetsRecyclerAdapter.ItemViewHolder holder, final TokenEntity tokenEntity) {
            if (tokenEntity == null) {
                return;
            }
            holder.layoutAssets.setOnClickListener(v -> {
                if (tokenEntity.getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                    List<AccountEntity> btcAccounts = new ArrayList<>();
                    for (AccountEntity accountEntity : cacheAccounts) {
                        if (accountEntity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                            btcAccounts.add(accountEntity);
                        }
                    }
                    if (btcAccounts.size() > 0) {
                        Intent intent = new Intent(getActivity(), BtcTransferActivity.class);
                        intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                        startActivityForResult(intent, REQ_CODE_TRANSFER);
                    } else {
                        AlertDialog passwordDialog = new AlertDialog.Builder(getContext())
                                .setMessage(R.string.tip_no_btc_account)
                                .setCancelable(true)
                                .setPositiveButton(R.string.create, (dialog, which) -> {
                                    dialog.cancel();
                                    Intent intent = new Intent(getActivity(), CreateBtcAccountActivity.class);
                                    startActivity(intent);
                                })
                                .create();
                        passwordDialog.show();
                    }

                } else {
                    Intent intent = new Intent(getActivity(), EthTransferActivity.class);
                    intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                    startActivityForResult(intent, REQ_CODE_TRANSFER);
                }
            });
            holder.tvTokenName.setText(tokenEntity.getShortName());
            holder.tvTokenFullName.setText(tokenEntity.getName());
            holder.tvTokenPrice.setText("0");
            ImageManager.showTokenIcon(getActivity(), holder.ivTokenIcon,
                    tokenEntity.getName(), tokenEntity.getAvatar());
            BigInteger tokenCount = BigInteger.ZERO;

            for (AccountAssets accountAssets : cacheAssets) {
                if (accountAssets.getTokenEntity().getAddress().toLowerCase().equals(tokenEntity.getAddress().toLowerCase())) {
                    tokenCount = tokenCount.add(accountAssets.getBalance());
                }
            }
            BigDecimal tokenValue = BigDecimal.ZERO;
            if (cacheCryptoCurrencies != null && cacheCryptoCurrencies.size() > 0) {
                for (CryptoCurrency cryptoCurrency : cacheCryptoCurrencies) {
                    if (CommonUtil.cryptoCurrencyCompareToken(cryptoCurrency, tokenEntity)) {
                        double tokenPrice = cryptoCurrency.getPriceUsd();
                        if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_CNY)) {
                            tokenPrice = cryptoCurrency.getPriceCny();
                            Glide.with(getContext())
                                    .load(R.drawable.currency_cny)
                                    .into(holder.ivTokenPrice);
                            Glide.with(getContext())
                                    .load(R.drawable.currency_cny)
                                    .into(holder.ivTokenAssets);
                        } else {
                            Glide.with(getContext())
                                    .load(R.drawable.currency_usd)
                                    .into(holder.ivTokenPrice);
                            Glide.with(getContext())
                                    .load(R.drawable.currency_usd)
                                    .into(holder.ivTokenAssets);
                        }
                        tokenValue = CommonUtil.convertUnit(tokenEntity.getName(), tokenCount).multiply(new BigDecimal(tokenPrice));
                        holder.tvTokenPrice.setText(String.valueOf(new BigDecimal(tokenPrice).setScale(3, BigDecimal.ROUND_HALF_UP)));
                        break;
                    }
                }
            }
            if (BrahmaConfig.getInstance().isAssetsVisible()) {
                holder.tvTokenApproEqual.setText(R.string.asymptotic);
                holder.tvTokenAccount.setText(String.valueOf(CommonUtil.convertUnit(tokenEntity.getName(), tokenCount)));
                holder.tvTokenAssetsCount.setText(String.valueOf(tokenValue.setScale(2, BigDecimal.ROUND_HALF_UP)));
            } else {
                holder.tvTokenApproEqual.setText("");
                holder.tvTokenAccount.setText("****");
                holder.tvTokenAssetsCount.setText("********");
            }
            if (tokenEntity.getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                if (bitcoinDownloadProgress != null) {
                    if (bitcoinDownloadProgress.isDownloaded()) {
                        holder.ivBtcSync.setVisibility(View.GONE);
                        holder.tvBtcSyncStatus.setVisibility(View.GONE);
                        holder.tvTokenAccount.setVisibility(View.VISIBLE);
                    } else {
                        holder.ivBtcSync.setVisibility(View.VISIBLE);
                        holder.tvBtcSyncStatus.setVisibility(View.VISIBLE);
                        holder.tvTokenAccount.setVisibility(View.GONE);
                        Animation rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.sync_rotate);
                        if (rotate != null) {
                            holder.ivBtcSync.startAnimation(rotate);
                        }
                        int progress = 1;
                        if ((int) bitcoinDownloadProgress.getProgressPercentage() > progress) {
                            progress = (int) bitcoinDownloadProgress.getProgressPercentage();
                        }
                        holder.tvBtcSyncStatus.setText(String.format(Locale.US, "%s %d%%",
                                getResources().getString(R.string.sync), progress));
                    }
                } else {
                    holder.ivBtcSync.setVisibility(View.GONE);
                    holder.tvBtcSyncStatus.setVisibility(View.GONE);
                    holder.tvTokenAccount.setVisibility(View.VISIBLE);
                }
            } else {
                holder.ivBtcSync.setVisibility(View.GONE);
                holder.tvBtcSyncStatus.setVisibility(View.GONE);
                holder.tvTokenAccount.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return cacheTokens.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            LinearLayout layoutAssets;
            ImageView ivTokenIcon;
            TextView tvTokenName;
            TextView tvTokenFullName;
            TextView tvTokenPrice;
            TextView tvTokenAccount;
            TextView tvTokenApproEqual;
            TextView tvTokenAssetsCount;
            ImageView ivTokenPrice;
            ImageView ivTokenAssets;
            TextView tvBtcSyncStatus;
            ImageView ivBtcSync;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutAssets = itemView.findViewById(R.id.layout_assets);
                ivTokenIcon = itemView.findViewById(R.id.iv_token_icon);
                tvTokenName = itemView.findViewById(R.id.tv_token_name);
                tvTokenAccount = itemView.findViewById(R.id.tv_token_count);
                tvTokenApproEqual = itemView.findViewById(R.id.tv_token_appro_equal);
                tvTokenAssetsCount = itemView.findViewById(R.id.tv_token_assets_count);
                tvTokenFullName = itemView.findViewById(R.id.tv_token_full_name);
                tvTokenPrice = itemView.findViewById(R.id.tv_token_price);
                ivTokenPrice = itemView.findViewById(R.id.iv_currency_unit);
                ivTokenAssets = itemView.findViewById(R.id.iv_currency_amount);
                ivBtcSync = itemView.findViewById(R.id.iv_btc_sync);
                tvBtcSyncStatus = itemView.findViewById(R.id.tv_btc_sync);
            }
        }
    }
}

