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

import android.app.Application;

import io.brahmaos.wallet.brahmawallet.db.database.WalletDatabase;
import io.brahmaos.wallet.brahmawallet.repository.DataRepository;
import io.brahmaos.wallet.util.BLog;

/**
 * Android Application class. Used for accessing singletons.
 */
public class WalletApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WalletDatabase.getInstance(getApplicationContext());
    }

    public WalletDatabase getDatabase() {
        return WalletDatabase.getInstance(this);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
