package com.arwave.skywriter.wavecontrol;

import com.arwave.skywriter.start;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WaveListView extends ListView {
	
   boolean DataUpdated = false;
   Context meep;
   
	public WaveListView(Context context) {
		super(context);		 
		meep = context;

		this.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				

				//shouldn't be position based - that stops filtering working
				WaveDetails o = (WaveDetails)arg0.getAdapter().getItem(arg2);
				
				
				// one of the wave check boxs has changed,so find out which
				String WaveName = o.name;

				Log.i("wavelist", "changing wave name:" + WaveName);

				// get the id
				String WaveID = WaveList.getWaveIDFromNick(WaveName);

				Log.i("wavelist", "changing wave id:" + WaveID);
				
				 o.visible = !o.visible;
				 
				// set boolean to its checked state
				Boolean isVisible = o.visible;
				Log.i("wavelist", "to:" + isVisible);

				// toggle visibility (only has effect if wave is already open)
				start.arView.setWaveVisiblity(WaveID, isVisible);

				// else we load it, setting it also as the current wave

			}

		});
		
	}
		
	
	/** refreshs the view next time onDraw is called **/
	public void setDataUpdated()
	{
		DataUpdated = true;		
		
	
		
	}
	
	@Override
	protected
	void onDraw(Canvas canvas){		
		
		//update list contents
		
		//Log.i("state", "updateing view");		
		
		if (DataUpdated){
			//in future, we probably want to bring this adapter into this view so we dont need to do this unchecked cast
		((ArrayAdapter<String>)super.getAdapter()).notifyDataSetChanged();
		DataUpdated = false;
		
		
		}
		super.onDraw(canvas);	
		
	}
	
	public void setWaveVisible(int position, boolean state){

		WaveDetails o = (WaveDetails)this.getAdapter().getItem(position);
		
		o.visible = state;
		
		 // Toast.makeText(meep, "wave set to "+state, Toast.LENGTH_LONG).show();
		  
	}

		
		
	
	
/*
	waveListViewBox.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			// one of the wave check boxs has changed,so find out which
			String WaveName = waveListViewBox.getItemAtPosition(arg2)
					.toString(); // <---This really needs to be improved,
									// the waveID should be stored somehow
									// so we can use proper labels

			Log.i("wavelist", "changing wave name:" + WaveName);

			// get the id
			String WaveID = WaveList.getWaveIDFromNick(WaveName);

			Log.i("wavelist", "changing wave id:" + WaveID);

			// set boolean to its checked state
			Boolean isVisible = waveListViewBox.isItemChecked(arg2);
			Log.i("wavelist", "to " + isVisible);

			// toggle visibility (only has effect if wave is already open)
			arView.setWaveVisiblity(WaveID, isVisible);

			// else we load it, setting it also as the current wave

		}

	});*/

}
