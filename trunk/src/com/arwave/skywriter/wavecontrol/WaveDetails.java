package com.arwave.skywriter.wavecontrol;

public class WaveDetails {
	
    public String name;
    public String wid;
    
    boolean visible = false;
    boolean postToThis = false;
    
    boolean allowPostingTo = true;
    
    
    public WaveDetails(){
        super();
    }
    
	public WaveDetails(String name,boolean postToThis ) {
		super();
		this.name = name;
		this.wid = "no id";
		
		this.visible = true;
		this.postToThis = postToThis;
		
		
	}
	
	public void setUnpostable(boolean state){
		

	    allowPostingTo = !state;
	    
	    if (!state){
	    	this.postToThis = false;
	    }
		
	}
    
	public WaveDetails(String name, String wid, boolean visible,
			boolean postToThis) {
		super();
		this.name = name;
		this.wid = wid;
		this.visible = visible;
		this.postToThis = postToThis;
	}
    
}
