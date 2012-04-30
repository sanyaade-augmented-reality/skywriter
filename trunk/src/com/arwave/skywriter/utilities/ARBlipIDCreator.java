package com.arwave.skywriter.utilities;

import com.arwave.skywriter.start;

/** simply supplys unique IDs for the blips **/
public class ARBlipIDCreator {

	static int UsersCurrentNumber = 0;
	
	static public String getFreshID() throws NoConnection {
		
		
		
		String NewID = start.acm.getLoggedInUser()+ "_"+UsersCurrentNumber;
		
		UsersCurrentNumber++;
		
		
		return NewID;
		
	}
}
