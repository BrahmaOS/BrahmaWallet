package io.brahmaos.wallet.brahmawallet.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

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
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.VersionInfo;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.service.VersionUpgradeService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountsActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.brahmawallet.ui.contact.ContactsActivity;
import io.brahmaos.wallet.brahmawallet.ui.home.MeFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.QuickPayFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.WalletFragment;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountInfoActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountTransferActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayTestActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayTransactionsListActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.AboutActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.HelpActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.SettingsActivity;
import io.brahmaos.wallet.brahmawallet.view.HomeViewPager;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.BrahmaOSURI;
import io.brahmaos.wallet.util.PermissionUtil;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, VersionUpgradeService.INewVerNotify {
    @Override
    protected String tag() {
        return MainActivity.class.getName();
    }

    @BindView(R.id.content_pager)
    HomeViewPager contentPager;
    @BindView(R.id.bottom_navigation)
    AHBottomNavigation bottomNavigation;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private VersionInfo newVersionInfo;
    private AccountViewModel mViewModel;
    private int currentFragmentPosition = 0;
    private int WALLET_FRAGMENT_POSITION = 0;
    private int PAY_FRAGMENT_POSITION = 1;
    private int ME_FRAGMENT_POSITION = 2;
    private List<AccountEntity> cacheAccounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BLog.i(tag(), "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        VersionUpgradeService.getInstance().checkVersion(this, true, this);
        initView();
        initData();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(getResources().getString(R.string.fragment_wallet),
                R.drawable.icon_bottom_tab_wallet);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(getResources().getString(R.string.fragment_pay),
                R.drawable.icon_bottom_tab_pay_a);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(getResources().getString(R.string.fragment_me),
                R.drawable.icon_bottom_tab_account);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#F9F9F9"));
        bottomNavigation.setAccentColor(Color.parseColor("#3C78C2"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

        // Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(false);
        // Force to tint the drawable (useful for font with icon for example)
        // bottomNavigation.setForceTint(true);
        // Manage titles
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        // Set listeners
        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            contentPager.setCurrentItem(position);
            currentFragmentPosition = position;
            invalidateOptionsMenu();

            if (position == WALLET_FRAGMENT_POSITION) {
                toolbar.setTitle(getString(R.string.title_brahma_wallet));
            } else if (position == PAY_FRAGMENT_POSITION) {
                toolbar.setTitle(getString(R.string.fragment_pay));
            } else if (position == ME_FRAGMENT_POSITION) {
                toolbar.setTitle(getString(R.string.fragment_me));
            }
            return true;
        });

        contentPager.setLocked(true);
        contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        contentPager.setAdapter(adapter);
    }

    private void initData() {
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            cacheAccounts = accountEntities;
            invalidateOptionsMenu();
        });
        PayService.getInstance().checkPayRequestToken();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        BLog.e(tag(), "onNewIntent");
        boolean changeLanguageFlag = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_LANGUAGE, false);
        boolean changeCurrencyUnit = intent.getBooleanExtra(IntentParam.FLAG_CHANGE_CURRENCY_UNIT, false);
        // change language; if change language, then recreate the activity to reload the resource.
        if (changeLanguageFlag) {
            this.recreate();
        }
        // change currency unit
        if (changeCurrencyUnit) {
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
        if (cacheAccounts != null && cacheAccounts.size() > 0 &&
                currentFragmentPosition == WALLET_FRAGMENT_POSITION) {
            getMenuInflater().inflate(R.menu.menu_accounts, menu);
        } else if (currentFragmentPosition == ME_FRAGMENT_POSITION) {
            getMenuInflater().inflate(R.menu.menu_settings, menu);
        } else if (currentFragmentPosition == PAY_FRAGMENT_POSITION) {
            getMenuInflater().inflate(R.menu.menu_scan, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_accounts) {
            Intent intent = new Intent(this, AccountsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_scan) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraScanPermission();
            } else {
                scanAddressCode();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private void scanAddressCode() {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "");
        startActivityForResult(intent, ReqCode.SCAN_QR_CODE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_accounts) {
            if (cacheAccounts != null && cacheAccounts.size() > 0) {
                Intent intent = new Intent(this, AccountsActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, CreateAccountActivity.class);
                startActivity(intent);
            }
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

    public void onClickLayout(View v) {
        switch (v.getId()){
            case R.id.layout_account_info:
                if (null == BrahmaConfig.getInstance().getPayAccount() ||
                        BrahmaConfig.getInstance().getPayAccount().isEmpty()) {
//                    contentPager.setCurrentItem(PAY_FRAGMENT_POSITION);
                    showLongToast(getString(R.string.no_quick_pay_account));

                } else {
                    Intent accInfoIntent = new Intent(this, PayAccountInfoActivity.class);
                    startActivity(accInfoIntent);
                }
                break;
            case R.id.tv_pay_trans_more:
            case R.id.layout_transactions:
                Intent transListIntent = new Intent(this, PayTransactionsListActivity.class);
                startActivity(transListIntent);
                break;
            case R.id.layout_address:
                Intent addrIntent = new Intent(this, ContactsActivity.class);
                startActivity(addrIntent);
                break;
            case R.id.layout_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                break;
            case R.id.layout_about_us:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.layout_pay_test:
                Intent payTestIntent = new Intent(this, PayTestActivity.class);
                startActivity(payTestIntent);
                break;
            default:
                break;
        }
    }

    /**
     * TAB
     */
    private class MainViewPagerAdapter extends FragmentPagerAdapter {

        private List<BaseFragment> fragments = new ArrayList<>();

        MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments.clear();
            fragments.add(WalletFragment.newInstance(R.layout.fragment_wallet,
                    R.string.fragment_wallet));
            fragments.add(QuickPayFragment.newInstance(R.layout.fragment_pay,
                    R.string.fragment_pay));
            fragments.add(MeFragment.newInstance(R.layout.fragment_me,
                    R.string.fragment_me));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PermissionUtil.CODE_EXTERNAL_STORAGE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                handleExternalStoragePermission();
            } else {
                PermissionUtil.openSettingActivity(this, getString(R.string.tip_external_storage_permission));
            }
        } else if (requestCode == ReqCode.SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String qrCode = data.getStringExtra(Intents.Scan.RESULT);
                    if (qrCode != null && qrCode.length() > 0) {
                        BrahmaOSURI brahmaOsUri = BrahmaOSURI.parse(qrCode);
                        if (brahmaOsUri == null) {
                            showLongToast(R.string.tip_invalid_receiver);
                            return;
                        }
//                        Intent transferActivity = new Intent(this, PayAccountTransferActivity.class);
//                        transferActivity.putExtra(IntentParam.PARAM_PAY_TRANSFER_RECEIPT, brahmaOsUri.getAddress());
//                        if (brahmaOsUri.getAmount() != null) {
//                            transferActivity.putExtra(IntentParam.PARAM_PAY_TRANSFER_AMOUNT, String.valueOf(brahmaOsUri.getAmount()));
//                        }
//                        transferActivity.putExtra(IntentParam.PARAM_PAY_TRANSFER_COIN, brahmaOsUri.getCoin());
//                        startActivity(transferActivity);
                        if (null == brahmaOsUri.getAmount() || brahmaOsUri.getAmount() <= 0) {
                            showShortToast(getString(R.string.tip_invalid_amount));
                            return;
                        }
                        String sendValueStr = String.valueOf(brahmaOsUri.getAmount());
                        if (sendValueStr.length() < 1) {
                            showLongToast(R.string.tip_invalid_amount);
                            return;
                        }
                        if (null == brahmaOsUri.getAddress() || !brahmaOsUri.getAddress().startsWith("0x")) {
                            showLongToast(R.string.tip_invalid_receiver);
                            return;
                        }
                        int coinCode = BrahmaConst.PAY_COIN_CODE_BRM;
                        if (null != brahmaOsUri.getCoin()) {
                            String coinName = brahmaOsUri.getCoin();
                            if (coinName.equalsIgnoreCase(BrahmaConst.COIN_SYMBOL_BRM)) {
                                coinCode = BrahmaConst.PAY_COIN_CODE_BRM;
                            } else if (coinName.equalsIgnoreCase(BrahmaConst.COIN_SYMBOL_BTC)) {
                                coinCode = BrahmaConst.PAY_COIN_CODE_BTC;
                            } else if (coinName.equalsIgnoreCase(BrahmaConst.COIN_SYMBOL_ETH)) {
                                coinCode = BrahmaConst.PAY_COIN_CODE_ETH;
                            }
                        }
                        Uri uri = Uri.parse(String.format("brahmaos://wallet/pay?trade_type=3&amount=%s&coin_code=%d&sender=%s&receiver=%s",
                                sendValueStr, coinCode, BrahmaConfig.getInstance().getPayAccount(), brahmaOsUri.getAddress()));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);

                    } else {
                        showLongToast(R.string.tip_scan_code_failed);
                    }
                } else {
                    showLongToast(R.string.tip_scan_code_failed);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
