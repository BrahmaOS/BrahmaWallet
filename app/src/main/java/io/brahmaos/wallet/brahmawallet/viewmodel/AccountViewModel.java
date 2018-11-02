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
import com.google.common.base.Splitter;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
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
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.util.BLog;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
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
        // When accounts change, reacquire the total assets.
        mObservableAccounts.addSource(accounts, value -> {
            BLog.i("view model", "get account list");
            mObservableAccounts.setValue(value);
            getTotalAssets();
        });
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return mObservableAccounts;
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

    /**
     * Generate ethereum account via mnemonics
     */
    public Completable createAccountWithMnemonic(String name, String password) {
        return Completable.fromAction(() -> {
            try {
                String passphrase = "";
                SecureRandom secureRandom = new SecureRandom();
                long creationTimeSeconds = System.currentTimeMillis() / 1000;
                DeterministicSeed deterministicSeed = new DeterministicSeed(secureRandom, 128, passphrase, creationTimeSeconds);
                List<String> mnemonicCode = deterministicSeed.getMnemonicCode();

                if (mnemonicCode != null && mnemonicCode.size() > 0) {
                    StringBuilder mnemonicStr = new StringBuilder();
                    for (String mnemonic : mnemonicCode) {
                        mnemonicStr.append(mnemonic).append(" ");
                    }
                    long timeSeconds = System.currentTimeMillis() / 1000;
                    DeterministicSeed seed = new DeterministicSeed(mnemonicStr.toString().trim(), null, "", timeSeconds);

                    // create ethereum account
                    DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed).build();
                    List<ChildNumber> keyPath = HDUtils.parsePath("M/44H/60H/0H/0/0");
                    DeterministicKey key = chain.getKeyByPath(keyPath, true);
                    BigInteger privateKey = key.getPrivKey();
                    // Web3j
                    ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
                    WalletFile walletFile = Wallet.createLight(password, ecKeyPair);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
                    String filename = dateFormat.format(new Date()) + walletFile.getAddress() + ".json";

                    File destination = new File(getApplication().getFilesDir(), filename);
                    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                    objectMapper.writeValue(destination, walletFile);

                    AccountEntity account = new AccountEntity();
                    account.setName(name);
                    account.setAddress(BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress()));
                    account.setFilename(filename);
                    account.setMnemonics(mnemonicCode);
                    account.setType(BrahmaConst.ETH_ACCOUNT_TYPE);
                    MainService.getInstance().setNewMnemonicAccount(account);
                    ((WalletApp) getApplication()).getRepository().createAccount(account);

                    // create btc account
                    String btcFilePrefix = seed.toHexString();
                    seed.setCreationTimeSeconds(timeSeconds);
                    BtcAccountManager.getInstance().createWalletAppKit(btcFilePrefix, seed);
                    AccountEntity btcAccount = new AccountEntity();
                    btcAccount.setName(name);
                    btcAccount.setAddress("");
                    btcAccount.setFilename(btcFilePrefix);
                    btcAccount.setMnemonics(mnemonicCode);
                    btcAccount.setType(BrahmaConst.BTC_ACCOUNT_TYPE);
                    MainService.getInstance().setNewMnemonicAccount(btcAccount);
                    ((WalletApp) getApplication()).getRepository().createAccount(btcAccount);
                }
            } catch (IOException | CipherException | UnreadableWalletException e) {
                e.printStackTrace();
            }
        });
    }

    /*
     * Restore ETH account & BTC account through mnemonics
     * If an exception is returned, an exception has occurred during processing.
     */
    public Observable<String> restoreAccountWithMnemonics(String mnemonics, String password, String accountName) {
        return Observable.create((Subscriber<? super String> e) -> {
            try {
                List<String> mnemonicsCodes = Splitter.on(" ").splitToList(mnemonics);
                MnemonicCode.INSTANCE.check(mnemonicsCodes);
                long timeSeconds = System.currentTimeMillis() / 1000;
                DeterministicSeed seed = new DeterministicSeed(mnemonics, null, "", timeSeconds);

                // restore eth account
                DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed).build();
                List<ChildNumber> keyPath = HDUtils.parsePath("M/44H/60H/0H/0/0");
                DeterministicKey key = chain.getKeyByPath(keyPath, true);
                BigInteger privateKey = key.getPrivKey();
                ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
                WalletFile walletFile = Wallet.createLight(password, ecKeyPair);

                String address = BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress());

                // check the eth account address
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
                    account.setName(accountName);
                    account.setType(BrahmaConst.ETH_ACCOUNT_TYPE);
                    account.setAddress(address);
                    account.setFilename(filename);
                    ((WalletApp) getApplication()).getRepository().createAccount(account);

                    // restore btc account
                    String btcFilePrefix = seed.toHexString();
                    //seed.setCreationTimeSeconds(timeSeconds);
                    BtcAccountManager.getInstance().restoreWalletAppKit(btcFilePrefix, seed);
                    AccountEntity btcAccount = new AccountEntity();
                    btcAccount.setName(accountName);
                    btcAccount.setAddress("");
                    btcAccount.setFilename(btcFilePrefix);
                    btcAccount.setType(BrahmaConst.BTC_ACCOUNT_TYPE);
                    MainService.getInstance().setNewMnemonicAccount(btcAccount);
                    ((WalletApp) getApplication()).getRepository().createAccount(btcAccount);
                    e.onNext(address);
                }
            } catch (MnemonicException e1) {
                e1.printStackTrace();
                e.onError(e1);
            } catch (CipherException | IOException | UnreadableWalletException e1) {
                e1.printStackTrace();
                e.onNext("exception");
            }
            e.onCompleted();
        });
    }

    public Observable<Boolean> importEthAccountWithKeystore(WalletFile walletFile, String password, String name) {
        return Observable.create(e -> {
            try {
                BLog.d("view model", "Observable thread is : " + Thread.currentThread().getName());
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
                    account.setType(BrahmaConst.ETH_ACCOUNT_TYPE);
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
    public Observable<String> importEthAccountWithPrivateKey(String privateKey, String password, String name) {
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
                    account.setType(BrahmaConst.ETH_ACCOUNT_TYPE);
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

    /*
     * Due to the wallet file is generated by mnemonics,
     * don't need to check private key;
     * @result the account address,
     * If an empty string is returned, the imported account already exists.
     * If an exception is returned, an exception has occurred during processing.
     */
    public Observable<String> importEthAccountWithMnemonics(String mnemonics, String password, String accountName) {
        return Observable.create((Subscriber<? super String> e) -> {
            try {
                List<String> mnemonicsCodes = Splitter.on(" ").splitToList(mnemonics);
                MnemonicCode.INSTANCE.check(mnemonicsCodes);

                long timeSeconds = System.currentTimeMillis() / 1000;
                DeterministicSeed seed = new DeterministicSeed(mnemonics, null, "", timeSeconds);
                DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed).build();
                List<ChildNumber> keyPath = HDUtils.parsePath("M/44H/60H/0H/0/0");
                DeterministicKey key = chain.getKeyByPath(keyPath, true);
                BigInteger privateKey = key.getPrivKey();
                ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
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
                    account.setName(accountName);
                    account.setAddress(BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress()));
                    account.setFilename(filename);
                    account.setType(BrahmaConst.ETH_ACCOUNT_TYPE);
                    ((WalletApp) getApplication()).getRepository().createAccount(account);
                    e.onNext(address);
                }
            } catch (MnemonicException e1) {
                e1.printStackTrace();
                e.onError(e1);
            } catch (CipherException | IOException | UnreadableWalletException e1) {
                e1.printStackTrace();
                e.onNext("exception");
            }
            e.onCompleted();
        });
    }

    public LiveData<AccountEntity> getAccountById(int accountId) {
        return ((WalletApp) getApplication()).getRepository().getAccountById(accountId);
    }

    public LiveData<List<TokenEntity>> getTokens() {
        if (mObservableTokens.getValue() == null ||
                mObservableTokens.getValue().size() == 0) {
            LiveData<List<TokenEntity>> tokens = ((WalletApp) getApplication()).getRepository()
                    .getTokens();

            // observe the changes of the tokens from the database and forward them
            // When tokens change, reacquire the total assets.
            mObservableTokens.addSource(tokens, new android.arch.lifecycle.Observer<List<TokenEntity>>() {
                @Override
                public void onChanged(@Nullable List<TokenEntity> value) {
                    mObservableTokens.setValue(value);
                    getTotalAssets();
                }
            });
        }
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
        if (mObservableAssets.getValue() == null ||
                mObservableAssets.getValue().size() == 0) {
            getTotalAssets();
        }
        return mObservableAssets;
    }

    /*
     * Get all the token's assets for all accounts
     */
    public void getTotalAssets() {
        List<AccountEntity> accounts = mObservableAccounts.getValue();
        List<TokenEntity> tokens = mObservableTokens.getValue();
        if (accounts != null && accounts.size() > 0 && tokens != null && tokens.size() > 0) {
            BLog.i("view model", "get account assets success");
            // init the assets
            assetsList = new ArrayList<>();
            for (AccountEntity accountEntity : accounts) {
                if (accountEntity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                    getBtcBalance(accountEntity);
                } else {
                    for (TokenEntity tokenEntity : tokens) {
                        if (!tokenEntity.getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                            getTokenAssetsOnEthereumChain(accountEntity, tokenEntity);
                        }
                    }
                }
            }
        }
    }

    /*
     * Get all the btc assets for all btc accounts
     */
    public void getBtcAssets() {
        List<AccountEntity> accounts = mObservableAccounts.getValue();
        if (accounts != null && accounts.size() > 0) {
            for (AccountEntity accountEntity : accounts) {
                if (accountEntity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                    getBtcBalance(accountEntity);
                }
            }
        }
    }

    /*
     * Get the specified token asset of the specified account
     */
    private void getTokenAssetsOnEthereumChain(final AccountEntity account, final TokenEntity tokenEntity) {
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
                                        tokenEntity.getName() + " balance is " + ethCall.getValue());
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
     * Get the btc balance
     */
    private void getBtcBalance(AccountEntity accountEntity) {
        TokenEntity btcEntity = new TokenEntity();
        btcEntity.setAddress("btc");
        btcEntity.setName("Bitcoin");
        btcEntity.setShortName("BTC");
        WalletAppKit kit = BtcAccountManager.getInstance().getBtcWalletAppKit(accountEntity.getFilename());
        if (kit != null) {
            AccountAssets assets = new AccountAssets(accountEntity, btcEntity,
                    BigInteger.valueOf(kit.wallet().getBalance().value));
            checkTokenAsset(assets);
        } else {
            BtcAccountManager.getInstance().initExistsWalletAppKit(accountEntity);
            AccountAssets assets = new AccountAssets(accountEntity, btcEntity,
                    BigInteger.ZERO);
            checkTokenAsset(assets);
        }
    }

    /*
     * If the token asset of the account has already exists, then replace it with the new assets.
     * When all assets has exists, post value to main page
     */
    private void checkTokenAsset(AccountAssets assets) {
        for (AccountAssets localAssets : assetsList) {
            if (localAssets.getAccountEntity().getType() == assets.getAccountEntity().getType() &&
                    localAssets.getAccountEntity().getType() == BrahmaConst.ETH_ACCOUNT_TYPE &&
                    localAssets.getAccountEntity().getAddress().equals(assets.getAccountEntity().getAddress()) &&
                    localAssets.getTokenEntity().getAddress().equals(assets.getTokenEntity().getAddress())) {
                assetsList.remove(localAssets);
                break;
            } else if (localAssets.getAccountEntity().getType() == assets.getAccountEntity().getType() &&
                    localAssets.getAccountEntity().getType() == BrahmaConst.BTC_ACCOUNT_TYPE &&
                    localAssets.getAccountEntity().getFilename().equals(assets.getAccountEntity().getFilename()) &&
                    localAssets.getTokenEntity().getName().equals(assets.getTokenEntity().getName()) &&
                    localAssets.getTokenEntity().getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
                assetsList.remove(localAssets);
                break;
            }
        }
        assetsList.add(assets);
        if (mObservableAccounts.getValue() != null && mObservableTokens.getValue() != null) {
            int ethAccountCount = 0;
            int btcAccountCount = 0;
            for (AccountEntity account : mObservableAccounts.getValue()) {
                if (account.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                    btcAccountCount++;
                } else if (account.getType() == BrahmaConst.ETH_ACCOUNT_TYPE) {
                    ethAccountCount++;
                }
            }
            int ethTokenCount = mObservableTokens.getValue().size() - 1;
            int totalCount = ethAccountCount * ethTokenCount + btcAccountCount;
            if (totalCount == assetsList.size()) {
                mObservableAssets.postValue(assetsList);
                MainService.getInstance().setAccountAssetsList(assetsList);
            }
        }
    }

    public Completable changeAccountName(int accountId, String newName) {
        return Completable.fromAction(() -> ((WalletApp) getApplication()).getRepository().changeAccountName(accountId, newName));
    }

    /*
     * Change the account password with the private key
     * Due to the wallet file is generated by private key,
     * don't need to check private key;
     * @result the account address,
     */
    public Observable<String> changeAccountPassword(String privateKey, String password, int accountId) {
        return Observable.create(e -> {
            try {
                ECKeyPair ecKeyPair = ECKeyPair.create(Hex.decode(privateKey));
                WalletFile walletFile = Wallet.createLight(password, ecKeyPair);
                String address = BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress());

                SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
                String filename = dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
                File destination = new File(getApplication().getFilesDir(), filename);
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                objectMapper.writeValue(destination, walletFile);
                ((WalletApp) getApplication()).getRepository().changeAccountFilename(accountId, filename);
                e.onNext(address);
            } catch (CipherException | IOException e1) {
                e1.printStackTrace();
                e.onNext("exception");
            }
            e.onCompleted();
        });
    }

    public Completable deleteAccount(int accountId) {
        return Completable.fromAction(() -> ((WalletApp) getApplication()).getRepository().deleteAccount(accountId));
    }

    public LiveData<List<AllTokenEntity>> getAllTokens() {
        return ((WalletApp) getApplication()).getRepository().getAllTokens();
    }

    public LiveData<List<AllTokenEntity>> getShowTokens() {
        return ((WalletApp) getApplication()).getRepository().getShowTokens();
    }

    public LiveData<List<AllTokenEntity>> queryAllTokens(String param) {
        return ((WalletApp) getApplication()).getRepository().queryAllTokens(param);
    }

    public Completable showAllToken(TokenEntity tokenEntity, AllTokenEntity allTokenEntity) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().createToken(tokenEntity);
            ((WalletApp) getApplication()).getRepository().showAllToken(allTokenEntity);
        });
    }

    public Completable hideAllToken(TokenEntity tokenEntity, AllTokenEntity allTokenEntity) {
        return Completable.fromAction(() -> {
            ((WalletApp) getApplication()).getRepository().deleteToken(tokenEntity.getAddress());
            ((WalletApp) getApplication()).getRepository().hideAllToken(allTokenEntity);
        });
    }

    public Observable<List<AllTokenEntity>> queryAllTokensSync(String param) {
        return Observable.create(e -> {
            List<AllTokenEntity> allTokenEntities = ((WalletApp) getApplication()).getRepository().queryAllTokensSync(param);
            e.onNext(allTokenEntities);
            e.onCompleted();
        });
    }

    public Observable<List<TokenEntity>> getChosenTokens() {
        return Observable.create(e -> {
            List<TokenEntity> allChosenTokens = ((WalletApp) getApplication()).getRepository().queryChosenTokensSync();
            e.onNext(allChosenTokens);
            e.onCompleted();
        });
    }

    public LiveData<Integer> getAllTokensCount() {
        return ((WalletApp) getApplication()).getRepository().getAllTokensCount();
    }
}
