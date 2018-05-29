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
import android.view.View;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.ui.MainActivity;

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
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_network_url)));
        //bindPreferenceSummaryToValue(findPreference(getString(R.string.key_wallet_language)));

        Preference networkUrl = findPreference(getString(R.string.key_network_url));
        networkUrl.setOnPreferenceChangeListener((preference, value) -> {
            BrahmaConfig.getInstance().setNetworkUrl(value.toString());
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_network_url)));
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(IntentParam.FLAG_CHANGE_NETWORK, true);
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

        /*findPreference("three").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "再点一下试试", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        findPreference("four").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "试试就试试", Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
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

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("silent");

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
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
