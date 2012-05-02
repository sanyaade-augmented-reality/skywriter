package com.arwave.skywriter.utilities;

import com.arwave.skywriter.start;

import android.util.Log;


public class NoConnection extends Exception {
	 
	


	/**
	 *  One of these days I'll work out what this serial thing actualy means. Eclipse tells me 
	 *  to add it, and I obay.
	 */
	private static final long serialVersionUID = 7249026310216567843L;

	public NoConnection() {
		  
		  //run tasks that should happen on disconnect?
		  Log.i("con", "no connection! - Trigger logout?");
		  
		start.ProcessLogout();
		  
	  }

	  public NoConnection(String msg) {
	    super(msg);
	  }
	}