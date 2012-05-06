package com.arwave.skywriter;

import java.util.ArrayList;

import com.arwave.skywriter.utilities.NoConnection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.provider.MUCUserProvider;

import android.util.Log;
import android.widget.Toast;

public class XMPPCommunicationManager implements AbstractCommunicationManager {

	private static XMPPConnection con = null;
	private ConnectionConfiguration cc = null;

	private static MultiUserChat current_muc;

	private static HashMap<String, MultiUserChat> all_mucs = new HashMap<String, MultiUserChat>();

	static String loggedIn_username = "";
	
	private static start mainWindow;
	
	private String ServerAddress;

	final private static String ARBLIPPREFIX = "#ARWAVE#";

	//userlist
	final static ArrayList<String> friendlist = new ArrayList<String>();
	
	//this 	static XMPPCommunicationManager thisxmppcm = this;
	
	
	public XMPPCommunicationManager(start context) {

		mainWindow = context;

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
		
		//register extensions
		ProviderManager pm = ProviderManager.getInstance();
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
		//--
		
		if (con == null) {
			mainWindow.addMessage("conection failed : ");
			return;
		}

		try {
		
			con.connect();
			
			
			ServerAddress = serverAddress;

			// //if connected login
			Log.i("xmpp", "server connected, logging in : ");
			mainWindow.addMessage("Connected to: " + serverAddress);

			boolean success = processLogin(username, password);
			if (success) {
				mainWindow.addMessage("logged in to: " + serverAddress);
				// add disconnet detection
				con.addConnectionListener(new ConnectionListener() {

					public void connectionClosed() {
						mainWindow.addMessage("Connected closed");
						start.ProcessLogout();

					}

					public void connectionClosedOnError(Exception arg0) {
						mainWindow.addMessage("Connected closed with error:"
								+ arg0);
						start.ProcessLogout();

					}

					public void reconnectingIn(int arg0) {

					}

					public void reconnectionFailed(Exception arg0) {

					}

					public void reconnectionSuccessful() {

					}

				});


			} else {
				mainWindow.addMessage("failed to login");
			}
			//

		} catch (XMPPException e) {

			Log.i("xmpp", "conection failed : " + e.getMessage());
			mainWindow.addMessage("conection failed : " + e.getMessage());

		}

	}

	private boolean processLogin(String username, String password) {
		try {

			con.login(username, password);
			Log.e("xmpp", "Logged in");
			loggedIn_username = username;

			// get user list
			getBuddyList();

			// add invite listener (doesnt seem to work, at least on gtalk )
			MultiUserChat.addInvitationListener(con, new InvitationListener() {

				public void invitationReceived(Connection arg0, String arg1,
						String arg2, String arg3, String arg4, Message arg5) {

					Log.i("xmpp", "invite recieved!");

					Log.i("xmpp", "invite recieved FOR ROOM :" + arg1);

					// join room
					//XMPPCommunicationManager.joinWaveStatic(arg1);
					
					//trigger invite activity
					start.inviteRecieved(arg1,arg2);
					
				}

			});

			return true;

		} catch (XMPPException xe) {

			Log.e("xmpp",
					"Could not login with given username(" + username
							+ ") or password(" + password + "): "
							+ xe.getLocalizedMessage());

			mainWindow.addMessage("Could not login with given username("
					+ username + ") or password(" + password + "): "
					+ xe.getLocalizedMessage());

			return false;
		}
	}

	public void logout() {
		con.disconnect();
		Log.i("xmpp", "logout");
	}
	public static void joinWaveStatic(String roomID) {
		// test there's a connection
				if (!con.isConnected()) {
					Log.i("xmpp", "no connection");
					return;
				}
				
				Log.i("xmpp", "joining existing room");
				
				current_muc = new MultiUserChat(con, roomID);

				all_mucs.put(roomID, current_muc);

				Log.i("xmpp", "joining muc");
				
				//joins current muc
				joinARoom();
				
				start.addWave(roomID, roomID);
	}
	
