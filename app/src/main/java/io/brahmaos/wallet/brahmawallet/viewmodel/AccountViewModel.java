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

package io.brahmaos.wallet.brahmawallet.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.util.BLog;
import rx.Completable;

public class AccountViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<AccountEntity>> mObservableAccounts;

    public AccountViewModel(Application application) {
        super(application);

        mObservableAccounts = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableAccounts.setValue(null);

        LiveData<List<AccountEntity>> accounts = ((WalletApp) application).getRepository()
                .getAccounts();

        // observe the changes of the accounts from the database and forward them
        mObservableAccounts.addSource(accounts, new Observer<List<AccountEntity>>() {
            @Override
            public void onChanged(@Nullable List<AccountEntity> value) {
                mObservableAccounts.setValue(value);
            }
        });
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return mObservableAccounts;
    }

    public Completable createAccount(AccountEntity account) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().createAccount(account);
        });
    }
}
