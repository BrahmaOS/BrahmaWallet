package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.util.BLog;
import rx.Completable;
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
        return true;
    }

    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private List<AccountAssets> accountAssetsList = new ArrayList<>();

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

    public void loadCryptoCurrencies(List<CryptoCurrency> currencies) {
        if (currencies != null) {
            for (CryptoCurrency currency : currencies) {
                for (CryptoCurrency localCurrency : cryptoCurrencies) {
                    if (localCurrency.getId().equals(currency.getId())) {
                        cryptoCurrencies.remove(localCurrency);
                        break;
                    }
                }
                cryptoCurrencies.add(currency);
            }
        }
    }

    public Completable loadAllTokens(List<AllTokenEntity> tokenEntities) {
        return Completable.fromAction(() -> {
            ((WalletApp) context.getApplicationContext()).getRepository().insertAllTokens(tokenEntities);
        });
    }

    public void getTokenList() {
        BrahmaWeb3jService.getInstance()
                .getTokensHash()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String tokenListIpfsHash) {
                        BLog.i(tag(), "the ipfs token hash is:" + tokenListIpfsHash);
                        String localTokenHash = BrahmaConfig.getInstance().getTokenListHash();
                        if (tokenListIpfsHash != null && tokenListIpfsHash.length() > 0 &&
                                !tokenListIpfsHash.equals(localTokenHash)) {
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

                                                            },
                                                            throwable -> {
                                                                BLog.e(tag(), "Unable to check token", throwable);
                                                            });

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }
}