	/** join a existing room  **/
	public void joinWave(String roomID) {
		
		joinWaveStatic(roomID);
		
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

		Log.i("xmpp", "just  made muc");

		// Create the room and add current user
		joinARoom();

		// Send an empty room configuration form which indicates that we want
		// an instant room
		try {
			current_muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
		} catch (XMPPException e) {
			Log.i("xmpp", "no room created:" + e.getMessage());

		}

		//

		// temp testing: add users
	//	current_muc.invite(loggedIn_username, "Meet me in this excellent room:"
	//			+ roomID);
		try {
			current_muc.sendMessage("Test muc message :");// send roomID maybe
															// as a link?
		} catch (XMPPException e) {

			e.printStackTrace();
		}

		Toast.makeText(mainWindow.getApplicationContext(),
				"New Wave Created (XMPP Room)", 20);

		

		// update interfaces wave list
		return current_muc.getRoom();

	}

	private static void joinARoom() {
		try {

			current_muc.join(loggedIn_username);

			// make a message listener for updates.
			current_muc.addMessageListener(new PacketListener() {
				final MultiUserChat cm = current_muc;

				public void processPacket(Packet arg0) {

					Log.e("xmpp", "processing incoming packets");

					processIncomingMessage(arg0, cm.getRoom());

				}

			});
			current_muc.addParticipantListener(new PacketListener() {

				public void processPacket(Packet arg0) {
					Log.i("xmpp", "packet recieved!");

					Log.i("xmpp", "packet recieved :" + arg0.toXML());

				}

			});

			Roster roster = con.getRoster();
			roster.addRosterListener(new RosterListener() {
				// Ignored events public void entriesAdded(Collection<String>
				// addresses) {}
				public void entriesDeleted(Collection<String> addresses) {
				}

				public void entriesUpdated(Collection<String> addresses) {
				}

				public void presenceChanged(Presence presence) {
					Log.i("xmpp", "Presence changed: " + presence.getFrom()
							+ " " + presence);
				}

				public void entriesAdded(Collection<String> arg0) {
					Log.i("xmpp", "Presence changed: " + arg0.size());

					Iterator<String> uit = arg0.iterator();
					while (uit.hasNext()) {
						String string = uit.next();

						Log.i("xmpp", "Presence changed: " + string);

					}

				}
			});

			// String user = presence.getFrom();
			// Presence bestPresence = roster.getPresence(user);

			//
			current_muc.addPresenceInterceptor(new PacketInterceptor() {

				public void interceptPacket(Packet arg0) {
					Log.i("xmpp", "packet i :" + arg0.toXML());

				}

			});

			// add invite listener (doesnt seem to work, at least on gtalk )
			current_muc
					.addInvitationRejectionListener(new InvitationRejectionListener() {

						public void invitationDeclined(String arg0, String arg1) {
							Log.i("xmpp", "rejection recieved!");

							Log.i("xmpp", "rejection recieved FOR ROOM :"
									+ arg1);

						}

					});

		} catch (XMPPException e1) {
			Log.i("xmpp", "no room created:" + e1.getMessage());

		}
		
		// set listener for updates to room ocupants
				current_muc.addParticipantListener(new PacketListener() {

					public void processPacket(Packet arg0) {

						Log.i("xmpp", " partipant update:" + arg0.toXML().toString());

						// dpesnt work
						// getParticipantList("waveid here");

					}

				});
				
	}

	public void getBuddyList() {
		Roster roster = con.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		friendlist.clear();
		System.out.println("\n\n" + entries.size() + " buddy(ies):");
		for (RosterEntry r : entries) {
			String newuser=r.getUser();
			Log.i("xmpp", "user:" + newuser);
			friendlist.add(newuser);
		}
	}

	public void openWavelet(String waveletID) {

	}

	public void closeWavelet() {

	}

	/** add AR Blip to active wave ID */
	public void addARBlip(String text) {
		Log.i("xmpp", "adding blip data to muc" + text);

		if (con.isConnected() && current_muc.isJoined()) {
			try {
				current_muc.sendMessage(text);
			} catch (XMPPException e) {
				Log.i("xmpp", "no muc:" + e.getMessage());

			}
		} else {
			Log.i("xmpp",
					"adding blip data to muc failed due to no connection with room");

		}
	}

