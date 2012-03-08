package com.arwave.skywriter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateWaveActivity extends Activity {
	private Button createWaveButton;
	static private int RoomNameCounter =0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.createnewwave_page);
		((EditText) findViewById(R.id.NewWaveName)).setText("Room #:"+RoomNameCounter);
		
		Log.i("wave", "adding wave");

		Button cancelButton = (Button) findViewById(R.id.CancelWaveCreation);

		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// return to main activity
				finish();
			}
		});
		
		createWaveButton = (Button) findViewById(R.id.CreateNewWave);

		createWaveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//run the create wave function
				// create a new wave if logged in
				
	String waveName = ((EditText) findViewById(R.id.NewWaveName)).getText().toString();

				TextView feedbackBox = ((TextView) findViewById(R.id.WaveCreationFeedback));
				feedbackBox.setText("creating wave called '"+waveName+"'");
				
				
			
				start.createANewWave(waveName);
			
				//update textbox
				RoomNameCounter=RoomNameCounter+1;
				//((EditText) findViewById(R.id.NewWaveName)).setText("Room #:"+RoomNameCounter);
				
				// return to main activity
				finish();
			}
		});
		
		// extract the params gotten from caller
		Bundle b = getIntent().getExtras();
		if (b != null) {
			//do  stuff with b
		}
	}
}