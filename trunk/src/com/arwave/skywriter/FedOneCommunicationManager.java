/**
 * 
 */
package com.arwave.skywriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.waveprotocol.wave.examples.fedone.common.DocumentConstants;
import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientBackend;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientUtils;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientWaveView;
import org.waveprotocol.wave.examples.fedone.waveclient.common.IndexEntry;
import org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener;
import org.waveprotocol.wave.model.document.operation.AnnotationBoundaryMap;
import org.waveprotocol.wave.model.document.operation.Attributes;
import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
import org.waveprotocol.wave.model.document.operation.DocInitializationCursor;
import org.waveprotocol.wave.model.document.operation.impl.InitializationCursorAdapter;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import android.widget.Toast;

/**
 * @author Davide
 *
 */
public class FedOneCommunicationManager implements
		AbstractCommunicationManager, WaveletOperationListener {

	/** */
	private ClientBackend backend = null;
	start mainWindow;
	private String blips;
	
	
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
		Map<String, BufferedDocOp> documentMap = ClientUtils.getConversationRoot(wave).getDocuments();
	    BufferedDocOp manifest = documentMap.get("conversation"); //it's a BufferedDocOpImpl actually
	    
	    renderManifest(documentMap, manifest);
	}

	private void renderManifest(final Map<String, BufferedDocOp> documentMap,
			BufferedDocOp manifest) {
		final StringBuilder blipsText = new StringBuilder();
		
		manifest.apply( new InitializationCursorAdapter(
		        new DocInitializationCursor() {

					public void annotationBoundary(AnnotationBoundaryMap arg0) {
						// TODO maybe this allows us to use annotations?
						
					}

					public void characters(String arg0) {
						//FIXME what to do here?
						
					}

					public void elementEnd() {
						//FIXME is this useful?
						
					}

					public void elementStart(String type, Attributes attr) {
						//big things here
						if( type.equals(DocumentConstants.BLIP)){
							//looks like this is a blip...
							//let's see if it has content too
							if( attr.containsKey(DocumentConstants.BLIP_ID)) {
								BufferedDocOp document = documentMap.get(attr.get(DocumentConstants.BLIP_ID));
								renderDocument(document, blipsText);
								
							}
						}
					}
		        	
		        })
				);
		
	}


	protected void renderDocument(BufferedDocOp document, final StringBuilder blipsText) {
		//final StringBuilder blipText = new StringBuilder();
		
		document.apply(new InitializationCursorAdapter(
		        new DocInitializationCursor() {

					public void annotationBoundary(AnnotationBoundaryMap arg0) {
						// TODO Auto-generated method stub
						
					}

					public void characters(String arg0) {
						blipsText.append(arg0);
						
					}

					public void elementEnd() {
						// TODO Auto-generated method stub
						blipsText.append("\n");
						
					}

					public void elementStart(String type, Attributes attr) {
						if( type.equals(DocumentConstants.LINE)) {
							
						}
						
					}
		        })
		);
		
		//mainWindow.addMessage(blipsText.toString());
		blips = blipsText.toString();
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
        }
        
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
		Toast.makeText(mainWindow.getApplicationContext(), arg0, 20);
	}

	public String getBlips(String waveletID) {
		this.openWavelet(waveletID);
		return blips;
	}
}
