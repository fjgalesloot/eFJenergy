package nl.galesloot_ict.efjenergy.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import nl.galesloot_ict.efjenergy.R;
import nl.galesloot_ict.efjenergy.SettingsActivity;

/**
 * Created by FlorisJan on 4-12-2014.
 */
public class APIHelper {

    private SharedPreferences sharedPref;

    private String GetUrl() {
        String retval="";

        Boolean useSSL = sharedPref.getBoolean("pref_key_api_ssl",false);
        String hostname = sharedPref.getString("pref_key_api_host", null);
        String port = sharedPref.getString("pref_key_api_port","");
        if ( port.isEmpty() ) {
            port = useSSL ? "443" : "80";
        }
        String path = sharedPref.getString("pref_key_api_url","");

        if ( !path.isEmpty() ) {
            if ( path.startsWith("/") ) {
                path = path.substring(1);
            }
            if ( !path.endsWith("/")) {
                path = path + "/";
            }
        }

        retval = useSSL ? "https://" : "http://";
        retval += hostname + ":" + port + "/" + path + "api/v1/";


        return retval;
    }
    public String GetUrl(String method) {
        String url = GetUrl();
        url = url + method + "?apiKey=" + sharedPref.getString("pref_key_api_key","");
        return url;
    }

    public APIHelper(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }
}
