package com.arwave.skywriter.wavecontrol;

import com.arwave.skywriter.start;

import android.app.ListActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class WaveListActivity extends ListActivity {

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		WaveDetails o = (WaveDetails)this.getListAdapter().getItem(position);
		
		// one of the wave check boxs has changed,so find out which
		String WaveName = o.name;
		
		Log.i("wavelist", "changing wave name:" + WaveName);

		// get the id (in future get it straight from the details)
		String WaveID = WaveList.getWaveIDFromNick(WaveName);

		Log.i("wavelist", "changing wave id:" + WaveID);

		 o.visible = !o.visible;
		 
		// set boolean to its checked state
		Boolean isVisible = o.visible;
		Log.i("wavelist", "to " + isVisible);

		// toggle visibility (only has effect if wave is already open)
		start.arView.setWaveVisiblity(WaveID, isVisible);

		// else we load it, setting it also as the current wave

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
