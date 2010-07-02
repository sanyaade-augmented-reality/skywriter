package com.arwave.skywriter;

import java.util.ArrayList;

public interface AbstractCommunicationManager {

	public void login( String serverAddress, int serverPort, String username, String password );
	public void logout();
	public void createWave( String waveTitle );
	public ArrayList<ARBlip> openWavelet( String waveletID );
	public void closeWavelet();
	public void addARBlip( String waveID, String text );
	public void updateARBlip( String blipID, String text );
	public void deleteARBlip( String blipID );
	public void addParticipant( String participant );
	public void ARBlipUpdated();
	public void ARBlipInserted();
	public String getBlips(String waveID);
	public ArrayList<ARBlip> getARBlips( String waveletID );
	public String getUsername();
}
