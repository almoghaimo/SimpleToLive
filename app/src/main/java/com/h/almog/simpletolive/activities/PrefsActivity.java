package com.h.almog.simpletolive.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.h.almog.simpletolive.R;
import com.h.almog.simpletolive.fragments.SettingsFragment;

/*
// ===> THE ACTIVITY HOLD AND USE THE PREFERENCE FRAGMENT
 */

public class PrefsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        getFragmentManager().beginTransaction().add(R.id.containerPrefs, new SettingsFragment()).addToBackStack("prefs").commit();

    }
}
