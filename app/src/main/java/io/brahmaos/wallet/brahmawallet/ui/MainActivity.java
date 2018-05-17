package io.brahmaos.wallet.brahmawallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountsActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.ImportAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.SettingsActivity;
import io.brahmaos.wallet.brahmawallet.ui.test.TestActivity;
import io.brahmaos.wallet.brahmawallet.ui.token.TokensActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.TransferActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    protected String tag() {
        return MainActivity.class.getName();
    }

    public static int REQ_CODE_TRANSFER = 10;

    @BindView(R.id.layout_new_account)
    ConstraintLayout createAccountLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_appro_equal)
    TextView tvApproEqual;
    @BindView(R.id.tv_test_network)
    TextView tvTestNetwork;
    @BindView(R.id.tv_total_assets)
    TextView tvTotalAssets;
    @BindView(R.id.iv_assets_visibility)
    ImageView ivAssetsVisible;
    @BindView(R.id.tv_assets_categories_num)
    TextView tvTokenCategories;
    @BindView(R.id.assets_recycler)
    RecyclerView recyclerViewAssets;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private AccountViewModel mViewModel;
    private List<AccountEntity> cacheAccounts = new ArrayList<>();
    private List<TokenEntity> cacheTokens = new ArrayList<>();
    private List<AccountAssets> cacheAssets = new ArrayList<>();
    private List<CryptoCurrency> cacheCryptoCurrencies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initView();
        initData();
    }

    private void initView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.master);
        swipeRefreshLayout.setRefreshing(true);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // get the latest assets
            mViewModel.getTotalAssets();
            // get Currencies
            if (cacheCryptoCurrencies == null || cacheCryptoCurrencies.size() == 0) {
                mViewModel.fetchCurrenciesFromNet();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAssets.setLayoutManager(layoutManager);
        recyclerViewAssets.setAdapter(new AssetsRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewAssets.setHasFixedSize(true);
        recyclerViewAssets.setNestedScrollingEnabled(false);

        ImageView ivChooseToken = findViewById(R.id.iv_choose_token);
        ivChooseToken.setOnClickListener(v -> {
            Intent intent = new Intent(this, TokensActivity.class);
            startActivity(intent);
        });

        Button createWalletBtn = findViewById(R.id.btn_create_account);
        createWalletBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });
        Button importAccountBtn = findViewById(R.id.btn_import_account);
        importAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImportAccountActivity.class);
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
        mViewModel.getAccounts().observe(this, accountEntities -> {
            cacheAccounts = accountEntities;
            checkContentShow();
        });

        mViewModel.getTokens().observe(this, tokenEntities -> {
            if (tokenEntities != null) {
                swipeRefreshLayout.setRefreshing(true);
                tvTokenCategories.setText(String.valueOf(tokenEntities.size()));
                cacheTokens = tokenEntities;
                recyclerViewAssets.getAdapter().notifyDataSetChanged();
            }
        });

        mViewModel.getAssets().observe(this, (List<AccountAssets> accountAssets) -> {
            BLog.i(tag(), "get home account assets");
            if (accountAssets != null) {
                cacheAssets = accountAssets;
                showAssetsCurrency();
            }
        });

        mViewModel.getCryptoCurrencies().observe(this, (List<CryptoCurrency> cryptoCurrencies) -> {
            BLog.i(tag(), "get crypto currencies");
            if (cryptoCurrencies != null && cryptoCurrencies.size() > 0) {
                cacheCryptoCurrencies = cryptoCurrencies;
                showAssetsCurrency();
                recyclerViewAssets.getAdapter().notifyDataSetChanged();
            } else if (cryptoCurrencies == null) {
                swipeRefreshLayout.setRefreshing(false);
                showLongToast(R.string.error_network);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cacheAssets = MainService.getInstance().getAccountAssetsList();
        showAssetsCurrency();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        BLog.e(tag(), "onNewIntent");
        boolean changeNetworkFlag = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_NETWORK, false);
        boolean changeLanguageFlag = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_LANGUAGE, false);
        // change network type
        if (changeNetworkFlag) {
            MainService.getInstance().setAccountAssetsList(new ArrayList<>());
            swipeRefreshLayout.setRefreshing(true);
            mViewModel.getTotalAssets();

            changeNetwork();
        }
        // change language; if change language, then recreate the activity to reload the resource.
        if (changeLanguageFlag) {
            this.recreate();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_wallet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.menu_accounts) {
            if (cacheAccounts != null && cacheAccounts.size() > 0) {
                Intent intent = new Intent(this, AccountsActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_accounts) {
            Intent intent = new Intent(this, AccountsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_info) {
            /*Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);*/
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     *  if account's length > 0, show the total assets;
     *  else show the create account.
     */
    private void checkContentShow() {
        if (cacheAccounts == null || cacheAccounts.size() == 0) {
            BLog.e(tag(), "the account is null");
            createAccountLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            swipeRefreshLayout.setRefreshing(true);
            BLog.e(tag(), "the account size is: " + cacheAccounts.size());
            createAccountLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Display the number of tokens and the corresponding legal currency value
     */
    private void showAssetsCurrency() {
        if (cacheAssets.size() == cacheAccounts.size() * cacheTokens.size()) {
            recyclerViewAssets.getAdapter().notifyDataSetChanged();

            BigDecimal totalValue = BigDecimal.ZERO;
            for (AccountAssets accountAssets : cacheAssets) {
                if (accountAssets.getBalance().compareTo(BigInteger.ZERO) > 0 && cacheCryptoCurrencies != null) {
                    for (CryptoCurrency cryptoCurrency : cacheCryptoCurrencies) {
                        if (cryptoCurrency.getName().toLowerCase()
                                .equals(accountAssets.getTokenEntity().getName().toLowerCase())) {
                            BigDecimal value = new BigDecimal(cryptoCurrency.getPriceCny())
                                    .multiply(CommonUtil.getAccountFromWei(accountAssets.getBalance()));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
                Intent intent = new Intent(MainActivity.this, TransferActivity.class);
                intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                startActivityForResult(intent, REQ_CODE_TRANSFER);
            });
            holder.tvTokenName.setText(tokenEntity.getShortName());
            ImageManager.showTokenIcon(MainActivity.this, holder.ivTokenIcon, tokenEntity.getAddress());
            BigInteger tokenCount = BigInteger.ZERO;
            for (AccountAssets accountAssets : cacheAssets) {
                if (accountAssets.getTokenEntity().getAddress().equals(tokenEntity.getAddress())) {
                    tokenCount = tokenCount.add(accountAssets.getBalance());
                }
            }
            if (BrahmaConfig.getInstance().isAssetsVisible()) {
                holder.tvTokenApproEqual.setText(R.string.asymptotic);
                holder.tvTokenAccount.setText(String.valueOf(CommonUtil.getAccountFromWei(tokenCount)));
                BigDecimal tokenValue = BigDecimal.ZERO;
                if (cacheCryptoCurrencies != null && cacheCryptoCurrencies.size() > 0) {
                    for (CryptoCurrency cryptoCurrency : cacheCryptoCurrencies) {
                        if (cryptoCurrency.getName().toLowerCase().equals(tokenEntity.getName().toLowerCase())) {
                            tokenValue = CommonUtil.getAccountFromWei(tokenCount).multiply(new BigDecimal(cryptoCurrency.getPriceCny()));
                        }
                    }
                }
                holder.tvTokenAssetsCount.setText(String.valueOf(tokenValue.setScale(2, BigDecimal.ROUND_HALF_UP)));
            } else {
                holder.tvTokenApproEqual.setText("");
                holder.tvTokenAccount.setText("****");
                holder.tvTokenAssetsCount.setText("********");
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
            TextView tvTokenAccount;
            TextView tvTokenApproEqual;
            TextView tvTokenAssetsCount;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutAssets = itemView.findViewById(R.id.layout_assets);
                ivTokenIcon = itemView.findViewById(R.id.iv_token_icon);
                tvTokenName = itemView.findViewById(R.id.tv_token_name);
                tvTokenAccount = itemView.findViewById(R.id.tv_token_count);
                tvTokenApproEqual = itemView.findViewById(R.id.tv_token_appro_equal);
                tvTokenAssetsCount = itemView.findViewById(R.id.tv_token_assets_count);
            }
        }
    }
}
