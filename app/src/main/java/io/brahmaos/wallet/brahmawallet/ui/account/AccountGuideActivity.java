package io.brahmaos.wallet.brahmawallet.ui.account;

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
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

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
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.service.VersionUpgradeService;
import io.brahmaos.wallet.brahmawallet.ui.MainActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.contact.ContactsActivity;
import io.brahmaos.wallet.brahmawallet.ui.home.MeFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.QuickPayFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.WalletFragment;
import io.brahmaos.wallet.brahmawallet.ui.setting.AboutActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.HelpActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.SettingsActivity;
import io.brahmaos.wallet.brahmawallet.view.HomeViewPager;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.PermissionUtil;

public class AccountGuideActivity extends BaseActivity {
    @Override
    protected String tag() {
        return AccountGuideActivity.class.getName();
    }
    private AccountViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BLog.i(tag(), "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_guide);
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button createWalletBtn = findViewById(R.id.btn_create_account);
        createWalletBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });
        TextView restoreAccountBtn = findViewById(R.id.btn_restore_account);
        restoreAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestoreAccountActivity.class);
            startActivity(intent);
        });
    }

    private void initData() {
        // Get the account list to prevent the home page sloshing
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null && accountEntities.size() > 0) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            timerExit.schedule(timerTask, 500);
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
