package com.arwave.skywriter;

import org.waveprotocol.wave.model.document.operation.AnnotationBoundaryMapBuilder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class ARBlipAddingView extends Activity {

	private AbstractCommunicationManager acm;
	private String waveID;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set up the layout
		setContentView(R.layout.add_arblip);
		//extract the params gotten from caller
		Bundle b = getIntent().getExtras();
		if( b != null ) {
			waveID = b.getString("WaveID"); //FIXME: this shouldn't be hardcoded
			//EditText out = (EditText)findViewById(R.id.arblipContent);
			//out.setText(waveID);
		}
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//return to main activity
				finish();
			}
		});
		
		Button addButton = (Button)findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//check if a wave was actually selected (paranoid?)
				if( waveID != null ) {
					acm = FedOneCommunicationManager.getFedOneCommunicationManager();
					//should be done with annotations in the future
					//AnnotationBoundaryMapBuilder annotations = new AnnotationBoundaryMapBuilder();
					//annotations.change( "LAT", "", ((EditText)findViewById(R.id.altitude)).getText().toString() );
					StringBuilder sb = new StringBuilder();
					//read latitude
					sb.append( ((EditText)findViewById(R.id.latitude)).getText().toString() );
					sb.append("#");
					//read longitude
					sb.append( ((EditText)findViewById(R.id.longitude)).getText().toString() );
					sb.append("#");
					//read altitude
					sb.append( ((EditText)findViewById(R.id.altitude)).getText().toString() );
					sb.append("#");
					//read content text
					sb.append( ((EditText)findViewById(R.id.arblipContent)).getText().toString() );
					
					//actually append the new blip
					acm.addARBlip( waveID, sb.toString() );
				}
				finish();
			}
		});
		
	}

}
