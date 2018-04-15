package io.brahmaos.wallet.brahmawallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;


/**
 *  Splash Page.
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected String tag() {
        return SplashActivity.class.getName();
    }

    // display time length
    private static final int DISPLAY_LEN = 500;

    private AccountViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get the account list to prevent the home page sloshing
        mViewModel.getAccounts().observe(this, new android.arch.lifecycle.Observer<List<AccountEntity>>() {
            @Override
            public void onChanged(@Nullable List<AccountEntity> accountEntities) {
                if (accountEntities != null) {
                    jumpToMain();
                }
            }
        });
        jumpToMain();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // main page
    private void jumpToMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, DISPLAY_LEN);
    }
}
