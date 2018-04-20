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
import android.media.session.MediaSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.util.BLog;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class AccountViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<AccountEntity>> mObservableAccounts;
    private final MediatorLiveData<List<TokenEntity>> mObservableTokens;

    public AccountViewModel(Application application) {
        super(application);

        mObservableAccounts = new MediatorLiveData<>();
        mObservableTokens = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableAccounts.setValue(null);
        mObservableTokens.setValue(null);

        LiveData<List<AccountEntity>> accounts = ((WalletApp) application).getRepository()
                .getAccounts();

        // observe the changes of the accounts from the database and forward them
        mObservableAccounts.addSource(accounts, value -> mObservableAccounts.setValue(value));

        LiveData<List<TokenEntity>> tokens = ((WalletApp) application).getRepository()
                .getTokens();

        // observe the changes of the accounts from the database and forward them
        mObservableTokens.addSource(tokens, value -> mObservableTokens.setValue(value));
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

    public Completable createAccount(String name, String password) {
        return Completable.fromAction(() -> {
            try {
                String filename = BrahmaWeb3jService.getInstance().generateLightNewWalletFile(password,
                        getApplication().getFilesDir());
                String address = BrahmaWeb3jService.getInstance().getWalletAddress(password,
                        getApplication().getFilesDir() + "/" +  filename);

                AccountEntity account = new AccountEntity();
                account.setName(name);
                account.setAddress(address);
                account.setFilename(filename);
                ((WalletApp) getApplication()).getRepository().createAccount(account);
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | IOException | CipherException e) {
                e.printStackTrace();
            }
        });
    }

    public Observable<Boolean> importAccount(WalletFile walletFile, String password, String name) {
        return Observable.create(e -> {
            BLog.e("view model", "Observable thread is : " + Thread.currentThread().getName());
            if (BrahmaWeb3jService.getInstance().isValidKeystore(walletFile, password)) {
                // save keystore in local system
                SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
                String filename = dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
                File destination = new File(getApplication().getFilesDir(), filename);
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                objectMapper.writeValue(destination, walletFile);

                BLog.i("viewModel", "the private key is valid;");
                AccountEntity account = new AccountEntity();
                account.setName(name);
                account.setAddress(BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress()));
                account.setFilename(filename);
                ((WalletApp) getApplication()).getRepository().createAccount(account);
                e.onNext(Boolean.TRUE);
            } else {
                e.onNext(Boolean.FALSE);
            }
            e.onComplete();
        });
    }

    /*
     * Due to the wallet file is generated by private key,
     * don't need to check private key;
     * @result the account address
     */
    public Observable<String> importAccountWithPrivateKey(String privateKey, String password, String name) {
        return Observable.create(e -> {
            ECKeyPair ecKeyPair = ECKeyPair.create(Hex.decode(privateKey));
            WalletFile walletFile = Wallet.createLight(password, ecKeyPair);
            String address = BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress());

            // check the account address
            List<AccountEntity> accounts = mObservableAccounts.getValue();
            boolean cancel = false;
            if (accounts != null && accounts.size() > 0) {
                for (AccountEntity accountEntity : accounts) {
                    if (accountEntity.getAddress().equals(address)) {
                        cancel = true;
                        break;
                    }
                }
            }

            if (cancel) {
                e.onNext("");
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
                String filename = dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
                File destination = new File(getApplication().getFilesDir(), filename);
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                objectMapper.writeValue(destination, walletFile);

                AccountEntity account = new AccountEntity();
                account.setName(name);
                account.setAddress(BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress()));
                account.setFilename(filename);
                ((WalletApp) getApplication()).getRepository().createAccount(account);
                e.onNext(address);
            }
            e.onComplete();
        });
    }

    public LiveData<List<TokenEntity>> getTokens() {
        return mObservableTokens;
    }
}
