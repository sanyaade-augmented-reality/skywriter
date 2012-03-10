/**
 * 
 */
package com.arwave.skywriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.waveprotocol.wave.examples.fedone.common.DocumentConstants;
import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.examples.fedone.util.BlockingSuccessFailCallback;
import org.waveprotocol.wave.examples.fedone.util.SuccessFailCallback;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientBackend;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientUtils;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientWaveView;
import org.waveprotocol.wave.examples.fedone.waveclient.common.IndexEntry;
import org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitResponse;
import org.waveprotocol.wave.model.document.operation.AnnotationBoundaryMap;
import org.waveprotocol.wave.model.document.operation.Attributes;
import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
import org.waveprotocol.wave.model.document.operation.DocInitializationCursor;
import org.waveprotocol.wave.model.document.operation.impl.InitializationCursorAdapter;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;
import org.waveprotocol.wave.model.id.IdSerialiser.InvalidIdException;
import org.waveprotocol.wave.model.id.IdSerialiser.RuntimeInvalidIdException;
import org.waveprotocol.wave.model.operation.OperationException;
import org.waveprotocol.wave.model.operation.wave.WaveletDelta;
import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import android.util.Log;
import android.widget.Toast;

/**
 * @author Davide
 *
 */
public class FedOneCommunicationManager implements


		AbstractCommunicationManager, WaveletOperationListener {

	private static final String ServerAddress = null;
	/** */
	private ClientBackend backend = null;
	start mainWindow;
	//private String blips;
	//changed to array
	private ArrayList<Blip> blips = new ArrayList<Blip>();
	
	//current wave ID
	private String CurrentWaveletID = "";
	
	
	
	FedOneCommunicationManager( start s ) {
		mainWindow = s;
	}
	
	
	/*
	 * check if the client is actually connected
	 */
	public boolean isConnected() {
		return backend != null;
	}
	
	
	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#ARBlipInserted()
	 */
	public void ARBlipInserted(ARBlip blip,String WaveID) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#ARBlipUpdated()
	 */
	public void ARBlipUpdated(ARBlip blip, String WaveID) {
		// TODO Auto-generated method stub
		Log.i("wave", "updateARBlip");
	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#addARBlip(java.lang.String)
	 */
	public void addARBlip(String blipText) {
		// TODO Auto-generated method stub
		Log.i("wave", "addARBlip:"+blipText);
		
		 final ClientWaveView wave;
		try {
			wave = backend.getWave(( WaveId.checkedDeserialise(CurrentWaveletID) ));
		} catch (InvalidIdException e) {
			// TODO Auto-generated catch block
			Log.i("wave","invalid wave");
			return;
		}
		 
		 
		    if (wave == null) {
		    	Log.i("wave","null wave");
		      return;
		    }
		    
		    
		    WaveletData convRoot = ClientUtils.getConversationRoot(wave);
		    if (convRoot == null) {
		    	Log.i("wave","null convRoot");
		      return;
		    }
		    
		    BufferedDocOp manifest = convRoot.getDocuments().get(DocumentConstants.MANIFEST_DOCUMENT_ID);
		    if (convRoot == null) {
		    	Log.i("wave","null manifest");
		      return;
		    }
		    final String newDocId =  backend.getIdGenerator().newDocumentId();
		    
		    WaveletDelta delta = ClientUtils.createAppendBlipDelta(manifest, backend.getUserId(), newDocId, blipText);
		    
		    
		    backend.sendWaveletDelta(convRoot.getWaveletName(), delta, new SuccessFailCallback<ProtocolSubmitResponse, String>(){
		    	public void onFailure(String arg0) {
					Log.i("wave", "addingBlip failed");
				    
				}

				public void onSuccess(ProtocolSubmitResponse arg0) {
					
					Log.i("wave", "addingBlip success:");	
					
					/*
					Iterator<FieldDescriptor> fields = arg0.
					while (fields.hasNext())
					{
						Log.d("add", "FIELD="+fields.next().getName());
					}
					
					Iterator<FieldDescriptor> fields2 = arg0.getHashedVersionAfterApplication().getAllFields().keySet(). ;
					while (fields.hasNext())
					{
						Log.d("add", "FIELD2="+fields2.next().getName());
					}
					*/
					
					
					//turn the page back to the 3d view
					start.mHandler.post(start.goBackToWorldView);
					
				}
		    
		    });
		    
		    Log.i("wave", "addingBlip:"+newDocId);
		    
		    return;
		
		

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
	public String createWave(String waveTitle) {
		BlockingSuccessFailCallback<ProtocolSubmitResponse, String> callback =
				BlockingSuccessFailCallback.create();
		backend.createConversationWave(callback);
		callback.await(60, TimeUnit.SECONDS ); //one minute
		
		//should return waves true id
		return waveTitle;
		
	}

	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#deleteARBlip(java.lang.String)
	 */
	public void updateARBlip(String blipID, String WaveID, String newcontent) {
		
		Log.e("wave", "update GO!");
		 
		 ClientWaveView wave = null;
		 
		 
		 
		 try {
			wave = backend.getWave(( WaveId.checkedDeserialise(WaveID) ));
		} catch (InvalidIdException e1) {
			// TODO Auto-generated catch block
			
			Log.i("wave", "failed to get wave from waveletid");
			return;
			
		}
		
		WaveletData tempWaveletData = ClientUtils.getConversationRoot(wave);
		
		Map<String, BufferedDocOp> documentMap = tempWaveletData.getDocuments();
		   
		 BufferedDocOp manifest = documentMap.get("conversation");
		 try {
			 Log.i("wave", "modifying:");
			wave.getWavelet(WaveletId.deserialise(blipID)).modifyDocument(newcontent, manifest);
		 } catch (RuntimeInvalidIdException e) {
			// TODO Auto-generated catch block
			Log.e("wave", "failed to get wavelet from blipID:"+e.getMessage()+"__"+blipID);
			
		} catch (OperationException e) {
			// TODO Auto-generated catch block
			Log.e("wave", "failed to modify:"+e.getMessage());
		}		  
		// DocumentBasedManifest.delete(getManifestDocument(wavelet));
		 
		// Preconditions.checkState(manifest == null, "Conversation still usable");
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
	//	mainWindow.addMessage("Trying to open wavelet with id " + waveletID);
		if (waveletID==null){
			return;
		}

		CurrentWaveletID=waveletID;
		Log.i("wave", "opening wavelet = "+CurrentWaveletID);
		//we need to split
			
		ClientWaveView wave = backend.getWave( WaveId.deserialise(CurrentWaveletID) );
		Log.i("wave", "opening wavelet ="+wave.getWaveId()+";");
		
		WaveletData tempWaveletData = ClientUtils.getConversationRoot(wave);
		

		if (tempWaveletData==null){
			Log.e("wave","null wave error");
			return;
		}
		
		
		
		
		Log.i("wave", "wavelet participants="+tempWaveletData.getParticipants().toArray()[0]);
		
		Map<String, BufferedDocOp> documentMap = tempWaveletData.getDocuments();
	   
		BufferedDocOp manifest = documentMap.get("conversation"); //it's a BufferedDocOpImpl actually
		
		
	    //clear current blips
		blips.clear();
		
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
						
						//if( type.equals(DocumentConstants.BLIP_ID)){
											
							final String blipsID = attr.get(DocumentConstants.BLIP_ID);									
							Log.i("wave", "blipID="+blipsID);
							
							Log.i("wave", "blipsWaveID="+CurrentWaveletID);
							
					//	}
						
						if( type.equals(DocumentConstants.BLIP)){
							
							
							//looks like this is a blip...
							//let's see if it has content too
							if( attr.containsKey(DocumentConstants.BLIP_ID)) {
								
								
								
								
								BufferedDocOp document = documentMap.get(attr.get(DocumentConstants.BLIP_ID));
								
								renderDocument(document, blipsText, blipsID);
								
							}
						}
					}
		        	
		        })
				);
		
	}


	protected void renderDocument(BufferedDocOp document, final StringBuilder blipsText, final String BlipsID) {
		//final StringBuilder blipText = new StringBuilder();
		
		//quick check for null (empty) document.
		if (document==null){
			return;
		}
		
	
		document.apply(new InitializationCursorAdapter(
		        new DocInitializationCursor() {

					public void annotationBoundary(AnnotationBoundaryMap arg0) {
						// TODO Auto-generated method stub
						
					}

					public void characters(String arg0) {
						//blipsText.append(arg0);
						Blip newblip = new Blip();
						newblip.Content = arg0;
						newblip.BlipID = BlipsID;
						newblip.BlipsParentWaveID = CurrentWaveletID;
												
						blips.add(newblip);
						Log.i("wave","blips text="+arg0);
						
					}

					public void elementEnd() {
						// TODO Auto-generated method stub
						blipsText.append("\n");
						
						
						
					}

					public void elementStart(String type, Attributes attr) {
						if( type.equals(DocumentConstants.LINE)) {
							
						}
						
					//	Log.i("wave"," "+DocumentConstants.BLIP_ID);
					//	Log.i("wave"," "+DocumentConstants.MANIFEST_DOCUMENT_ID);
						
						
					}
		        })
		);
		
		//mainWindow.addMessage(blipsText.toString());
		//blips = blipsText.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.arwave.skywriter.AbstractCommunicationManager#updateARBlip(java.lang.String, java.lang.String)
	 */
	public void updateARBlip(String blipID, String text) {
		// TODO Auto-generated method stub
		
		Log.w("wave", "updatedBlip-"+text);
		
		
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
		Log.i("wave", "onCommit-");

	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#onDeltaSequenceEnd(org.waveprotocol.wave.model.wave.data.WaveletData)
	 */
	public void onDeltaSequenceEnd(WaveletData arg0) {
		
		Log.i("wave","-"+arg0.getWaveletName());
		
		List<IndexEntry> indexEntries = ClientUtils.getIndexEntries(backend.getIndexWave());
        String[] list = new String[ indexEntries.size() ];
        int i = 0;
        for (IndexEntry entry: indexEntries) {
        	
      	  list[i++] = entry.getWaveId().serialise();      	  
      	  Log.i("wave", "serialisedWaveID:"+list[i-1]);
      	  
        }
        
        mainWindow.showWaveList(list); 
        
        Log.i("wave", "maybe point for blip update?");
        
        //eek...is there no better way to do this? re-doing the whole wave for one blip update?
       // if we have a wavelet opened, then update all blips in that wave.
        // in future this could maybe itterate over all open waves?
        	String waveid = CurrentWaveletID;
        	 Log.i("wave", "current waveID="+waveid);
        	 
        	if (waveid.length()>2){
        	mainWindow.updateWavesARBlips(waveid);
        	}
      
                
        
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#onDeltaSequenceStart(org.waveprotocol.wave.model.wave.data.WaveletData)
	 */
	public void onDeltaSequenceStart(WaveletData arg0) {
		// TODO default method stub
		//mainWindow.addMessage("onDeltaSequenceStart");
		Log.i("wave", "dealtSequenceStart-");
	}

	/* (non-Javadoc)
	 * @see org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener#participantAdded(java.lang.String, org.waveprotocol.wave.model.wave.data.WaveletData, org.waveprotocol.wave.model.wave.ParticipantId)
	 */
	public void participantAdded(String arg0, WaveletData arg1,
			ParticipantId arg2) {
		// TODO default method stub
		//mainWindow.addMessage("participantAdded");
		/**
		   * Adds a participant to a wave.
		   *
		   * @param waveId the ID of the wave
		   * @param name   the user to add to the wave
		   * @return "OK" or "NOT FOUND"
		   
		    ParticipantId addId = new ParticipantId(name);
		    ClientWaveView wave = getWave(waveId);
		    if (wave == null) {
		      return "NOT FOUND";
		    }
		    WaveletData convRoot = ClientUtils.getConversationRoot(wave);
		    AddParticipant addUserOp = new AddParticipant(addId);
		    sendWaveletDelta(convRoot.getWaveletName(), new WaveletDelta(userId, ImmutableList.of(addUserOp)));
		    return "OK";
		  
*/
		
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
		
		Log.i("wave", "documented updated");
		mainWindow.waveletDocumentUpdated(arg0, arg1, arg2);
		
	}

	public ArrayList<Blip> getBlips(String waveletID) {
		this.openWavelet(waveletID);
		return blips;
	}


	public void deleteARBlip(String blipID) {
		// TODO Auto-generated method stub
		
	}


	public void setActiveWave(String waveID) {
		// TODO Auto-generated method stub
		
	}


	public String getCurrentWaveID() {
		// TODO Auto-generated method stub
		return null;
	}


	public String[] getParticipantList(String WaveID) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getServerAddress() {
		return ServerAddress;
		
	}
}