	public void updateARBlip(String blipID, String WaveID, String text) {

		// we cant really edit with xmpp, so we just repost the blip
		// as it as the same ID it should update itself anyway
		// (in future creators should only be allowed to edit?))

	}

	public void deleteARBlip(String blipID) {

		// we cant really delete with xmpp, so we just repost the blip
		// as it as the same ID it should update itself anyway
		// (in future creators should only be allowed to edit?))
		// to signify its deleted we first remove all the text and replace
		// with _null_
		// a check can be put into the blip update function to remove it if this
		// is detected
	}

	public void addParticipant(String participant, String inviteTowaveID) {

		MultiUserChat muc = all_mucs.get(inviteTowaveID);
		Log.i("xmpp", "participant adding:" + participant + " to "
				+ inviteTowaveID);

		if (muc.isJoined()) {

			// check formating is valid
			Log.i("xmpp", "participant adding:" + participant);

			// try to invite
			muc.invite(participant,
					"Meet me in this ARWave!(arwave compatabal browser needed)");

		}
	}

	public void ARBlipUpdated(ARBlip arblip, String WaveToAddToo) {

	}

	public void ARBlipInserted(ARBlip arBlip, String RoomID) {

		
		// room ID should always be the same as the wave ID
	//	mainWindow.addBlip(arBlip, RoomID);
 XMPPCommunicationManager.ARBlipInsertedStatic(arBlip, RoomID);
 
	}

	public static void ARBlipInsertedStatic(ARBlip arBlip, String RoomID){
		mainWindow.addBlip(arBlip, RoomID);

	}
	
	public ArrayList<Blip> getBlips(String waveID) {
		return null;
	}

	public boolean isConnected() {
		return con.isConnected();
	}

	private static void processIncomingMessage(Packet arg0, String roomName) {

		
		Log.i("xmpp", "message received");
		Message msg = (Message) arg0;

		String contents = msg.getBody();
		Log.i("xmpp", "contents:" + contents);

		// detect if AR Blip and process
		if (contents.startsWith(ARBLIPPREFIX)) {

			Log.i("xmpp", "blip:" + contents + " from:" + roomName);
			ARBlipInsertedStatic(new ARBlip(contents), roomName);

		}
	}

	public void setActiveWave(String waveID) {
		current_muc = all_mucs.get(waveID);

	}

	public String getCurrentWaveID() {

		return current_muc.getRoom();
	}

	public String[] getParticipantList(String WaveID) {

		Log.i("xmpp", "ocupants:" + current_muc.getOccupantsCount());
		int count = current_muc.getOccupantsCount();
		//
		// if (count>0){
		//
		// try {
		// Log.i("xmpp","ocupants:"+current_muc.getOccupantsCount());
		//
		// Collection<Occupant> users;
		//
		// if (current_muc.getParticipants()!=null){
		// users = current_muc.getParticipants();
		// } else {
		// Log.e("xmpp","null returned by get participants...dunno why");
		//
		// return null;
		// }
		//
		// Iterator<Occupant> oit= users.iterator();
		//
		// while (oit.hasNext()) {
		//
		// Occupant occupant = (Occupant) oit.next();
		//
		// Log.i("xmpp", occupant.getJid());
		//
		// }
		//
		//
		// } catch (XMPPException e) {
		//
		// // TODO Auto-generated catch block
		// Log.i("xmpp", "occupant  count fail");
		// }
		//
		// }
		return null;
	}

	public String getServerAddress() {
		return ServerAddress;
	}

	public String[] getFriendList() {
		
        
		String []strArray = new String[friendlist.size()];

		return friendlist.toArray(strArray);
		
	}

	public String getLoggedInUser() throws NoConnection {
		if (start.acm.isConnected()){
		 return con.getUser();
		} else {
			throw new NoConnection();	
		}
	}

}
