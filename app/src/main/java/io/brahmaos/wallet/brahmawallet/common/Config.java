package io.brahmaos.wallet.brahmawallet.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public boolean init(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        localKeystorePath = context.getFilesDir().toString();

        return true;
    }

    public String getLocalKeystorePath() {
        return localKeystorePath;
    }
}
