package com.arwave.skywriter;

import com.threed.jpct.RGBColor;

public class SkywriterBillboard extends Rectangle {

	
	
	
	
	
	public SkywriterBillboard() {
			
		super(1, 8, 3); // fixed, but in future these should vary based on the size of the text?
		
		this.setAdditionalColor(RGBColor.WHITE);
		this.setCulling(false);
		
		//in future handle the text/texture internaly
		
		
	}

}
