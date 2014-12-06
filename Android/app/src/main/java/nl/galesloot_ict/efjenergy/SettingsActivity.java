package nl.galesloot_ict.efjenergy;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by FlorisJan on 16-11-2014.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}