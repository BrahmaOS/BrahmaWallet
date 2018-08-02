package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.transaction.EthTransactionsActivity;
import io.brahmaos.wallet.brahmawallet.ui.transaction.TransactionsActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.TransferActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;

public class AccountAssetsActivity extends BaseActivity {

    @Override
    protected String tag() {
        return AccountAssetsActivity.class.getName();
    }

    public static final int REQ_CODE_TRANSFER = 10;

    // UI references.
    @BindView(R.id.layout_account_info)
    RelativeLayout mLayoutAccountInfo;
    @BindView(R.id.assets_recycler)
    RecyclerView recyclerViewAssets;
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.tv_total_assets)
    TextView tvTotalAssets;
    @BindView(R.id.tv_currency_unit)
    TextView tvCurrencyUnit;

    private int accountId;
    private AccountEntity account;
    private AccountViewModel mViewModel;
    private List<TokenEntity> tokenEntities = new ArrayList<>();
    private List<AccountAssets> accountAssetsList = new ArrayList<>();
    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_assets);
        ButterKnife.bind(this);
        showNavBackBtn();
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        String currencyUnit = BrahmaConfig.getInstance().getCurrencyUnit();
        if (currencyUnit != null) {
            tvCurrencyUnit.setText(currencyUnit);
        } else {
            tvCurrencyUnit.setText(BrahmaConst.UNIT_PRICE_CNY);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAssets.setLayoutManager(layoutManager);
        recyclerViewAssets.setAdapter(new AssetsRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewAssets.setHasFixedSize(true);
        recyclerViewAssets.setNestedScrollingEnabled(false);
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        cryptoCurrencies = MainService.getInstance().getCryptoCurrencies();
        accountAssetsList = MainService.getInstance().getAccountAssetsList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getAccountById(accountId)
                .observe(this, (AccountEntity accountEntity) -> {
                    if (accountEntity != null) {
                        account = accountEntity;
                        initView();
                        initAssets();
                        mViewModel.getTokens().observe(this, entities -> {
                            if (entities != null) {
                                tokenEntities = entities;
                                recyclerViewAssets.getAdapter().notifyDataSetChanged();
                            }
                        });
                    } else {
                        finish();
                    }
                });
    }

    private void initView() {
        ImageManager.showAccountAvatar(AccountAssetsActivity.this, ivAccountAvatar, account);
        tvAccountName.setText(account.getName());
        tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));

        mLayoutAccountInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountDetailActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
            startActivity(intent);
        });
    }

    private void initAssets() {
        if (accountAssetsList == null) {
            accountAssetsList = new ArrayList<>();
        }
        BigDecimal totalValue = BigDecimal.ZERO;
        for (AccountAssets assets : accountAssetsList) {
            if (assets.getAccountEntity().getAddress().equals(account.getAddress())) {
                if (assets.getBalance().compareTo(BigInteger.ZERO) > 0) {
                    for (CryptoCurrency cryptoCurrency : cryptoCurrencies) {
                        if (CommonUtil.cryptoCurrencyCompareToken(cryptoCurrency, assets.getTokenEntity())) {
                            double tokenPrice = cryptoCurrency.getPriceCny();
                            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                                tokenPrice = cryptoCurrency.getPriceUsd();
                            }
                            BigDecimal value = new BigDecimal(tokenPrice)
                                    .multiply(CommonUtil.getAccountFromWei(assets.getBalance()));
                            totalValue = totalValue.add(value);
                            break;
                        }
                    }
                }
            }
        }
        tvTotalAssets.setText(String.valueOf(totalValue.setScale(2, BigDecimal.ROUND_HALF_UP)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_CODE_TRANSFER) {
            if (resultCode == RESULT_OK) {
                BLog.i(tag(), "transfer success");
                mViewModel.getAccounts().observe(this, accountEntities -> {
                    BLog.i(tag(), "get all accounts for get total account assets");
                });
                customProgressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, getString(R.string.sync));
                customProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                customProgressDialog.setCancelable(false);
                mViewModel.getAssets().observe(this, (List<AccountAssets> accountAssets) -> {
                    customProgressDialog.show();
                    BLog.i(tag(), "the assets length is: " + accountAssets);
                    if (accountAssets != null) {
                        BLog.i(tag(), "the assets length is: " + accountAssets.size());
                        customProgressDialog.cancel();
                        accountAssetsList = accountAssets;
                        initAssets();
                        recyclerViewAssets.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * list item currency
     */
    private class AssetsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_assets, parent, false);
            rootView.setOnClickListener(v -> {

            });
            return new AssetsRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AssetsRecyclerAdapter.ItemViewHolder) {
                AssetsRecyclerAdapter.ItemViewHolder itemViewHolder = (AssetsRecyclerAdapter.ItemViewHolder) holder;
                TokenEntity tokenEntity = tokenEntities.get(position);
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
                if (tokenEntity.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
                    Intent intent = new Intent(AccountAssetsActivity.this, EthTransactionsActivity.class);
                    intent.putExtra(IntentParam.PARAM_ACCOUNT_INFO, account);
                    intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                    startActivityForResult(intent, REQ_CODE_TRANSFER);
                } else {
                    Intent intent = new Intent(AccountAssetsActivity.this, TransactionsActivity.class);
                    intent.putExtra(IntentParam.PARAM_ACCOUNT_INFO, account);
                    intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                    startActivityForResult(intent, REQ_CODE_TRANSFER);
                }
            });

            holder.tvTokenName.setText(tokenEntity.getShortName());
            holder.tvTokenFullName.setText(tokenEntity.getName());
            ImageManager.showTokenIcon(AccountAssetsActivity.this, holder.ivTokenIcon, tokenEntity.getAvatar(),
                    tokenEntity.getName(), tokenEntity.getAddress());
            BigInteger tokenCount = BigInteger.ZERO;
            for (AccountAssets accountAssets : accountAssetsList) {
                if (accountAssets.getTokenEntity().getAddress().equals(tokenEntity.getAddress()) &&
                        accountAssets.getAccountEntity().getAddress().equals(account.getAddress())) {
                    tokenCount = tokenCount.add(accountAssets.getBalance());
                }
            }
            holder.tvTokenAccount.setText(String.valueOf(CommonUtil.getAccountFromWei(tokenCount)));
            BigDecimal tokenValue = BigDecimal.ZERO;
            for (CryptoCurrency cryptoCurrency : cryptoCurrencies) {
                if (CommonUtil.cryptoCurrencyCompareToken(cryptoCurrency, tokenEntity)) {
                    double tokenPrice = cryptoCurrency.getPriceUsd();
                    if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_CNY)) {
                        tokenPrice = cryptoCurrency.getPriceCny();
                        Glide.with(AccountAssetsActivity.this)
                                .load(R.drawable.currency_cny)
                                .into(holder.ivTokenPrice);
                        Glide.with(AccountAssetsActivity.this)
                                .load(R.drawable.currency_cny)
                                .into(holder.ivTokenAssets);
                    } else {
                        Glide.with(AccountAssetsActivity.this)
                                .load(R.drawable.currency_usd)
                                .into(holder.ivTokenPrice);
                        Glide.with(AccountAssetsActivity.this)
                                .load(R.drawable.currency_usd)
                                .into(holder.ivTokenAssets);
                    }
                    tokenValue = CommonUtil.getAccountFromWei(tokenCount).multiply(new BigDecimal(tokenPrice));
                    holder.tvTokenPrice.setText(String.valueOf(new BigDecimal(tokenPrice).setScale(3, BigDecimal.ROUND_HALF_UP)));
                    break;
                }
            }
            holder.tvTokenAssetsCount.setText(String.valueOf(tokenValue.setScale(2, BigDecimal.ROUND_HALF_UP)));
        }

        @Override
        public int getItemCount() {
            return tokenEntities.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            LinearLayout layoutAssets;
            ImageView ivTokenIcon;
            TextView tvTokenName;
            TextView tvTokenFullName;
            TextView tvTokenPrice;
            TextView tvTokenAccount;
            TextView tvTokenAssetsCount;
            ImageView ivTokenPrice;
            ImageView ivTokenAssets;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutAssets = itemView.findViewById(R.id.layout_assets);
                ivTokenIcon = itemView.findViewById(R.id.iv_token_icon);
                tvTokenName = itemView.findViewById(R.id.tv_token_name);
                tvTokenAccount = itemView.findViewById(R.id.tv_token_count);
                tvTokenAssetsCount = itemView.findViewById(R.id.tv_token_assets_count);
                tvTokenFullName = itemView.findViewById(R.id.tv_token_full_name);
                tvTokenPrice = itemView.findViewById(R.id.tv_token_price);
                ivTokenPrice = itemView.findViewById(R.id.iv_currency_unit);
                ivTokenAssets = itemView.findViewById(R.id.iv_currency_amount);
            }
        }
    }

}
