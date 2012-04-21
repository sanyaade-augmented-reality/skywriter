package com.arwave.skywriter.wavecontrol;

import java.util.HashMap;

public class WaveList  {
	
	static HashMap<String, String> globalwavelist = new HashMap<String,String>();;
	
	static public String getWaveIDFromNick(String Name){
		return globalwavelist.get(Name);
	}
	
	static public String putWaveInfo(String Name,String WaveID){
		return globalwavelist.put(Name, WaveID);
		
	}
	

}
