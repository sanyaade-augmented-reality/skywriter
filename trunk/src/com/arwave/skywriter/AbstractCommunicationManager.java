package com.arwave.skywriter;

import java.util.ArrayList;

public interface AbstractCommunicationManager {

	public void login( String serverAddress, int serverPort, String username, String password );
	public void logout();
	public void createWave( String waveTitle );
	public void openWavelet( String waveletID );
	public void closeWavelet();
	public void addARBlip( String text );
	public void updateARBlip( String blipID,String WaveID, String text );
	public void deleteARBlip( String blipID );
	public void addParticipant( String participant );
	public void ARBlipUpdated();
	public void ARBlipInserted();
	public ArrayList<Blip> getBlips(String waveID);
	
}
