package com.h.almog.simpletolive.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.h.almog.simpletolive.R;

/**
 * Created by Almog on 05/04/2016.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private SwitchPreference unitsKmMiles;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_setting);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        unitsKmMiles = (SwitchPreference) findPreference(getResources().getString(R.string.settings_km_or_miles_key));
        unitsKmMiles.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String str;
            str = getResources().getString(R.string.settings_km_or_miles_key);
        if(str.equals(preference.getKey())){
                boolean km = (boolean) newValue;
                if (km) {
                    Toast.makeText(getActivity() , getResources().getString(R.string.km), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.miles), Toast.LENGTH_SHORT).show();
                }
        }
        return true;
    }
}
