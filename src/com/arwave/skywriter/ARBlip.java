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

	
	
	public String getBlipID() {
		return BlipID;
	}

	public void setBlipID(String blipID) {
		BlipID = blipID;
	}

	public String getObjectData() {
		return ObjectData;
	}

	public void setObjectData(String objectData) {
		ObjectData = objectData;
	}

	public String getMIMEtype() {
		return MIMEtype;
	}

	public void setMIMEtype(String mIMEtype) {
		MIMEtype = mIMEtype;
	}

	public String getCordinateSystemUsed() {
		return CordinateSystemUsed;
	}

	public void setCordinateSystemUsed(String cordinateSystemUsed) {
		CordinateSystemUsed = cordinateSystemUsed;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public int getBaring() {
		return baring;
	}

	public void setBaring(int baring) {
		this.baring = baring;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public int getRoll() {
		return roll;
	}

	public void setRoll(int roll) {
		this.roll = roll;
	}

	public String getDataUpdatedTimestamp() {
		return DataUpdatedTimestamp;
	}

	public void setDataUpdatedTimestamp(String dataUpdatedTimestamp) {
		DataUpdatedTimestamp = dataUpdatedTimestamp;
	}

	public String getMarkerURL() {
		return MarkerURL;
	}

	public void setMarkerURL(String markerURL) {
		MarkerURL = markerURL;
	}

	public ArrayList<String> getMetaTags() {
		return MetaTags;
	}

	public void setMetaTags(ArrayList<String> metaTags) {
		MetaTags = metaTags;
	}

	/**
	 * FIXME: this is very rough and will simply return an empty or malformed
	 * arblip in case of errors
	 * @param rawARBlip
	 * @return
	 */
	public static ARBlip fromString( String rawARBlip ) {
		ARBlip arblip = new ARBlip();
		try
		{
			//parse latitude
			arblip.setX( Double.parseDouble( rawARBlip.substring(0, rawARBlip.indexOf("#")) ) );
			rawARBlip = rawARBlip.substring(rawARBlip.indexOf("#")+1);
			//parse longitude
			arblip.setY( Double.parseDouble( rawARBlip.substring(0, rawARBlip.indexOf("#")) ) );
			rawARBlip = rawARBlip.substring(rawARBlip.indexOf("#")+1);
			//parse altitude
			arblip.setZ( Double.parseDouble( rawARBlip.substring(0, rawARBlip.indexOf("#")) ) );
			rawARBlip = rawARBlip.substring(rawARBlip.indexOf("#")+1);
			//parse data
			arblip.setObjectData(rawARBlip);
		}
		catch(Exception e)
		{
			return arblip;
		}
		
		return arblip;
	}
	
}

