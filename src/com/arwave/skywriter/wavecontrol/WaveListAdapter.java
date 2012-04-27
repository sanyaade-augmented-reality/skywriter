package com.arwave.skywriter.wavecontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arwave.skywriter.start;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

public class WaveListAdapter extends ArrayAdapter<WaveDetails>{

	    public static void deselectAllPostToButtons() {
		for (WaveDetails wd : data) {
			
			Log.i("wave", "disselecting button from "+wd.name);
			
			//if (waved!=wd){
			wd.postToThis=false;
			//}
			
		}
	}

		Context context; 
	    int layoutResourceId;    
	    
	    //stores the data for all waves
	    static List<WaveDetails> data = null;
	    
	    
	    
	//    static ArrayList<RadioButton> radiobuttons = new ArrayList<RadioButton>();
	    
	    
	    public WaveListAdapter(Context context, int layoutResourceId, List<WaveDetails> data) {

	    	super(context, layoutResourceId, data);
	    	
	    //	radiobuttons.clear();
	    	
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;
	        
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        WaveDataHolder holder = null;
	        
	        WaveDetails wavedata = data.get(position); //position unreliable?? what if nw stuff is inserted?
  
	        if(row == null)
	        {
	        	//for some reason new rows dont always trigger this :?
	        	
		        Log.i("wavelist", "setting new row for "+wavedata.name);
		      
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new WaveDataHolder();
	            
	            holder.chkInUse = (RadioButton)row.findViewWithTag("inUse");
	            
	            holder.chkInUse.setOnClickListener(new inUseBoxClicked(wavedata,this));
		    
	            holder.textWaveName = (CheckedTextView)row.findViewWithTag("WaveNameAndVis");
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (WaveDataHolder)row.getTag();
	        }
	        
	        Log.i("wavelist", "setting view active on "+wavedata.name+" to"+wavedata.postToThis);
		    
	        //update listener again (as this goes wrong due I think too objects being recycled
	        //this really is wastefull and shouldn't be needed, however :-/
            holder.chkInUse.setOnClickListener(new inUseBoxClicked(wavedata,this));
	        
            
	        
	        if (wavedata.name.length()<24){
	        holder.textWaveName.setText(wavedata.name);
	        } else {
	        	holder.textWaveName.setText(wavedata.name.substring(0, 24)); //maybe show end of name rather then start
		        	
	        }
	        
	        holder.chkInUse.setChecked(wavedata.postToThis);
	        
	        if (!wavedata.allowPostingTo){
	        	  holder.chkInUse.setVisibility(View.INVISIBLE);	        	  
	        } else{
	        	 holder.chkInUse.setVisibility(View.VISIBLE);	
	        }
	        	        
	        holder.textWaveName.setChecked(wavedata.visible);
	        
	        holder.chkInUse.setFocusable(false); //fixs glitch that causes bad additional selections
	        holder.textWaveName.setFocusable(false);
	          
	        return row;
	    }
	    
	    static class WaveDataHolder
	    {
	    	CheckedTextView textWaveName;
	        RadioButton chkInUse;
	        
	    }
	    
	    static class inUseBoxClicked implements OnClickListener
	    {
	    	WaveDetails waved = new WaveDetails();

		    WaveListAdapter tadapter;
		    
	    	public inUseBoxClicked(WaveDetails waved, WaveListAdapter tadapter){
	    		this.waved = waved;
	    		Log.i("wavelist", "wave id set too"+waved.name);
	    		this.tadapter=tadapter;
	    	}
	    	

			public void onClick(View v) {

				//test if its allowed to be set and only set if thats the case
				if (waved.allowPostingTo){
				
				Log.i("wavelist", "changed!");
				
				// get the id
				String waveID = WaveList.getWaveIDFromNick(waved.name);
				Log.i("wave", "setting active wave too:" + waveID);

				// set current wave to selected one
				start.acm.setActiveWave(waveID);
			
				//disselect others
				
				Log.i("wave", "disselect others");
				//Iterator<RadioButton> rbi=radiobuttons.iterator();
				
				deselectAllPostToButtons();
				
				waved.postToThis=true;
					//RadioButton rb = (RadioButton) rbi.next();
				tadapter.notifyDataSetChanged();
					Log.i("wave", "fin");
				} else {
					
					((RadioButton)v).setChecked(false);
					
					
				}
				
				
				
			}
	    	
	    }
	    
	}
	
