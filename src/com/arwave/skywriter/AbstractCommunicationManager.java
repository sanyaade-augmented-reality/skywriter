package com.arwave.skywriter;

public interface AbstractCommunicationManager {

	public void login( String serverAddress, int serverPort, String username, String password );
	public void logout();
	public void createWave( String waveTitle );
	public void openWavelet( String waveletID );
	public void closeWavelet();
	public void addARBlip( String text );
	public void updateARBlip( String blipID, String text );
	public void deleteARBlip( String blipID );
	public void addParticipant( String participant );
	public void ARBlipUpdated();
	public void ARBlipInserted();
}
