package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.database.WalletDatabase;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.model.KyberToken;
import io.brahmaos.wallet.brahmawallet.model.TokensVersionInfo;
import io.brahmaos.wallet.brahmawallet.repository.DataRepository;
import io.brahmaos.wallet.util.BLog;
import io.rayup.sdk.RayUpApp;
import io.rayup.sdk.model.Coin;
import io.rayup.sdk.model.CoinQuote;
import io.rayup.sdk.model.EthToken;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainService extends BaseService{
    @Override
    protected String tag() {
        return MainService.class.getName();
    }

    // singleton
    private static MainService instance = new MainService();
    public static MainService getInstance() {
        return instance;
    }

    @Override
    public boolean init(Context context) {
        super.init(context);
        BrahmaWeb3jService.getInstance().init(context);
        TransactionService.getInstance().init(context);
        BtcAccountManager.getInstance().init(context);
        return true;
    }

    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private List<AccountAssets> accountAssetsList = new ArrayList<>();
    private List<KyberToken> kyberTokenList = new ArrayList<>();
    private List<String> mnemonicCode = new ArrayList<>();
    private boolean isHaveAccount;

    public List<CryptoCurrency> getCryptoCurrencies() {
        return cryptoCurrencies;
    }

    public void setCryptoCurrencies(List<CryptoCurrency> cryptoCurrencies) {
        this.cryptoCurrencies = cryptoCurrencies;
    }

    public List<AccountAssets> getAccountAssetsList() {
        return accountAssetsList;
    }

    public void setAccountAssetsList(List<AccountAssets> accountAssetsList) {
        this.accountAssetsList = accountAssetsList;
    }

    public List<KyberToken> getKyberTokenList() {
        return kyberTokenList;
    }

    public void setKyberTokenList(List<KyberToken> kyberTokenList) {
        this.kyberTokenList = kyberTokenList;
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }

    public void setMnemonicCode(List<String> mnemonicCode) {
        this.mnemonicCode = mnemonicCode;
    }

    public void loadCryptoCurrencies(List<CryptoCurrency> currencies) {
        if (currencies != null) {
            for (CryptoCurrency currency : currencies) {
                for (CryptoCurrency localCurrency : cryptoCurrencies) {
                    if (localCurrency.getTokenAddress().toLowerCase().equals(currency.getTokenAddress().toLowerCase())) {
                        cryptoCurrencies.remove(localCurrency);
                        break;
                    }
                }
                cryptoCurrencies.add(currency);
            }
        }
    }

    public boolean isHaveAccount() {
        return isHaveAccount;
    }

    public void setHaveAccount(boolean haveAccount) {
        isHaveAccount = haveAccount;
    }

    public Completable loadAllTokens(List<AllTokenEntity> tokenEntities) {
        return Completable.fromAction(() -> {
            ((WalletApp) context.getApplicationContext()).getRepository().insertAllTokens(tokenEntities);
        });
    }

    /*
     * Fetch token price
     */
    public Observable<List<CryptoCurrency>> fetchCurrenciesFromNet(String symbols) {
        return Observable.create(e -> {
            RayUpApp app = ((WalletApp) context.getApplicationContext()).getRayUpApp();
            Map<Integer, Map<String, CoinQuote>> coinQuotes = app.getLatestCoinQuotesByCodeV2(symbols, "USD,CNY");
            Iterator<Map.Entry<Integer, Map<String, CoinQuote>>> iterator = coinQuotes.entrySet().iterator();

            List<CryptoCurrency> currencies = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Map<String, CoinQuote>> entry = iterator.next();
                CryptoCurrency cryptoCurrency = new CryptoCurrency();
                Object coinCodeKey = entry.getKey();
                int coinCode = Integer.parseInt((String)coinCodeKey);
                if (coinCode == BrahmaConst.COIN_CODE_BRM) {
                    cryptoCurrency.setName(BrahmaConst.COIN_BRM);
                    cryptoCurrency.setSymbol(BrahmaConst.COIN_SYMBOL_BRM);
                    cryptoCurrency.setTokenAddress(BrahmaConst.COIN_BRM_ADDRESS);
                    if (entry.getValue().get("USD") != null) {
                        cryptoCurrency.setPriceUsd((Double)((Map<String, Object>)entry.getValue().get("USD")).get("price"));
                    }
                    if (entry.getValue().get("CNY") != null) {
                        cryptoCurrency.setPriceCny((Double)((Map<String, Object>)entry.getValue().get("CNY")).get("price"));
                    }
                } else if (coinCode == BrahmaConst.COIN_CODE_ETH) {
                    cryptoCurrency.setName(BrahmaConst.COIN_ETH);
                    cryptoCurrency.setSymbol(BrahmaConst.COIN_SYMBOL_ETH);
                    cryptoCurrency.setTokenAddress(BrahmaConst.COIN_ETH_ADDRESS);
                    if (entry.getValue().get("USD") != null) {
                        cryptoCurrency.setPriceUsd((Double)((Map<String, Object>)entry.getValue().get("USD")).get("price"));
                    }
                    if (entry.getValue().get("CNY") != null) {
                        cryptoCurrency.setPriceCny((Double)((Map<String, Object>)entry.getValue().get("CNY")).get("price"));
                    }
                } else if (coinCode == BrahmaConst.COIN_CODE_BTC) {
                    cryptoCurrency.setName(BrahmaConst.COIN_BTC);
                    cryptoCurrency.setSymbol(BrahmaConst.COIN_SYMBOL_BTC);
                    cryptoCurrency.setTokenAddress("btc");
                    if (entry.getValue().get("USD") != null) {
                        cryptoCurrency.setPriceUsd((Double)((Map<String, Object>)entry.getValue().get("USD")).get("price"));
                    }
                    if (entry.getValue().get("CNY") != null) {
                        cryptoCurrency.setPriceCny((Double)((Map<String, Object>)entry.getValue().get("CNY")).get("price"));
                    }
                } else {
                    TokenEntity tokenEntity = ((WalletApp) context.getApplicationContext()).getRepository().getTokenByCode(coinCode);
                    cryptoCurrency.setName(tokenEntity.getName());
                    cryptoCurrency.setSymbol(tokenEntity.getShortName());
                    cryptoCurrency.setTokenAddress(tokenEntity.getAddress());
                    if (entry.getValue().get("USD") != null) {
                        cryptoCurrency.setPriceUsd((Double)((Map<String, Object>)entry.getValue().get("USD")).get("price"));
                    }
                    if (entry.getValue().get("CNY") != null) {
                        cryptoCurrency.setPriceCny((Double)((Map<String, Object>)entry.getValue().get("CNY")).get("price"));
                    }
                }
                currencies.add(cryptoCurrency);
            }

            loadCryptoCurrencies(currencies);
            e.onNext(cryptoCurrencies);
            e.onCompleted();
        });
    }

    /*
     * Get latest erc20 tokens
     */
    public Observable<List<EthToken>> getLatestTokenList() {
        return Observable.create(e -> {
            RayUpApp app = ((WalletApp) context.getApplicationContext()).getRayUpApp();
            List<EthToken> coins = app.loadEthErc20Tokens(0, BrahmaConst.COIN_COUNT);
            BLog.d(tag(), "the size is:" + coins.size());

            // Add coinCode for old version
            DataRepository dataRepository = ((WalletApp) context.getApplicationContext()).getRepository();
            List<TokenEntity> allChosenTokens = dataRepository.queryChosenTokensSync();
            if (allChosenTokens.size() > 0) {
                TokenEntity tokenEntity = allChosenTokens.get(0);
                if (tokenEntity.getCode() <= 0) {
                    dataRepository.deleteAllChooseTokens();
                    TokenEntity brmToken = new TokenEntity();
                    brmToken.setName("BrahmaOS");
                    brmToken.setShortName("BRM");
                    brmToken.setAddress("0xd7732e3783b0047aa251928960063f863ad022d8");
                    brmToken.setCode(BrahmaConst.COIN_CODE_BRM);
                    dataRepository.createToken(brmToken);

                    TokenEntity ethToken = new TokenEntity();
                    ethToken.setName("Ethereum");
                    ethToken.setShortName("ETH");
                    ethToken.setAddress("");
                    ethToken.setCode(BrahmaConst.COIN_CODE_ETH);
                    dataRepository.createToken(ethToken);

                    TokenEntity btcToken = new TokenEntity();
                    btcToken.setName("Bitcoin");
                    btcToken.setShortName("BTC");
                    btcToken.setAddress("btc");
                    btcToken.setCode(BrahmaConst.COIN_CODE_BTC);
                    dataRepository.createToken(btcToken);
                }
            }

            List<AllTokenEntity> allTokenEntities = new ArrayList<>();
            // add BRM and ETH
            AllTokenEntity ethToken = new AllTokenEntity(0, "Ethereum", "ETH",
                    "", "", BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG, BrahmaConst.COIN_CODE_ETH);
            AllTokenEntity brmToken = new AllTokenEntity(0, "BrahmaOS", "BRM",
                    "0xd7732e3783b0047aa251928960063f863ad022d8", "", BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG, BrahmaConst.COIN_CODE_BRM);
            allTokenEntities.add(brmToken);
            allTokenEntities.add(ethToken);
            for (EthToken coin : coins) {
                AllTokenEntity tokenEntity = new AllTokenEntity();
                tokenEntity.setAddress(coin.getAddress());
                tokenEntity.setShortName(coin.getSymbol());
                tokenEntity.setName(coin.getName());
                tokenEntity.setCode(coin.getCoinCode());
                tokenEntity.setAvatar(coin.getLogo());
                if (coins.indexOf(coin) < BrahmaConst.DEFAULT_TOKEN_COUNT) {
                    tokenEntity.setShowFlag(BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG);
                }
                if (coin.getCoinCode() != BrahmaConst.COIN_CODE_ETH &&
                        coin.getCoinCode() != BrahmaConst.COIN_CODE_BRM) {
                    allTokenEntities.add(tokenEntity);
                }

            }
            BLog.i(tag(), "the result:" + allTokenEntities.size());

            MainService.getInstance()
                    .loadAllTokens(allTokenEntities)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                e.onNext(coins);
                                e.onCompleted();
                            },
                            throwable -> {
                                BLog.e(tag(), "Unable to check token", throwable);
                                e.onError(throwable);
                                e.onCompleted();
                            });


        });
    }

    /*
     * Get kyber tokens
     */
    public Observable<List<KyberToken>> getKyberTokens() {
        return Observable.create(e -> {
            Networks.getInstance().getKyperApi()
                    .getKyberPairsTokens()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<LinkedHashMap<String, Object>>() {
                        @Override
                        public void onCompleted() {
                            e.onCompleted();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            e.onError(throwable);
                        }

                        @Override
                        public void onNext(LinkedHashMap<String, Object> apr) {
                            if (apr != null) {
                                BLog.i(tag(), apr.toString());
                                kyberTokenList.clear();
                                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                for(Map.Entry<String, Object> entry: apr.entrySet()){
                                    try {
                                        KyberToken kyberToken = objectMapper.readValue(objectMapper.writeValueAsString(entry.getValue()), new TypeReference<KyberToken>() {});
                                        kyberTokenList.add(kyberToken);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Collections.sort(kyberTokenList);
                                e.onNext(kyberTokenList);
                            }
                        }
                    });
        });
    }

    /*
     * Get accounts
     */
    public Observable<List<AccountEntity>> getAccounts() {
        return Observable.create(e -> {
            List<AccountEntity> accounts = ((WalletApp) context.getApplicationContext()).getRepository().getAccountsSync();
            e.onNext(accounts);
            e.onCompleted();
        });
    }
}
