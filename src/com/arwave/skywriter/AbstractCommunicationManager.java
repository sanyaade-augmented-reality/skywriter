package com.arwave.skywriter;

import java.util.ArrayList;

import com.arwave.skywriter.utilities.NoConnection;

public interface AbstractCommunicationManager {

	//login/logout of server and server details
	public void login( String serverAddress, int serverPort, String username, String password );
	public void logout();
	public boolean isConnected();
	
	public String getLoggedInUser() throws NoConnection;
	public String getServerAddress();
	public String[] getFriendList();
	
	//wave creation functions
	public String createWave( String waveTitle );
	
	public void openWavelet( String waveletID );
	public void closeWavelet();
	
	public void setActiveWave (String waveID); /** where to post too! **/
	public String getCurrentWaveID();
	
	//join a wave/room
	public void joinWave(String roomID); 
		
	//data sending functions
	public void addARBlip( String text );
	public void updateARBlip( String blipID,String WaveID, String text );
	
	public void deleteARBlip( String blipID );
	public void ARBlipUpdated(ARBlip blip, String WaveToAddToo);
	public void ARBlipInserted(ARBlip blip, String WaveToAddToo);
	public ArrayList<Blip> getBlips(String waveID);
	
	//participant functions
	public void addParticipant( String participant , String WaveID);
	public String[] getParticipantList( String WaveID);
	
}
