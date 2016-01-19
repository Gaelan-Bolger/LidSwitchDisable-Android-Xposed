package com.xposed.lidswitchdisable.preference;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.xposed.lidswitchdisable.R;
import com.xposed.lidswitchdisable.Xposed;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String TAG = SettingsFragment.class.getSimpleName();
        private static final String LD_CSS = "body { overflow-wrap: break-word; background-color: #2F2F2F; color: white; }\n" +
                "li { font-size: 1.0em; padding: 5px, 5px, 10px, 5px; }\n" +
                "a { font-size: 0.8em; color: #4da46b; }\n" +
                "pre { font-family: monospace; font-size: 0.8em; background-color: rgba(0,0,0,0.4); padding: 1em; white-space: pre-wrap; }";

        private SharedPreferences mPrefs;

        @SuppressWarnings("deprecation")
        @SuppressLint("WorldReadableFiles")
        @Override
        public void onCreate(Bundle paramBundle) {
            super.onCreate(paramBundle);
            setRetainInstance(true);
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(Xposed.PREFERENCES);
            preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
            mPrefs = preferenceManager.getSharedPreferences();

            addPreferencesFromResource(R.xml.prefs);

            findPreference("openSourceLicenses").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new LicensesDialog.Builder(getActivity())
                            .setThemeResourceId(R.style.AppAlertDialogTheme)
                            .setNoticesCssStyle(LD_CSS)
                            .setNotices(R.raw.notices)
                            .build().show();
                    return true;
                }
            });

            findPreference("appVersion").setSummary(getAppVersionName(getActivity()));
        }

        @Override
        public void onResume() {
            super.onResume();
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            }
            if (key.equals("hideLauncherIcon")) {
                int mode = mPrefs.getBoolean("hideLauncherIcon", false) ?
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                getActivity().getPackageManager()
                        .setComponentEnabledSetting(new ComponentName(getActivity(), "com.xposed.lidswitchdisable.Launcher"),
                                mode, PackageManager.DONT_KILL_APP);
            }
        }

        private String getAppVersionName(Context context) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return info.versionName;
            } catch (Exception e) {
                return "Unknown";
            }
        }

        /**
         * Displays a snackbar message of Snackbar.LENGTH_SHORT.
         */
        private void snack(String message) {
            Snackbar.make(getActivity().findViewById(R.id.coordinator), message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
