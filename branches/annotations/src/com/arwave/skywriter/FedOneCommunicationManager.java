/**
 * 
 */
package com.arwave.skywriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientBackend;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientUtils;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientWaveView;
import org.waveprotocol.wave.examples.fedone.waveclient.common.IndexEntry;
import org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener;
import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import android.util.Log;

/**
 * @author Davide
 *
 */
public class FedOneCommunicationManager implements
		AbstractCommunicationManager, WaveletOperationListener {

	/** */
	private ClientBackend backend = null;
	start mainWindow;
	
	
	FedOneCommunicationManager( start s ) {
		mainWindow = s;
	}
	
	
	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#ARBlipInserted()
	 */
	public void ARBlipInserted() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#ARBlipUpdated()
	 */
	public void ARBlipUpdated() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#addARBlip(java.lang.String)
	 */
	public void addARBlip(String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#addParticipant(java.lang.String)
	 */
	public void addParticipant(String participant) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#closeWavelet()
	 */
	public void closeWavelet() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#createWave(java.lang.String)
	 */
	public void createWave(String waveTitle) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#deleteARBlip(java.lang.String)
	 */
	public void deleteARBlip(String blipID) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#login(java.lang.String, int, java.lang.String, java.lang.String)
	 */
	public void login(String serverAddress, int serverPort, String username,
			          String password) {
		try {
            backend = new ClientBackend(username, serverAddress, serverPort);
        } catch (IOException e) {
        	mainWindow.addMessage("Error: failed to connect to " + serverAddress + 
        			    " with user " + username );
            return;
        }
        backend.addWaveletOperationListener(this);
        mainWindow.addMessage("Connected to " + serverAddress);

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#logout()
	 */
	public void logout() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#openWavelet(java.lang.String)
	 */
	public void openWavelet(String waveletID) {
		mainWindow.addMessage("Trying to open wavelet with id " + waveletID);
		//we need to split
		
		ClientWaveView wave = backend.getWave( WaveId.deserialise(waveletID) );
		Iterable<? extends WaveletData> wavelets = wave.getWavelets();
		for( WaveletData wavelet: wavelets )
		{
			Collection<BufferedDocOp>  blips = wavelet.getDocuments().values();
			for( BufferedDocOp bdo: blips)
			{
				mainWindow.addMessage( bdo.getCharactersString(0) );
				
			}
			
		}
		//mainWindow.addMessage(wave.toString());
	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#updateARBlip(java.lang.String, java.lang.String)
	 */
	public void updateARBlip(String blipID, String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#noOp(java.lang.String, org.waveprotocol.wave.model.wave.data.WaveletData)
	 */
	public void noOp(String arg0, WaveletData arg1) {
		// TODO default method stub
		//mainWindow.addMessage("noOp");
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#onCommitNotice(org.waveprotocol.wave.model.wave.data.WaveletData, org.waveprotocol.wave.examples.fedone.common.HashedVersion)
	 */
	public void onCommitNotice(WaveletData arg0, HashedVersion arg1) {
		// TODO default method stub
		//mainWindow.addMessage("onCommitNotice");
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#onDeltaSequenceEnd(org.waveprotocol.wave.model.wave.data.WaveletData)
	 */
	public void onDeltaSequenceEnd(WaveletData arg0) {
		List<IndexEntry> indexEntries = ClientUtils.getIndexEntries(backend.getIndexWave());
        String[] list = new String[ indexEntries.size() ];
        int i = 0;
        for (IndexEntry entry: indexEntries) {
      	  list[i++] = entry.getWaveId().serialise();
      	  //.toString();

        }
        
        /*
        mainWindow.setWaveList(list);
        mainWindow.runOnUiThread(new Runnable() {

            public void run() {
            	mainWindow.showWaveList();
            }
          });
		*/
        mainWindow.showWaveList(list);
        
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#onDeltaSequenceStart(org.waveprotocol.wave.model.wave.data.WaveletData)
	 */
	public void onDeltaSequenceStart(WaveletData arg0) {
		// TODO default method stub
		//mainWindow.addMessage("onDeltaSequenceStart");
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#participantAdded(java.lang.String, org.waveprotocol.wave.model.wave.data.WaveletData, org.waveprotocol.wave.model.wave.ParticipantId)
	 */
	public void participantAdded(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		// TODO default method stub
		//mainWindow.addMessage("participantAdded");
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#participantRemoved(java.lang.String, org.waveprotocol.wave.model.wave.data.WaveletData, org.waveprotocol.wave.model.wave.ParticipantId)
	 */
	public void participantRemoved(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		// TODO default method stub
		//mainWindow.addMessage("participantRemoved");
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#waveletDocumentUpdated(java.lang.String, org.waveprotocol.wave.model.wave.data.WaveletData, org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation)
	 */
	public void waveletDocumentUpdated(String arg0, WaveletData arg1,
			WaveletDocumentOperation arg2) {
		// TODO default method stub
		//mainWindow.addMessage("waveletDocumentUpdated");
	}

}
