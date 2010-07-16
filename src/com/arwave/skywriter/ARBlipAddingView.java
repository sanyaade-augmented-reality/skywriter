package com.arwave.skywriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class ARBlipAddingView extends Activity {

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set up the layout
		setContentView(R.layout.add_arblip);
		
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//return to main activity
				finish();
			}
		});
		
		//extract the params gotten from caller
		Bundle b = getIntent().getExtras();
		if( b != null ) {
			String waveID = b.getString("WaveID"); //FIXME: this shouldn't be hardcoded
			EditText out = (EditText)findViewById(R.id.arblipContent);
			out.setText(waveID);
		}
	}

}
