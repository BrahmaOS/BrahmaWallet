/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.brahmaos.wallet.brahmawallet;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.database.WalletDatabase;
import io.brahmaos.wallet.brahmawallet.repository.DataRepository;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticCrashHandler;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticEventAgent;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticLog;
import io.brahmaos.wallet.brahmawallet.ui.FingerActivity;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.FileHelper;
import io.rayup.sdk.RayUpApp;

/**
 * Android Application class. Used for accessing singletons.
 */
public class WalletApp extends Application {
//    private boolean firstOpenApp = true;
    private static Boolean isTimeOut = false;

    private Timer timerTimeOut = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            isTimeOut = true;
        }
    };
    private RayUpApp rayUpApp;

    @Override
    public void onCreate() {
        super.onCreate();
        WalletDatabase.getInstance(getApplicationContext());
        MainService.getInstance().init(getApplicationContext());
        BrahmaConfig.getInstance().init(getApplicationContext());
        FileHelper.getInstance().init(getApplicationContext());
        rayUpApp = RayUpApp.initialize(BrahmaConst.rayupAccessKeyId, BrahmaConst.rayupAccessKeySecret);

        AppFrontBackHelper helper = new AppFrontBackHelper();
        helper.register(WalletApp.this, new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            public void onFront(Activity activity) {
                if (/*!firstOpenApp && */isTimeOut && BrahmaConfig.getInstance().isTouchId()
                        && CommonUtil.isFinger(getApplicationContext()) &&
                        (null == activity || !FingerActivity.class.getName().equals(activity.getClass().getName()))) {
                    Intent intent = new Intent(WalletApp.this, FingerActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onBack() {
//                firstOpenApp = false;
                isTimeOut = false;
                if (timerTimeOut != null) {
                    timerTimeOut.cancel();
                }
                timerTimeOut = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        isTimeOut = true;
                    }
                };
                timerTimeOut.schedule(timerTask, 5000);
            }
        });
        //azalea
        StatisticLog.setDEBUG(BrahmaConfig.debugFlag);
        StatisticEventAgent.getInstance(this).init();
        StatisticCrashHandler.getInstance().init(this);

        StatisticEventAgent.onApplicationStart(this/*getClass().getName()*/);
    }

    public RayUpApp getRayUpApp() {
        return rayUpApp;
    }

//    public boolean isFirstOpenApp() {
//        return firstOpenApp;
//    }

    public WalletDatabase getDatabase() {
        return WalletDatabase.getInstance(this);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
