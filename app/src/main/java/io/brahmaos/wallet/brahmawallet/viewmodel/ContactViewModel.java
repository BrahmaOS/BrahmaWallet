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

import java.util.List;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import rx.Completable;

public class ContactViewModel extends AndroidViewModel {

    public ContactViewModel(Application application) {
        super(application);
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     *
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return ((WalletApp) getApplication()).getRepository().getAccounts();
    }

    public LiveData<List<ContactEntity>> getContacts() {
        return ((WalletApp) getApplication()).getRepository().loadAllContact();
    }

    public Completable createContact(ContactEntity contact) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().insertContact(contact);
        });
    }

    public Completable updateContact(int contactId, ContactEntity contact) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().updateContact(contactId, contact);
        });
    }

    public Completable deleteContact(int contactId) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().deleteContact(contactId);
        });
    }

    public LiveData<ContactEntity> getContactById(int contactId) {
        return ((WalletApp) getApplication()).getRepository().getContactById(contactId);
    }
}
