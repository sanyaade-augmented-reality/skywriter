package com.arwave.skywriter.wavecontrol;

import com.arwave.skywriter.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/** activity used to customise the settings for a particular wave **/
public class WavePreferances extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras(); 
        //get the ID
        if(extras !=null)
        {
        String itemId = extras.getString("waveID");

        
        String prefsName = "waveID_" + itemId;
        Log.i("wavepref", "saving too;"+prefsName);
        
        PreferenceManager prefManager = getPreferenceManager();
        prefManager.setSharedPreferencesName(prefsName);
        

        addPreferencesFromResource(R.xml.wavepreferances);

        
        
        
       /*
        Bundle extras = getIntent().getExtras(); 
        //get the ID
        if(extras !=null)
        {
        final String value = extras.getString("waveID");
        
        //Amend the wave ID to the start of all preferences
        Log.i("wavepref", "got ID from parent activity"+value);
        
        Preference BillboardScaleing = findPreference("WaveID_BillBoard_Scaleing");
        
        Log.i("wavepref", "BillboardScaleing set to "+this.getPreferences(MODE_WORLD_READABLE).getString(value+"_BillBoard_Scaleing", "no value set"));
        Log.i("wavepref", "BillboardScaleing set to "+this.getPreferences(MODE_WORLD_READABLE).getInt(value+"_BillBoard_Scaleing", 55));

        
        //set up listeners for committing;
        BillboardScaleing.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				  Log.i("wavepref", "preferance"+preference.getKey()+" set to "+newValue.toString());
				  				
			        Log.i("wavepref", "BillboardScaleing set to "+preference.getSharedPreferences().getString(value+"_BillBoard_Scaleing", "no value set"));
			        Log.i("wavepref", "BillboardScaleing set to "+preference.getSharedPreferences().getInt(value+"_BillBoard_Scaleing", 55));

			        
				  
				return false;
			}
        	
        });
        
        this.findPreference("WaveID_ShowByDefault").setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				
				  Log.i("wavepref", "preferance"+preference.getKey()+" set to "+newValue.toString());
				  //preference.getEditor().commit();
				  //preference.setPersistent(true);
				  
				  
				  
				return false;
			}
        	
        });
        
      //amending code goes here
        
        //set the key
        BillboardScaleing.setKey(value+"_BillBoard_Scaleing");
        //get existing value, if it exists
        
        
        this.findPreference("WaveID_ShowByDefault").setKey(value+"_ShowByDefault");
        
        this.findPreference(value+"_BillBoard_Scaleing").setPersistent(true);
        
        
        } else {
        	//exit! Because that ID needs to exist
        }
        */
    }
}};

