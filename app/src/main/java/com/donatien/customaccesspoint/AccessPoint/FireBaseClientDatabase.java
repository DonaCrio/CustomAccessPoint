package com.donatien.customaccesspoint.AccessPoint;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to manage access to Client storage using FireBase.
 * This class stores the clients in field "clients" that is updated constantly.
 */
public class FireBaseClientDatabase implements ValueEventListener {

    private static final String TAG = "FireBaseClientDatabase";

    private final Context mContext;

    FirebaseDatabase database;
    DatabaseReference myRef;

    private List<Client> clients;

    /**
     * Constructor.
     * Gets FireBaseDatabase instance.
     * Sets database reference.
     * @param context the context
     */
    public FireBaseClientDatabase(Context context) {
        this.mContext = context;
        this.clients = new ArrayList<Client>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.addValueEventListener(this);
        // We insert false client to trigger onDataChange() and get clients at instantiation time.
        myRef.child("Clients").child("00:00:00:00:00:00").setValue(new Client());
    }

    /**
     * Writes new client in the database.
     * Indexes clients by MAC address.
     * @param name the name
     * @param ipAddr the IP address
     * @param hwAddr the MAC adress
     * @param status the status
     */
    public void writeNewClient(String name, String ipAddr, String hwAddr, int status) {
        Client client = new Client(name, ipAddr, hwAddr, status);
        myRef.child("Clients").child(hwAddr).setValue(client);
    }

    /**
     * Overrides super.
     * Updates clients on data changes.
     * @param dataSnapshot
     */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
            if(childSnapshot.getKey() == "Clients") {
                clients.removeAll(clients);
                for (DataSnapshot clientSnapshot : childSnapshot.getChildren()) {
                    clients.add(clientSnapshot.getValue(Client.class));
                }
            }
        }
    }

    /**
     * Overrides super.
     * Logs errors if action is cancelled.
     * @param e
     */
    @Override
    public void onCancelled(DatabaseError e){
        Log.e(TAG, e.getMessage());
    }

    /**
     * Gets the clients stored in the database
     * @return ArrayList of {@link Client}
     */
    public ArrayList<Client> getClients() {
        return (ArrayList) this.clients;
    }

}
