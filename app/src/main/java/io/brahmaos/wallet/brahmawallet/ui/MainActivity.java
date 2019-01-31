package io.brahmaos.wallet.brahmawallet.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.VersionInfo;
import io.brahmaos.wallet.brahmawallet.service.VersionUpgradeService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountsActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.contact.ContactsActivity;
import io.brahmaos.wallet.brahmawallet.ui.home.DiscoverFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.HashRateFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.WalletFragment;
import io.brahmaos.wallet.brahmawallet.ui.setting.AboutActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.CelestialBodyIntroActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.HelpActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.SettingsActivity;
import io.brahmaos.wallet.brahmawallet.view.HomeViewPager;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
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
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private VersionInfo newVersionInfo;
    private AccountViewModel mViewModel;
    private int currentFragmentPosition = 0;
    private int WALLET_FRAGMENT_POSITION = 0;
    private int HASH_RATE_FRAGMENT_POSITION = 1;
    private int DISCOVER_FRAGMENT_POSITION = 2;
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

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(getResources().getString(R.string.fragment_wallet),
                R.drawable.icon_bottom_tab_wallet);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(getResources().getString(R.string.fragment_hash_rate),
                R.drawable.icon_bottom_tab_power);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(getResources().getString(R.string.fragment_discover),
                R.drawable.icon_bottom_tab_search);

        // Add items
        bottomNavigation.addItem(item1);
        /*bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);*/

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
            } else if (position == HASH_RATE_FRAGMENT_POSITION) {
                toolbar.setTitle(getString(R.string.fragment_hash_rate));
            } else if (position == DISCOVER_FRAGMENT_POSITION) {
                toolbar.setTitle(getString(R.string.fragment_discover));
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
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_accounts) {
            Intent intent = new Intent(this, AccountsActivity.class);
            startActivity(intent);
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
            /*fragments.add(HashRateFragment.newInstance(R.layout.fragment_hash_rate,
                    R.string.fragment_hash_rate));
            fragments.add(DiscoverFragment.newInstance(R.layout.fragment_discover,
                    R.string.fragment_discover));*/
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
