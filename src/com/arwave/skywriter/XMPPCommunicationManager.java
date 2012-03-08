package com.arwave.skywriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

import android.util.Log;
import android.widget.Toast;

public class XMPPCommunicationManager implements AbstractCommunicationManager {

	private XMPPConnection con = null;
	private ConnectionConfiguration cc = null;

	private MultiUserChat current_muc;

	private HashMap<String, MultiUserChat> all_mucs = new HashMap<String, MultiUserChat>();

	String loggedIn_username = "";
	private start mainWindow;
	
	

	final private static String ARBLIPPREFIX = "#ARWAVE#";

	public XMPPCommunicationManager(start context) {

		mainWindow = context;

		// TODO Auto-generated constructor stub
	}

	public void login(String serverAddress, int serverPort, String username,
			String password) {

		// ensure no active connection already
		if (con != null) {
			con.disconnect();
		}

		// first connect
		Log.i("xmpp", "login");

		cc = new ConnectionConfiguration(serverAddress, 5222, "gmail.com");

		con = new XMPPConnection(cc);

		try {
			con.connect();
			// //if connected login
			Log.i("xmpp", "server connected, logging in : ");
			mainWindow.addMessage("Connected to: " + serverAddress);

			boolean success = processLogin(username, password);
			if (success) {
				mainWindow.addMessage("logged in to: " + serverAddress);
				//add disconnet detection
				con.addConnectionListener(new ConnectionListener(){

					public void connectionClosed() {
						mainWindow.addMessage("Connected closed" );
						start.ProcessLogout();
						
					}

					public void connectionClosedOnError(Exception arg0) {
						mainWindow.addMessage("Connected closed with error:"+arg0 );
						start.ProcessLogout();
						
						
					}

					public void reconnectingIn(int arg0) {
						// TODO Auto-generated method stub
						
					}

					public void reconnectionFailed(Exception arg0) {
						// TODO Auto-generated method stub
						
					}

					public void reconnectionSuccessful() {
						// TODO Auto-generated method stub
						
					}
					
				});
			} else {
				mainWindow.addMessage("failed to login");
			}
			//

		} catch (XMPPException e) {
			// TODO Auto-generated catch block

			Log.i("xmpp", "conection failed : " + e.getMessage());
			mainWindow.addMessage("conection failed : " + e.getMessage());

		}

	}

	private boolean processLogin(String username, String password) {
		try {

			con.login(username, password);
			Log.e("xmpp", "Logged in");
			loggedIn_username = username;

			// add invite listener (doesnt seem to work, at least on gtalk )
			MultiUserChat.addInvitationListener(con, new InvitationListener() {

				public void invitationReceived(Connection arg0, String arg1,
						String arg2, String arg3, String arg4, Message arg5) {

					Log.i("xmpp", "invite recieved!");

					Log.i("xmpp", "invite recieved FOR ROOM :" + arg1);

					// join room

				}

			});

			return true;

		} catch (XMPPException xe) {
			
			Log.e("xmpp",
					"Could not login with given username(" + username
							+ ") or password(" + password + "): "
							+ xe.getLocalizedMessage());
			
			mainWindow.addMessage("Could not login with given username(" + username
					+ ") or password(" + password + "): "
					+ xe.getLocalizedMessage());
			
			return false;
		}
	}

	public void logout() {
		// TODO Auto-generated method stub
		con.disconnect();
		Log.i("xmpp", "logout");
	}

