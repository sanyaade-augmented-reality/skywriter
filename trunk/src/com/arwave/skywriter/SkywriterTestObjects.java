package com.arwave.skywriter;

import android.util.Log;


/** The intention of this class is to keep common objects we use for testing.
 * for example, a set of blips here our house, the local pub etc **/

class ThomasAndBertinesPlace extends ARBlip
{	
	public ThomasAndBertinesPlace(){		
		this.x = 51.558493; 
		this.y = 5.077505;
		this.z = 20; 
		this.BlipID = "OurPlace";
		Log.i("test_objects", "our place \r line 2 \r line 3"); 
		this.ObjectData = "Our Place! \r line 2 \r line 3"; 
		this.isFacingSprite=true;		
	};
}

class ClancysTilburg extends ARBlip
{	
	public ClancysTilburg(){		
		this.x = 51.557146; 
		this.y = 5.092139;
		this.z = 40; 
		this.BlipID = "Clancys";
		Log.i("test_objects", "clancys"); 
		this.ObjectData = "Clancys bar is here!"; 
		this.isFacingSprite=true;		
	};
}

//A list of sample markers down a street in tilburg!
// (please change if you wish to test more localy to you)
/*
 * double blipDataX[] = {
 * 51.560071,51.559150,51.558890,51.55839
 * ,51.55759,51.559230}; double blipDataY[] = {
 * 5.07822,5.07792,5.07785,5.07774,5.07765,5.07974 };
 * 
 * for (int i = 0; i < blipDataX.length;) {
 * 
 * // we can now load up some sample blips ARBlip testblip1
 * = new ARBlip(); testblip1.x = blipDataX[i]; // 51.558348
 * //51.558325 testblip1.y = blipDataY[i]; testblip1.z = 0;
 * testblip1.BlipID = "NewTestBlip" + i; Log.i("creating",
 * testblip1.BlipID); testblip1.ObjectData = "" + i;
 * testblip1.isFacingSprite=true;
 * 
 * try { arView.addBlip(testblip1); } catch (IOException e)
 * { // addBlip can cause an error if it has a malformed
 * url, or other problem loading a remote 3d file
 * e.printStackTrace(); } i++; }
 */

