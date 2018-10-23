package io.brahmaos.wallet.brahmawallet.ui.setting;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.ui.MainActivity;
import io.brahmaos.wallet.util.CommonUtil;

/**
 * Activities that contain this fragment must implement the PreferenceFragment
 */
public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ethereum network
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_network_url)));
        Preference networkUrl = findPreference(getString(R.string.key_network_url));
        networkUrl.setOnPreferenceChangeListener((preference, value) -> {
            BrahmaConfig.getInstance().setNetworkUrl(value.toString());
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_network_url)));
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(IntentParam.FLAG_CHANGE_NETWORK, true);
            startActivity(intent);
            return true;
        });

        // Bitcoin network
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_btc_network_url)));
        Preference btcNetwork = findPreference(getString(R.string.key_btc_network_url));
        btcNetwork.setOnPreferenceChangeListener((preference, value) -> {
            Log.i("Setting", "==> select - value:" + value);
            BrahmaConfig.getInstance().setBtcNetworkFlag(value.toString());
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_btc_network_url)));
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(IntentParam.FLAG_CHANGE_BTC_NETWORK, true);
            startActivity(intent);
            return true;
        });

        Preference languageLocale = findPreference(getString(R.string.key_wallet_language));
        languageLocale.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(languageLocale,
                PreferenceManager
                        .getDefaultSharedPreferences(languageLocale.getContext())
                        .getString(languageLocale.getKey(), BrahmaConfig.getInstance().getLanguageLocale()));
        languageLocale.setOnPreferenceChangeListener((preference, value) -> {
            // change SharedPreferences
            BrahmaConfig.getInstance().setLanguageLocale(value.toString());
            // change language locale
            BrahmaConfig.getInstance().setLocale();
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_wallet_language)));
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(IntentParam.FLAG_CHANGE_LANGUAGE, true);
            startActivity(intent);
            return true;
        });

        Preference currencyUnit = findPreference(getString(R.string.key_wallet_currency_unit));
        currencyUnit.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(currencyUnit,
                PreferenceManager
                        .getDefaultSharedPreferences(currencyUnit.getContext())
                        .getString(currencyUnit.getKey(), BrahmaConfig.getInstance().getCurrencyUnit()));
        currencyUnit.setOnPreferenceChangeListener((preference, value) -> {
            BrahmaConfig.getInstance().setCurrencyUnit(value.toString());
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_wallet_currency_unit)));
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(IntentParam.FLAG_CHANGE_CURRENCY_UNIT, true);
            startActivity(intent);
            return true;
        });

        Preference touchId = findPreference(getString(R.string.key_touch_id_switch));
        if (!CommonUtil.isFinger(getActivity())) {
            touchId.setEnabled(false);
        }
        touchId.setOnPreferenceChangeListener((preference, value) -> {
            BrahmaConfig.getInstance().setTouchId((Boolean) value);
            return true;
        });
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };



    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
