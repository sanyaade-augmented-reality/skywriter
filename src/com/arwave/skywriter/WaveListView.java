package com.arwave.skywriter;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WaveListView extends ListView {
	
   boolean DataUpdated = false;

	public WaveListView(Context context) {
		super(context);		 
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
		
		Log.i("state", "updateing view");		
		
		if (DataUpdated){
			//in future, we probably want to bring this adapter into this view so we dont need to do this unchecked cast
		((ArrayAdapter<String>)super.getAdapter()).notifyDataSetChanged();
		DataUpdated = false;
		}
		super.onDraw(canvas);	
		
	}
	
	

}
