package com.arwave.skywriter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InviteRecievedActivity extends Activity {
	private Button acceptInviteButton;
	private String wid="not set";
	private String invitor="not set";
	
	//static private int RoomNameCounter =0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.invite_recieved_activity);
		((TextView) findViewById(R.id.InviteSourceText)).setText("(source here)");
		
		Log.i("wave", "adding wave");

		Button cancelButton = (Button) findViewById(R.id.CancelInvite);

		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// return to main activity
				finish();
			}
		});
		
		acceptInviteButton = (Button) findViewById(R.id.AcceptInvite);

		acceptInviteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//run the join wave function
				
				
			
				start.joinWave(wid,wid);
			
				// return to main activity
				finish();
			}
		});
		
		// extract the params gotten from caller
		Bundle b = getIntent().getExtras();
		if (b != null) {
			//do  stuff with b
			wid= b.getString("waveid");
			invitor= b.getString("invitor");
			
		}
	}
}