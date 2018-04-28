package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.TransferActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;

public class AccountDetailActivity extends BaseActivity {

    @Override
    protected String tag() {
        return AccountDetailActivity.class.getName();
    }

    public static final int REQ_CODE_TRANSFER = 10;

    // UI references.
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
    @BindView(R.id.tv_copy_address)
    TextView tvCopyAddress;
    @BindView(R.id.layout_progress)
    FrameLayout layoutProgress;

    private AccountEntity account;
    private AccountViewModel mViewModel;
    private List<TokenEntity> tokenEntities = new ArrayList<>();
    private List<AccountAssets> accountAssetsList = new ArrayList<>();
    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        ButterKnife.bind(this);
        showNavBackBtn();
        account = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);
        if (account == null) {
            finish();
        }
        initView();

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        cryptoCurrencies = MainService.getInstance().getCryptoCurrencies();

        mViewModel.getTokens().observe(this, entities -> {
            if (entities != null) {
                tokenEntities = entities;
                recyclerViewAssets.getAdapter().notifyDataSetChanged();
            }
        });
        accountAssetsList = MainService.getInstance().getAccountAssetsList();

        initAssets();
    }

    private void initView() {
        ImageManager.showAccountAvatar(AccountDetailActivity.this, ivAccountAvatar, account);
        tvAccountName.setText(account.getName());
        tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAssets.setLayoutManager(layoutManager);
        recyclerViewAssets.setAdapter(new AssetsRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewAssets.setHasFixedSize(true);
        recyclerViewAssets.setNestedScrollingEnabled(false);

        tvCopyAddress.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(account.getAddress());
            showLongToast(R.string.tip_success_copy_address);
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
                        if (cryptoCurrency.getName().toLowerCase()
                                .equals(assets.getTokenEntity().getName().toLowerCase())) {
                            BigDecimal value = new BigDecimal(cryptoCurrency.getPriceCny())
                                    .multiply(new BigDecimal(CommonUtil.getAccountFromWei(assets.getBalance())));
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
                    BLog.i(tag(), "get account success");
                });

                mViewModel.getAssets().observe(this, (List<AccountAssets> accountAssets) -> {
                    layoutProgress.setVisibility(View.VISIBLE);
                    BLog.i(tag(), "get transfer after account assets");
                    if (accountAssets != null) {
                        layoutProgress.setVisibility(View.GONE);
                        BLog.i(tag(), "get transfer after account assets, the assets is not null");
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
                Intent intent = new Intent(AccountDetailActivity.this, TransferActivity.class);
                intent.putExtra(IntentParam.PARAM_ACCOUNT_INFO, account);
                intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                startActivityForResult(intent, REQ_CODE_TRANSFER);
            });

            holder.tvTokenName.setText(tokenEntity.getShortName());
            ImageManager.showTokenIcon(AccountDetailActivity.this, holder.ivTokenIcon, tokenEntity.getAddress());
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
                if (cryptoCurrency.getName().toLowerCase().equals(tokenEntity.getName().toLowerCase())) {
                    tokenValue = new BigDecimal(CommonUtil.getAccountFromWei(tokenCount)).multiply(new BigDecimal(cryptoCurrency.getPriceCny()));
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
            TextView tvTokenAccount;
            TextView tvTokenAssetsCount;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutAssets = itemView.findViewById(R.id.layout_assets);
                ivTokenIcon = itemView.findViewById(R.id.iv_token_icon);
                tvTokenName = itemView.findViewById(R.id.tv_token_name);
                tvTokenAccount = itemView.findViewById(R.id.tv_token_count);
                tvTokenAssetsCount = itemView.findViewById(R.id.tv_token_assets_count);
            }
        }
    }

}
