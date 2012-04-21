package com.arwave.skywriter.wavecontrol;

import com.arwave.skywriter.R;
import com.arwave.skywriter.start;
import com.arwave.skywriter.R.id;
import com.arwave.skywriter.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class WaveParticipantManager extends Activity {

	String WaveIDbeigManaged ="(no wave specified)";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.participant_list);
	
		Log.i("wpm","_open  participant manager_");
		
		// extract the extra params gotten from caller
		Bundle b = getIntent().getExtras();
		if (b != null) {
			String WaveID = (String) b.get("waveID");
			Log.i("wpm","_for wave:_"+WaveID+"_");
			WaveIDbeigManaged=WaveID;
			
			
		}
		
		//assign autocomplete
		 String[] FriendListArray = start.acm.getFriendList();
		 
		 if (FriendListArray.length==0){
			 FriendListArray = new String[] {"no friends fround :("};
		 }
		 
		 
		 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, FriendListArray);
		 
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.userNameToInvite);
        
        textView.setAdapter(adapter);
		
		Button cancelButton = (Button) findViewById(R.id.FinishUserListActivity);

		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// return to main activity
				finish();
			}
		});
		
		
		Button inviteUser = (Button) findViewById(R.id.InviteUserToWave);

		inviteUser.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				Log.i("wpm","inviteing user to wave:");
        		
				//get user userNameToInvite
				AutoCompleteTextView inviteUserBox = (AutoCompleteTextView) findViewById(R.id.userNameToInvite);
				String usersID = inviteUserBox.getText().toString().trim();
				
				//check it has server extension, if not we add the current one
				if (!usersID.contains("@")){
					
					usersID = usersID+"@"+start.acm.getServerAddress();
					inviteUserBox.setText(usersID);
				}
				//

				Log.i("wpm","inviteing user:"+usersID);
				
				//should have some prechecks here if it could be a valid ID 
				
				//ask the connection manager too add the user
				start.acm.addParticipant(usersID,WaveIDbeigManaged);
				
				//the connection manager should automaticaly update the waves user list itself
				
				
				
				// return to main activity
				finish();
			}
		});
		
	
	}

	public void updateUserList(){
		
	}
	
	
}
