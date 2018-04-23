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
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.util.BLog;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AccountViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<AccountEntity>> mObservableAccounts;
    private final MediatorLiveData<List<TokenEntity>> mObservableTokens;
    private final MediatorLiveData<List<AccountAssets>> mObservableAssets;
    private List<AccountAssets> assetsList = new ArrayList<>();

    public AccountViewModel(Application application) {
        super(application);

        mObservableAccounts = new MediatorLiveData<>();
        mObservableTokens = new MediatorLiveData<>();
        mObservableAssets = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableAccounts.setValue(null);
        mObservableTokens.setValue(null);
        mObservableAssets.setValue(null);

        LiveData<List<AccountEntity>> accounts = ((WalletApp) application).getRepository()
                .getAccounts();

        // observe the changes of the accounts from the database and forward them
        mObservableAccounts.addSource(accounts, value -> {
            mObservableAccounts.setValue(value);
            getTotalAssets();
        });

        LiveData<List<TokenEntity>> tokens = ((WalletApp) application).getRepository()
                .getTokens();

        // observe the changes of the accounts from the database and forward them
        mObservableTokens.addSource(tokens, new android.arch.lifecycle.Observer<List<TokenEntity>>() {
            @Override
            public void onChanged(@Nullable List<TokenEntity> value) {
                mObservableTokens.setValue(value);
                getTotalAssets();
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
            try {
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
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onNext(Boolean.FALSE);
            }

            e.onCompleted();
        });
    }

    /*
     * Due to the wallet file is generated by private key,
     * don't need to check private key;
     * @result the account address,
     * If an empty string is returned, the imported account already exists.
     * If an exception is returned, an exception has occurred during processing.
     */
    public Observable<String> importAccountWithPrivateKey(String privateKey, String password, String name) {
        return Observable.create(e -> {
            try {
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
            } catch (CipherException | IOException e1) {
                e1.printStackTrace();
                e.onNext("exception");
            }
            e.onCompleted();
        });
    }

    public LiveData<List<TokenEntity>> getTokens() {
        return mObservableTokens;
    }

    public Completable checkToken(TokenEntity tokenEntity) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().createToken(tokenEntity);
        });

    }

    public Completable uncheckToken(TokenEntity tokenEntity) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().deleteToken(tokenEntity.getAddress());
        });
    }

    public LiveData<List<AccountAssets>> getAssets() {
        return mObservableAssets;
    }

    /*
     * Get all the token's assets for all accounts
     */
    private void getTotalAssets() {
        List<AccountEntity> accounts = mObservableAccounts.getValue();
        List<TokenEntity> tokens = mObservableTokens.getValue();
        if (accounts != null && accounts.size() > 0 && tokens != null && tokens.size() > 0) {
            BLog.i("view model", "get account assets success");
            for (AccountEntity accountEntity : accounts) {
                for (TokenEntity tokenEntity : tokens) {
                    getTokenAssets(accountEntity, tokenEntity);
                }
            }
        }
    }

    /*
     * Get the specified token asset of the specified account
     */
    private void getTokenAssets(final AccountEntity account, final TokenEntity tokenEntity) {
        if (tokenEntity.getShortName().equals("ETH")) {
            BrahmaWeb3jService.getInstance().getEthBalance(account)
                    .observable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<EthGetBalance>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            BLog.e("view model", e.getMessage());
                            AccountAssets assets = new AccountAssets(account, tokenEntity, BigInteger.ZERO);
                            checkTokenAsset(assets);
                        }

                        @Override
                        public void onNext(EthGetBalance ethBalance) {
                            if (ethBalance != null && ethBalance.getBalance() != null) {
                                BLog.i("view model", "the " + account.getName() + " eth's balance is " + ethBalance.getBalance().toString());
                                AccountAssets assets = new AccountAssets(account, tokenEntity, ethBalance.getBalance());
                                checkTokenAsset(assets);
                            } else {
                                BLog.w("view model", "the " + account.getName() + " 's eth balance is " + ethBalance.getBalance().toString());
                                AccountAssets assets = new AccountAssets(account, tokenEntity, BigInteger.ZERO);
                                checkTokenAsset(assets);
                            }
                        }
                    });
        } else {
            BrahmaWeb3jService.getInstance().getTokenBalance(account, tokenEntity)
                    .observable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<EthCall>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            BLog.e("view model", e.getMessage());
                            AccountAssets assets = new AccountAssets(account, tokenEntity, BigInteger.ZERO);
                            checkTokenAsset(assets);
                        }

                        @Override
                        public void onNext(EthCall ethCall) {
                            if (ethCall != null && ethCall.getValue() != null) {
                                BLog.i("view model", "the " + account.getName() + "'s " +
                                        tokenEntity.getName() + " balance is " + Numeric.decodeQuantity(ethCall.getValue()));
                                AccountAssets assets = new AccountAssets(account, tokenEntity, Numeric.decodeQuantity(ethCall.getValue()));
                                checkTokenAsset(assets);
                            } else {
                                BLog.w("view model", "the " + account.getName() + "'s " +
                                        tokenEntity.getName() + " balance is null");
                                AccountAssets assets = new AccountAssets(account, tokenEntity, BigInteger.ZERO);
                                checkTokenAsset(assets);
                            }
                        }
                    });
        }

    }

    /*
     * If the token asset of the account has already exists, then replace it with the new assets.
     * When all assets has exists, post value to main page
     */
    private void checkTokenAsset(AccountAssets assets) {
        for (AccountAssets localAssets : assetsList) {
            if (localAssets.getAccountEntity().getAddress().equals(assets.getAccountEntity().getAddress()) &&
                    localAssets.getTokenEntity().getAddress().equals(assets.getTokenEntity().getAddress())) {
                assetsList.remove(localAssets);
                break;
            }
        }
        assetsList.add(assets);
        if (mObservableAccounts.getValue() != null && mObservableTokens.getValue() != null) {
            int totalCount = mObservableAccounts.getValue().size() *
                    mObservableTokens.getValue().size();
            if (totalCount == assetsList.size()) {
                mObservableAssets.postValue(assetsList);
            }
        }
    }
}
