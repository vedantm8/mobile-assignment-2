/*********************************************************************

 Chat server: accept chat messagesAdapter from clients.

 Sender name and GPS coordinates are encoded
 in the messagesAdapter, and stripped off upon receipt.

 Copyright (c) 2017 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chatserver.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;

import edu.stevens.cs522.base.Datagram;
import edu.stevens.cs522.base.DatagramConnectionFactory;
import edu.stevens.cs522.base.IDatagramConnection;
import edu.stevens.cs522.chatserver.R;
import edu.stevens.cs522.chatserver.entities.Message;
import edu.stevens.cs522.chatserver.entities.Peer;
import edu.stevens.cs522.chatserver.entities.TimestampConverter;

public class ChatServerActivity extends ComponentActivity implements OnClickListener {

    public final static String TAG = ChatServerActivity.class.getCanonicalName();

    public final static String SENDER_NAME = "name";

    public final static String CHATROOM = "room";

    public final static String MESSAGE_TEXT = "text";

    public final static String TIMESTAMP = "timestamp";

    public final static String LATITUDE = "latitude";

    public final static String LONGITUDE = "longitude";

    /*
     * Network connection used both for sending and receiving
     */

    private IDatagramConnection serverConnection;

    /*
     * True as long as we don't get network errors
     */
    private boolean socketOK = true;

    private ArrayList<Peer> peers;

    private ArrayList<Message> messages;

    /*
     * TODO: Declare a listview for messages, and an adapter for displaying messages.
     */

    /*
     * End Todo
     */

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_messages);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.view_messages), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the view_messages thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.getSharedPreferences(null, 0);

        try {
            /*
             * Get port information from the resources.
             */
            int port = getResources().getInteger(R.integer.app_port);

            DatagramConnectionFactory factory = new DatagramConnectionFactory();
            serverConnection = factory.getUdpConnection(port);

        } catch (Exception e) {
            throw new IllegalStateException("Cannot open server connection", e);
        }

        // List of peers
        peers = new ArrayList<Peer>();

        // List of messages
        messages = new ArrayList<Message>();

        // TODO: Initialize the list view with the array adapter.
        // Use android.R.layout.simple_list_item_1 for list item layout

        // TODO bind the button for "next" to this activity as listener


    }

    public void onClick(View v) {

        Datagram receivePacket = new Datagram();

        try {

            String sender = null;

            String room = null;

            String text = null;

            Instant timestamp = null;

            Double latitude = null;

            Double longitude = null;

            /*
             * THere is an apparent bug in the emulator TCP stack on Windows where
             * messages can arrive empty.
             *
             * If using SMS, we cannot block on the main thread waiting for a
             * message to arrive, because the broadcast receiver will never execute!
             */

            Log.d(TAG, "Receiving a packet....");
            serverConnection.receive(receivePacket);

            if (receivePacket.getData() == null) {
                Log.d(TAG, "....no data, skipping....");
                return;
            }

            String address = receivePacket.getAddress();
            Log.d(TAG, "Source Address: " + address);

            String content = receivePacket.getData();
            Log.d(TAG, "Message received: " + content);

            /*
             * Parse the JSON object
             */
            JsonReader rd = new JsonReader(new StringReader(content));

            rd.beginObject();
            if (SENDER_NAME.equals(rd.nextName())) {
                sender = rd.nextString();
            }
            if (CHATROOM.equals(rd.nextName())) {
                room = rd.nextString();
            }
            if (MESSAGE_TEXT.equals((rd.nextName()))) {
                text = rd.nextString();
            }
            if (TIMESTAMP.equals(rd.nextName())) {
                timestamp = TimestampConverter.deserialize(rd.nextString());
            }
            if (LATITUDE.equals(rd.nextName())) {
                latitude = rd.nextDouble();
            }
            if (LONGITUDE.equals((rd.nextName()))) {
                longitude = rd.nextDouble();
            }
            rd.endObject();

            rd.close();

            /*
             * Add the sender to our list of senders
             */
            Peer peer = new Peer();
            peer.name = sender;
            peer.timestamp = timestamp;
            peer.latitude = latitude;
            peer.longitude = longitude;
            addPeer(peer);

            Message message = new Message();
            message.messageText = text;
            message.chatroom = room;
            message.sender = sender;
            message.timestamp = timestamp;
            message.latitude = latitude;
            message.longitude = longitude;
            /*
             * TODO: Add message to the display.
             */

            /*
             * End Todo
             */

        } catch (Exception e) {

            Log.e(TAG, "Problems receiving packet: ", e);
            socketOK = false;
        }

    }

    /*
     * Close the socket before exiting application
     */
    public void closeSocket() {
        if (serverConnection != null) {
            serverConnection.close();
            serverConnection = null;
        }
    }

    /*
     * If the socket is OK, then it's running
     */
    boolean socketIsOK() {
        return socketOK;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSocket();
    }

    private void addPeer(Peer peer) {
        for (Peer p : peers) {
            if (p.name.equals(peer.name)) {
                p.timestamp = peer.timestamp;
                p.latitude = peer.latitude;
                p.longitude = peer.longitude;
                return;
            }
        }
        peers.add(peer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO inflate a menu with PEERS option


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        if (itemId == R.id.peers) {
            // TODO PEERS provide the UI for viewing list of peers
            // The list of peers must be passed as an argument to the subactivity..

            return true;

        }
        return false;
    }
    
}