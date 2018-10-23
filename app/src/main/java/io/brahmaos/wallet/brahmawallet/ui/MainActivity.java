package io.brahmaos.wallet.brahmawallet.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Timer;
import java.util.TimerTask;

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
import io.brahmaos.wallet.brahmawallet.model.VersionInfo;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.service.VersionUpgradeService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountsActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.ImportAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.contact.ContactsActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.AboutActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.CelestialBodyIntroActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.HelpActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.SettingsActivity;
import io.brahmaos.wallet.brahmawallet.ui.token.TokensActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.InstantExchangeActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.TransferActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.PermissionUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, VersionUpgradeService.INewVerNotify {
    @Override
    protected String tag() {
        return MainActivity.class.getName();
    }

    public static int REQ_CODE_TRANSFER = 10;

    @BindView(R.id.layout_header)
    LinearLayout layoutHeader;
    @BindView(R.id.layout_new_account)
    ConstraintLayout createAccountLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_appro_equal)
    TextView tvApproEqual;
    @BindView(R.id.tv_test_network)
    TextView tvTestNetwork;
    @BindView(R.id.tv_money_unit)
    TextView tvCurrencyUnit;
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
    private VersionInfo newVersionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BLog.i(tag(), "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);

        RxBus.get().register(this);

        VersionUpgradeService.getInstance().checkVersion(this, true, this);
        MainService.getInstance().getTokensLatestVersion();

        if (MainService.getInstance().isHaveAccount()) {
            createAccountLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            createAccountLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        initView();
        initData();
    }

    private void initView() {
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        ImageView ivCelestialBody = navigationView.getHeaderView(0).findViewById(R.id.iv_celestial_body);
        ivCelestialBody.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CelestialBodyIntroActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // get the latest assets
            mViewModel.getTotalAssets();
            // get Currencies
            getCryptoCurrents();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAssets.setLayoutManager(layoutManager);
        recyclerViewAssets.setAdapter(new AssetsRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewAssets.setHasFixedSize(true);
        recyclerViewAssets.setNestedScrollingEnabled(false);

        tvCurrencyUnit.setText(BrahmaConfig.getInstance().getCurrencyUnit());

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
        mViewModel.getAllTokensCount().observe(this, count -> {
            if (count == null || count <= 0) {
                MainService.getInstance().getTokenListByIPFS();
            }
        });
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



    @Override
    protected void onNewIntent(Intent intent) {
        BLog.e(tag(), "onNewIntent");
        boolean changeNetworkFlag = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_NETWORK, false);
        boolean changeLanguageFlag = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_LANGUAGE, false);
        boolean changeCurrencyUnit = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_CURRENCY_UNIT, false);
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
        // change currency unit
        if (changeCurrencyUnit) {
            tvCurrencyUnit.setText(BrahmaConfig.getInstance().getCurrencyUnit());
            showAssetsCurrency();
            recyclerViewAssets.getAdapter().notifyDataSetChanged();
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
        if (item.getItemId() == R.id.menu_instant_exchange) {
            if (cacheAccounts.size() > 0) {
                Intent intent = new Intent(this, InstantExchangeActivity.class);
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
        } else if (id == R.id.nav_contacts) {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void alreadyLatest() {

    }

    @Override
    public void confirmUpdate(VersionInfo newVer) {
        newVersionInfo = newVer;
    }

    @Override
    public void cancelUpdate(VersionInfo newVer) {

    }

    @Override
    public void handleExternalStoragePermission() {
        VersionUpgradeService.getInstance().downloadApkFile(this, newVersionInfo, this);
    }

    private void getCryptoCurrents() {
        String symbols;
        if (cacheTokens != null && cacheTokens.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (TokenEntity token : cacheTokens) {
                stringBuilder.append(token.getShortName()).append(",");
            }
            symbols = stringBuilder.toString();
        } else {
            symbols = "ETH,BRM";
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
                        if (CommonUtil.cryptoCurrencyCompareToken(cryptoCurrency, accountAssets.getTokenEntity())) {
                            double tokenPrice = cryptoCurrency.getPriceCny();
                            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                                tokenPrice = cryptoCurrency.getPriceUsd();
                            }
                            BigDecimal value = new BigDecimal(tokenPrice)
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
        } else if (requestCode == PermissionUtil.CODE_EXTERNAL_STORAGE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                handleExternalStoragePermission();
            } else {
                PermissionUtil.openSettingActivity(this, getString(R.string.tip_external_storage_permission));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
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
                Intent intent = new Intent(MainActivity.this, TransferActivity.class);
                intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                startActivityForResult(intent, REQ_CODE_TRANSFER);
            });
            holder.tvTokenName.setText(tokenEntity.getShortName());
            holder.tvTokenFullName.setText(tokenEntity.getName());
            holder.tvTokenPrice.setText("0");
            ImageManager.showTokenIcon(MainActivity.this, holder.ivTokenIcon,
                    tokenEntity.getName(), tokenEntity.getAddress());
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
                            Glide.with(MainActivity.this)
                                    .load(R.drawable.currency_cny)
                                    .into(holder.ivTokenPrice);
                            Glide.with(MainActivity.this)
                                    .load(R.drawable.currency_cny)
                                    .into(holder.ivTokenAssets);
                        } else {
                            Glide.with(MainActivity.this)
                                    .load(R.drawable.currency_usd)
                                    .into(holder.ivTokenPrice);
                            Glide.with(MainActivity.this)
                                    .load(R.drawable.currency_usd)
                                    .into(holder.ivTokenAssets);
                        }
                        tokenValue = CommonUtil.getAccountFromWei(tokenCount).multiply(new BigDecimal(tokenPrice));
                        holder.tvTokenPrice.setText(String.valueOf(new BigDecimal(tokenPrice).setScale(3, BigDecimal.ROUND_HALF_UP)));
                        break;
                    }
                }
            }
            if (BrahmaConfig.getInstance().isAssetsVisible()) {
                holder.tvTokenApproEqual.setText(R.string.asymptotic);
                holder.tvTokenAccount.setText(String.valueOf(CommonUtil.getAccountFromWei(tokenCount)));
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
            TextView tvTokenFullName;
            TextView tvTokenPrice;
            TextView tvTokenAccount;
            TextView tvTokenApproEqual;
            TextView tvTokenAssetsCount;
            ImageView ivTokenPrice;
            ImageView ivTokenAssets;

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
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                finish();
                timerExit.schedule(timerTask, 500);
            }
        }
        return false;
    }
    private Timer timerExit = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            System.exit(0);
        }
    };
}
