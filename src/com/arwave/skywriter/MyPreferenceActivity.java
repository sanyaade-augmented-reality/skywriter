package com.arwave.skywriter;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class MyPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferances);
    }
    
}