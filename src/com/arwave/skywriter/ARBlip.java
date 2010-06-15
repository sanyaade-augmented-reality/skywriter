package com.arwave.skywriter;
import java.util.ArrayList;

/** Current, crude, version of a blip object. A lot needed to refine **/
public class ARBlip {
	
//Should be a unique identification of the object
	String BlipID = "";
		
//A url pointing to the data to geolocate
	String ObjectData = "";
	
//MIME type
	String MIMEtype = "";
	
// Co-ordinate system in use
	String CordinateSystemUsed ="";
	
//Location
	double x = 0.0;
	double y = 0.0;
	double z = 0.0;

//Vector
//If no vector is specified, its assumed to be a facing sprite.
	int baring = 0;
	int elevation = 0;
	int roll = 0;
	// (should these be doubles too? or is that wastefull? float? half a degree could make quite a difference)
	
	String DataUpdatedTimestamp; //Time the Data was updated/changed, not merely when the blip was changed.
	
//Permissions 
	
	
//Referance URL for positioning on a marker
	String MarkerURL="";
	
//Metatags
	ArrayList<String> MetaTags = new ArrayList<String>();

	
}
