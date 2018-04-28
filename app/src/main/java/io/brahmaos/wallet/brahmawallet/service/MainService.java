package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;

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
}
