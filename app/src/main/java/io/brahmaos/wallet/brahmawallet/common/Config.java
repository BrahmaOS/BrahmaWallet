package io.brahmaos.wallet.brahmawallet.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;

/**
 * the project common config
 */

public class Config {

    private static Config instance = new Config();
    public static Config getInstance() {
        return instance;
    }

    private SharedPreferences sharedPref = null;
    private static final String FIRST_OPEN_APP_FLAG = "new.first.open.app.flag";

    // first user app, show the guide
    private boolean firstOpenAppFlag = true;

    public String localKeystorePath;
    private List<TokenEntity> tokenEntities = new ArrayList<>();

    public boolean init(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        localKeystorePath = context.getFilesDir().toString();
        initTokens();
        return true;
    }

    private void initTokens() {
        tokenEntities.add(new TokenEntity(0, "BrahmaOS", "BRM",
                "0xb958c57d1896823b8f4178a21e1bf6796371eac4", R.drawable.icon_brm));
        tokenEntities.add(new TokenEntity(0, "Ethereum", "ETH",
                "", R.drawable.icon_eth));
        tokenEntities.add(new TokenEntity(0, "EOS", "EOS",
                "0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0", R.drawable.icon_eos));
        tokenEntities.add(new TokenEntity(0, "Tronix", "TRX",
                "0xf230b790e05390fc8295f4d3f60332c93bed42e2", R.drawable.icon_trx));
        tokenEntities.add(new TokenEntity(0, "VeChain", "VEN",
                "0xd850942ef8811f2a866692a623011bde52a462c1", R.drawable.icon_ven));
        tokenEntities.add(new TokenEntity(0, "OmiseGO", "OMG",
                "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07", R.drawable.icon_omg));
        tokenEntities.add(new TokenEntity(0, "BNB", "BNB",
                "0xB8c77482e45F1F44dE1745F52C74426C631bDD52", R.drawable.icon_bnb));
        tokenEntities.add(new TokenEntity(0, "ICON", "ICX",
                "0xb5a5f22694352c15b00323844ad545abb2b11028", R.drawable.icon_icx));
        tokenEntities.add(new TokenEntity(0, "Bytom", "BTM",
                "0xcb97e65f07da24d46bcdd078ebebd7c6e6e3d750", R.drawable.icon_bytom));
        tokenEntities.add(new TokenEntity(0, "Populous", "PPT",
                "0xd4fa1460f537bb9085d22c7bccb5dd450ef28e3a", R.drawable.icon_ppt));
        tokenEntities.add(new TokenEntity(0, "ZRX", "ZRX",
                "0xe41d2489571d322189246dafa5ebde1f4699f498", R.drawable.icon_zrx));
        tokenEntities.add(new TokenEntity(0, "DGD", "DGD",
                "0xe0b7927c4af23765cb51314a0e0521a9645f0e2a", R.drawable.icon_dgd));
        tokenEntities.add(new TokenEntity(0, "Zilliqa", "ZIL",
                "0x05f4a42e251f2d52b8ed15e9fedaacfcef1fad27", R.drawable.icon_zil));
        tokenEntities.add(new TokenEntity(0, "Maker", "MKR",
                "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2", R.drawable.icon_mkr));
        tokenEntities.add(new TokenEntity(0, "StatusNetwork", "SNT",
                "0x744d70fdbe2ba4cf95131626614a1763df805b9e", R.drawable.icon_snt));
        tokenEntities.add(new TokenEntity(0, "RHOC", "RHOC",
                "0x168296bb09e24a88805cb9c33356536b980d3fc5", R.drawable.icon_rhoc));
        tokenEntities.add(new TokenEntity(0, "Aeternity", "AE",
                "0x5ca9a71b1d01849c0a95490cc00559717fcf0d1d", R.drawable.icon_ae));
        tokenEntities.add(new TokenEntity(0, "AION", "AION",
                "0x4CEdA7906a5Ed2179785Cd3A40A69ee8bc99C466", R.drawable.icon_aion));
        tokenEntities.add(new TokenEntity(0, "Loopring", "LRC",
                "0xef68e7c694f40c8202821edf525de3782458639f", R.drawable.icon_lrc));
        tokenEntities.add(new TokenEntity(0, "Golem", "GNT",
                "0xa74476443119A942dE498590Fe1f2454d7D4aC0d", R.drawable.icon_gnt));
        tokenEntities.add(new TokenEntity(0, "Walton", "WTC",
                "0xb7cb1c96db6b22b0d3d9536e0108d062bd488f74", R.drawable.icon_wtc));
        tokenEntities.add(new TokenEntity(0, "REP", "REP",
                "0xe94327d07fc17907b4db788e5adf2ed424addff6", R.drawable.icon_rep));
        /*tokenEntities.add(new TokenEntity(0, "BrahmaOS", "BRM(TEST)",
                "0xb958c57d1896823b8f4178a21e1bf6796371eac4", R.drawable.icon_eth));*/
    }

    public String getLocalKeystorePath() {
        return localKeystorePath;
    }

    public List<TokenEntity> getTokenEntities() {
        return tokenEntities;
    }
}
