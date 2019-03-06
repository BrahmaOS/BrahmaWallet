package io.brahmaos.wallet.brahmawallet.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Locale;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticEventAgent;

/**
 * the project common config
 */

public class BrahmaConfig {

    private static BrahmaConfig instance = new BrahmaConfig();
    public static BrahmaConfig getInstance() {
        return instance;
    }

    private Context context;
    private SharedPreferences sharedPref = null;
    private static final String FIRST_OPEN_APP_FLAG = "new.first.open.app.flag";
    private static final String KEY_ASSETS_VISIBLE = "assets.visible";
    private static final String KEY_TOKEN_LIST_HASH = "token.list.hash";
    private static final String KEY_TOKEN_LIST_VERSION = "token.list.version";
    private static final String KEY_PAY_REQUEST_TOKEN = "pay.request.token";

    // false: main net; true: ropsten testnet;
    public static boolean debugFlag = true;//false;
    private String networkUrl;
    private String languageLocale;
    private String currencyUnit;
    private String payRequestToken;
    private boolean assetsVisible = true;
    private String tokenListHash;
    private boolean touchId = false;
    private int tokenListVersion = 0;
    private boolean allowStatistic = true;

    public void init(Context context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Ethereum network
        if (debugFlag) {
            networkUrl = sharedPref.getString(context.getString(R.string.key_network_url), BrahmaConst.ROPSTEN_TEST_URL);
        } else {
            networkUrl = sharedPref.getString(context.getString(R.string.key_network_url), BrahmaConst.MAINNET_URL);
        }

        languageLocale = sharedPref.getString(context.getString(R.string.key_wallet_language), null);
        currencyUnit = sharedPref.getString(context.getString(R.string.key_wallet_currency_unit), null);
        payRequestToken = sharedPref.getString(KEY_PAY_REQUEST_TOKEN, null);
        assetsVisible = sharedPref.getBoolean(KEY_ASSETS_VISIBLE, true);
        tokenListHash = sharedPref.getString(KEY_TOKEN_LIST_HASH, "");
        touchId = sharedPref.getBoolean(context.getString(R.string.key_touch_id_switch), false);
        tokenListVersion = sharedPref.getInt(KEY_TOKEN_LIST_VERSION, 0);
        allowStatistic = sharedPref.getBoolean(context.getString(R.string.key_statistic_switch), true);
        initLocale();
    }

    public String getNetworkUrl() {
        return networkUrl;
    }

