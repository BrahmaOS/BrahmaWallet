package io.brahmaos.wallet.brahmawallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import rx.Completable;
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

        });
        mViewModel.getTokens().observe(this, accountEntities -> {

        });

        /**
         *  If there is no token list at present, then access the main page after getting
         *  the token list in the splash page, otherwise go to the main page and compare
         *  whether the token is changed
         */
        String localHash = BrahmaConfig.getInstance().getTokenListHash();
        if (localHash == null || localHash.length() < 1) {
            BrahmaWeb3jService.getInstance()
                    .getTokensHash()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onNext(String tokenListIpfsHash) {
                            if (tokenListIpfsHash != null && tokenListIpfsHash.length() > 0) {
                                BrahmaConfig.getInstance().setTokenListHash(tokenListIpfsHash);
                                Networks.getInstance().getIpfsApi()
                                        .getIpfsInfo(tokenListIpfsHash)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Observer<List<List<Object>>>() {
                                            @Override
                                            public void onCompleted() {
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                e.printStackTrace();
                                            }

                                            @Override
                                            public void onNext(List<List<Object>> apiRespResult) {
                                                List<AllTokenEntity> allTokenEntities = new ArrayList<>();
                                                // add BRM and ETH
                                                AllTokenEntity ethToken = new AllTokenEntity(0, "Ethereum", "ETH",
                                                        "", "", BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG);
                                                AllTokenEntity brmToken = new AllTokenEntity(0, "BrahmaOS", "BRM",
                                                        "0xd7732e3783b0047aa251928960063f863ad022d8", "", BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG);
                                                allTokenEntities.add(brmToken);
                                                allTokenEntities.add(ethToken);
                                                for (List<Object> token : apiRespResult) {
                                                    if (!token.get(2).toString().toLowerCase().equals(BrahmaConst.BRAHMAOS_TOKEN)) {
                                                        AllTokenEntity tokenEntity = new AllTokenEntity();
                                                        tokenEntity.setAddress(token.get(0).toString());
                                                        tokenEntity.setShortName(token.get(1).toString());
                                                        tokenEntity.setName(token.get(2).toString());
                                                        HashMap avatarObj = (HashMap) token.get(3);
                                                        tokenEntity.setAvatar(avatarObj.get("128x128").toString());
                                                        if (apiRespResult.indexOf(token) < BrahmaConst.DEFAULT_TOKEN_COUNT) {
                                                            tokenEntity.setShowFlag(BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG);
                                                        }
                                                        allTokenEntities.add(tokenEntity);
                                                    }

                                                }
                                                BLog.i(tag(), "the result:" + allTokenEntities.size());

                                                MainService.getInstance()
                                                        .loadAllTokens(allTokenEntities)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(() -> {
                                                                    flagAllTokens = true;
                                                                    jumpToMain();
                                                                },
                                                                throwable -> {
                                                                    BLog.e(tag(), "Unable to check token", throwable);
                                                                    flagAllTokens = true;
                                                                    jumpToMain();
                                                                });

                                            }
                                        });
                            } else {
                                flagAllTokens = true;
                                jumpToMain();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            flagAllTokens = true;
                            jumpToMain();
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });
        } else {
            flagAllTokens = true;
        }

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
        if (flagCountdown && flagAllTokens) {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
