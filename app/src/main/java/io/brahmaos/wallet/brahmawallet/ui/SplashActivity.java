package io.brahmaos.wallet.brahmawallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.model.TokensVersionInfo;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountGuideActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.rayup.sdk.model.EthToken;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 *  Splash Page.
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected String tag() {
        return SplashActivity.class.getName();
    }

    // display time length
    private static final int DISPLAY_LEN = 2000;
    // the flag get all tokens
    private boolean flagAllTokens = false;
    // End of countdown sign
    private boolean flagCountdown = false;
    // the flag of get all accounts
    private boolean flagAllAccounts = false;

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
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null) {
                flagAllAccounts = true;
                if (accountEntities.size() > 0) {
                    MainService.getInstance().setHaveAccount(true);
                    // init btc account walletAppKit
                    for (AccountEntity accountEntity : accountEntities) {
                        if (accountEntity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                            BtcAccountManager.getInstance().initExistsWalletAppKit(accountEntity);
                        }
                    }
                }
                jumpToMain();
            }
        });

        // If there is no token list at present, access the main page after getting
        // the token list in the splash page, otherwise go to the main page.
        mViewModel.getAllTokens().observe(this, allTokens -> {
            if (allTokens != null && allTokens.size() > 0) {
                AllTokenEntity allTokenEntity = allTokens.get(0);
                if (allTokenEntity.getCode() > 0) {
                    flagAllTokens = true;
                    jumpToMain();
                    return;
                }
            }

            MainService.getInstance().getLatestTokenList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<EthToken>>() {
                        @Override
                        public void onNext(List<EthToken> coins) {
                            flagAllTokens = true;
                            jumpToMain();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            BLog.e(tag(), "Unable to load token", e);
                            flagAllTokens = true;
                            jumpToMain();
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });
        });

        splashCountdown();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // main page
    private void splashCountdown() {
        new Handler().postDelayed(() -> {
            flagCountdown = true;
            jumpToMain();
        }, DISPLAY_LEN);
    }

    // main page
    private void jumpToMain() {
        if (flagAllAccounts && flagCountdown && flagAllTokens) {
            if (BrahmaConfig.getInstance().isTouchId() && CommonUtil.isFinger(this)) {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, FingerActivity.class);
                startActivity(intent);
                finish();
            } else if (MainService.getInstance().isHaveAccount()) {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, AccountGuideActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