    public void setNetworkUrl(String networkUrl) {
        this.networkUrl = networkUrl;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.key_network_url), networkUrl);
        editor.apply();
    }

    public String getLanguageLocale() {
        return languageLocale;
    }

    public void setLanguageLocale(String languageLocale) {
        this.languageLocale = languageLocale;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.key_wallet_language), languageLocale);
        editor.apply();
    }

    public String getCurrencyUnit() {
        return currencyUnit;
    }

    public void setCurrencyUnit(String currencyUnit) {
        this.currencyUnit = currencyUnit;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.key_wallet_currency_unit), currencyUnit);
        editor.apply();
    }

    public void initLocale() {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale newLocale = Locale.getDefault();
        if (languageLocale == null) {
            if (newLocale.getLanguage().equals("zh")) {
                languageLocale = BrahmaConst.LANGUAGE_CHINESE;
            } else {
                languageLocale = BrahmaConst.LANGUAGE_ENGLISH;
            }
        }
        if (languageLocale.equals(BrahmaConst.LANGUAGE_CHINESE)) {
            newLocale = Locale.CHINESE;
        } else {
            newLocale = Locale.ENGLISH;
        }
        config.setLocale(newLocale);
        resources.updateConfiguration(config, dm);

        if (currencyUnit == null) {
            if (newLocale.equals(Locale.CHINESE)) {
                currencyUnit = BrahmaConst.UNIT_PRICE_CNY;
            } else {
                currencyUnit = BrahmaConst.UNIT_PRICE_USD;
            }
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.key_wallet_language), languageLocale);
        editor.putString(context.getString(R.string.key_wallet_currency_unit), currencyUnit);
        editor.apply();
    }

    public void setLocale() {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale newLocale = Locale.getDefault();
        if (languageLocale == null) {
            if (newLocale.getLanguage().equals("zh")) {
                languageLocale = BrahmaConst.LANGUAGE_CHINESE;
            } else {
                languageLocale = BrahmaConst.LANGUAGE_ENGLISH;
            }
        }
        if (languageLocale.equals(BrahmaConst.LANGUAGE_CHINESE)) {
            newLocale = Locale.CHINESE;
        } else {
            newLocale = Locale.ENGLISH;
        }

        config.setLocale(newLocale);
        resources.updateConfiguration(config, dm);
    }

    public String getPayRequestToken() {
        return payRequestToken;
    }

    public void setPayRequestToken(String payRequestToken) {
        this.payRequestToken = payRequestToken;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_PAY_REQUEST_TOKEN, payRequestToken);
        editor.apply();
    }

    public boolean isAssetsVisible() {
        return assetsVisible;
    }

    public void setAssetsVisible(boolean assetsVisible) {
        this.assetsVisible = assetsVisible;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(KEY_ASSETS_VISIBLE, assetsVisible);
        editor.apply();
    }

    public String getTokenListHash() {
        return tokenListHash;
    }

    public void setTokenListHash(String tokenListHash) {
        this.tokenListHash = tokenListHash;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_TOKEN_LIST_HASH, tokenListHash);
        editor.apply();
    }

    public String getServiceTermsUrl() {
        String serviceUrl = BrahmaConst.PAGE_BASE_URL + BrahmaConst.HELP_PREFIX + "service_en.html";
        if (languageLocale.equals(BrahmaConst.LANGUAGE_CHINESE)) {
            serviceUrl = BrahmaConst.PAGE_BASE_URL + BrahmaConst.HELP_PREFIX + "service_zh.html";
        }
        return serviceUrl;
    }

    public String getPrivacyUrl() {
        String serviceUrl = BrahmaConst.PAGE_BASE_URL + BrahmaConst.HELP_PREFIX + "privacy_policy_en.html";
        if (languageLocale.equals(BrahmaConst.LANGUAGE_CHINESE)) {
            serviceUrl = BrahmaConst.PAGE_BASE_URL + BrahmaConst.HELP_PREFIX + "privacy_policy_zh.html";
        }
        return serviceUrl;
    }

    public String getHelpUrl() {
        String serviceUrl = BrahmaConst.PAGE_BASE_URL + BrahmaConst.HELP_PREFIX + "help_en.html";
        if (languageLocale.equals(BrahmaConst.LANGUAGE_CHINESE)) {
            serviceUrl = BrahmaConst.PAGE_BASE_URL + BrahmaConst.HELP_PREFIX + "help_zh.html";
        }
        return serviceUrl;
    }

    public boolean isTouchId() {
        return touchId;
    }

    public boolean isStatisticAllowed() {
        return allowStatistic;
    }

    public boolean getTouchIDPayState(String accountAddr) {
        // Touch ID pay is default closed.
        return sharedPref.getBoolean(accountAddr, false);
    }

    public void setTouchId(boolean touchId) {
        this.touchId = touchId;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.key_touch_id_switch), touchId);
        editor.apply();
    }

    public void setStatistic(boolean allow) {
        this.allowStatistic = allow;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.key_statistic_switch), allow);
        editor.apply();
        StatisticEventAgent.allowStatistic(context, allow);
    }

    public void setTouchIDPayState(String accountAddr, boolean state) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(accountAddr, state);
        editor.apply();
    }

    public int getTokenListVersion() {
        return tokenListVersion;
    }

    public void setTokenListVersion(int tokenListVersion) {
        this.tokenListVersion = tokenListVersion;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_TOKEN_LIST_VERSION, tokenListVersion);
        editor.apply();
    }

    public String getEtherscanTxsUrl(AccountEntity mAccount, TokenEntity mToken) {
        String etherscanUrl = BrahmaConst.ETHERSCAN_BASE_URL + "address/" + mAccount.getAddress().toLowerCase();
        if (!mToken.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
            etherscanUrl += "#tokentxns";
        }
        return etherscanUrl;
    }

    public String getEtherscanTxDetailUrl(String txHash) {
        return BrahmaConst.ETHERSCAN_BASE_URL + "tx/" + txHash;
    }

    public String getBlochchainTxDetailUrl(String txHash) {
        return BrahmaConst.BLOCKCHAIN_BASE_URL + txHash;
    }

    public String getFeedbackUrl() {
        return BrahmaConst.FEEDBACK_URL;
    }

    public String getHashRateUrl() {
        return BrahmaConst.HASH_RATE_URL;
    }
}