	public String createWave(String preposedTitle) {

		// In XMPP the nearest equivalent to a wave is a room

		Log.i("xmpp", "createing wave");

		// test there's a connection
		if (!con.isConnected()) {
			Log.i("xmpp", "no connection");
			return null;
		}

		// Create a MultiUserChat using a Connection for a room
		Log.i("xmpp", "creating room id and muc");

		String roomID = "private-chat-" + UUID.randomUUID().toString()
				+ "@groupchat.google.com";

		// room="private-chat-" + "room name"+ "@groupchat.google.com";
		// new muc is current by default
		current_muc = new MultiUserChat(con, roomID);

		all_mucs.put(roomID, current_muc);

		Log.i("xmpp", "muc made");

		// Create the room and add current user
		try {

			current_muc.join(loggedIn_username);

			// make a message listener for updates.
			current_muc.addMessageListener(new PacketListener() {
				final MultiUserChat cm = current_muc;

				public void processPacket(Packet arg0) {
					Log.e("xmpp", "processing packets");

					processIncomingMessage(arg0, cm.getRoom());

				}

			});
			

		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			Log.i("xmpp", "no room created:" + e1.getMessage());

		}

		// Send an empty room configuration form which indicates that we want
		// an instant room
		try {
			current_muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			Log.i("xmpp", "no room created:" + e.getMessage());

		}

		//

		// add users
		current_muc.invite(loggedIn_username, "Meet me in this excellent room:"+roomID);
		current_muc.invite("atresica@gmail.com","Meet me in this excellent room:"+roomID);

		try {
			current_muc.sendMessage("Test muc message :");//send roomID maybe as a link?
		} catch (XMPPException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Toast.makeText(mainWindow.getApplicationContext(),
				"New Wave Created (XMPP Room)", 20);

		//set listener for updates to room ocupants
		current_muc.addParticipantListener(new PacketListener(){

			public void processPacket(Packet arg0) {
				// TODO Auto-generated method stub
				
				Log.i("xmpp", " partipant update:"+arg0.toString());
				getParticipantList("waveid here");
				
				
			}
			
		});
		
		// update interfaces wave list
		return current_muc.getRoom();

	}

	public void openWavelet(String waveletID) {
		// TODO Auto-generated method stub

	}

	public void closeWavelet() {
		// TODO Auto-generated method stub

	}

	/** add AR Blip to active wave ID */

	public void addARBlip(String text) {
		// TODO Auto-generated method stub
		Log.i("xmpp", "adding blip data to muc" + text);

		if (con.isConnected() && current_muc.isJoined()) {
			try {
				current_muc.sendMessage(text);
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				Log.i("xmpp", "no muc:" + e.getMessage());

			}
		} else {
			Log.i("xmpp",
					"adding blip data to muc failed due to no connection with room");

		}
	}

	public void updateARBlip(String blipID, String WaveID, String text) {
		// TODO Auto-generated method stub

	}

	public void deleteARBlip(String blipID) {
		// TODO Auto-generated method stub

	}

	public void addParticipant(String participant) {
		// TODO Auto-generated method stub
		if (current_muc.isJoined()) {
			current_muc.invite(participant, "Meet me in this excellent room");

		}
	}
	
	

	public void ARBlipUpdated(ARBlip arblip, String WaveToAddToo) {
		// TODO Auto-generated method stub

	}

	public void ARBlipInserted(ARBlip arBlip, String RoomID) {

		// room ID should always be the same as the wave ID
		mainWindow.addBlip(arBlip, RoomID);

	}

	public ArrayList<Blip> getBlips(String waveID) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return con.isConnected();
	}

	private void processIncomingMessage(Packet arg0, String roomName) {

		Log.i("xmpp", "message received");
		Message msg = (Message) arg0;

		String contents = msg.getBody();
		Log.i("xmpp", "contents:" + contents);

		// detect if AR Blip and process
		if (contents.startsWith(ARBLIPPREFIX)) {

			Log.i("xmpp", "blip:" + contents + " from:" + roomName);
			ARBlipInserted(new ARBlip(contents), roomName);

		}
	}

	public void setActiveWave(String waveID) {
		// TODO Auto-generated method stub

		current_muc = all_mucs.get(waveID);

	}

	public String getCurrentWaveID() {

		return current_muc.getRoom();
	}

	public String[] getParticipantList(String WaveID) {
		
		Log.i("xmpp","ocupants:"+current_muc.getOccupantsCount());
		int count=current_muc.getOccupantsCount();
		
		if (count>0){
		
		try {
			Log.i("xmpp","ocupants:"+current_muc.getOccupantsCount());
			
			Collection<Occupant> users;
			
			if (current_muc.getParticipants()!=null){
			users = current_muc.getParticipants();
			} else {
				Log.e("xmpp","null returned by get participants...dunno why");
					
				return null;
			}
			
			Iterator<Occupant> oit= users.iterator();
			
			while (oit.hasNext()) {
				
				Occupant occupant = (Occupant) oit.next();
				
				Log.i("xmpp", occupant.getJid());
				
			}
			
		
		} catch (XMPPException e) {
			
			// TODO Auto-generated catch block
			Log.i("xmpp", "occupant  count fail");
		}
		
		}
		return null;
	}

}
