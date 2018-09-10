package com.donatien.customaccesspoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.donatien.customaccesspoint.AccessPoint.AccessPointManager;
import com.donatien.customaccesspoint.AccessPoint.AccessPointSettings;
import com.donatien.customaccesspoint.AccessPoint.Client;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AccessPointManager apManager;
    private Handler handler;

    Switch switch_enable;

    Button button_settings;
    AccessPointSettings settings;

    TextView textView_ssid;
    TextView textView_password;

    ListView listView_clients;
    ClientAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Creates Access point manager */
        this.apManager = new AccessPointManager(this.getApplicationContext());
        this.apManager.showWritePermissionSettings(false);

        /* Turns previous AP off */
        this.apManager.stopWifiTethering();
        this.apManager.setWifiConfig(this.apManager.DEFAULT_SSID, this.apManager.DEFAULT_PASSWORD);

        /* Switch */
        switch_enable = (Switch) findViewById(R.id.switch_enable);
        // We set OnClick listener to get more control on the switch's state
        // If we set OnChange listener, there is trouble to bind AP state and switch state
        switch_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(apManager.isWifiApEnabled()) {
                    apManager.stopWifiTethering();
                } else {
                    apManager.startWifiTethering();
                }
            }
        });

        /* TextViews with informations */
        textView_ssid = (TextView) findViewById(R.id.textView_ssid);
        textView_password = (TextView) findViewById(R.id.textView_password);
        textView_ssid.setText("SSID : " + this.apManager.getWifiConfig().SSID);
        textView_password.setText("Password : " + this.apManager.getWifiConfig().preSharedKey);

        /* ListView of connected clients */
        this.adapter = new ClientAdapter(this, new ArrayList<Client>());
        listView_clients = (ListView) findViewById(R.id.listView_clients);
        listView_clients.setAdapter(adapter);
        final Activity activity = this;
        /* We set on click listener to start new activity in order to update client's information */
        listView_clients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Client client = (Client) parent.getItemAtPosition(position);
                Intent intent = new Intent(activity, ClientActivity.class);
                intent.putExtra(ClientActivity.CLIENT_NAME, client.getName());
                intent.putExtra(ClientActivity.CLIENT_IP, client.getIpAddr());
                intent.putExtra(ClientActivity.CLIENT_HW, client.getHwAddr());
                intent.putExtra(ClientActivity.CLIENT_STATUS, client.getStatus().toInteger());
                startActivityForResult(intent, ClientActivity.ACTIVITY_CODE);
            }
        });

        /* Button settings */
        button_settings = (Button) findViewById(R.id.button_setting);
        this.settings = new AccessPointSettings(activity, this.apManager);
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.showLayout();
            }
        });

        /* Required to perform async tasks */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /* UI refresh */
        handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                refreshUI();
                handler.postDelayed(this,100);
            }
        });
    }

    /**
     * Overrides super.
     * Adds the updates Client to FireBase when receiving back ClientActivity.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (ClientActivity.ACTIVITY_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    this.apManager.addKnownClient(
                            data.getStringExtra(ClientActivity.CLIENT_NAME),
                            data.getStringExtra(ClientActivity.CLIENT_IP),
                            data.getStringExtra(ClientActivity.CLIENT_HW),
                            data.getIntExtra(ClientActivity.CLIENT_STATUS, 0)
                    );

                }
                break;
            }
        }
    }

    /**
     * Overrides super.
     * Turs off wifi access point
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We stop the tethering
        this.apManager.stopWifiTethering();
    }

    /**
     * Updates elements of the UI.
     * Scans connected clients.
     */
    private void refreshUI() {
        // We get the ssid and password from the TextViews
        String ssid = textView_ssid.getText().toString().split(" ")[2];
        String password = textView_password.getText().toString().split(" ")[2];

        // Sets switch state according to apManager
        if(this.apManager.isWifiApEnabled()) {
            switch_enable.setChecked(true);
        } else {
            switch_enable.setChecked(false);
        }

        // If ssid or password changed we update views and disable switch
        if(!ssid.equals(apManager.getWifiConfig().SSID) || !password.equals(apManager.getWifiConfig().preSharedKey)) {
            ssid = apManager.getWifiConfig().SSID;
            password = apManager.getWifiConfig().preSharedKey;
            textView_ssid.setText("SSID : " + ssid);
            textView_password.setText("Password : " + password);
            switch_enable.setChecked(false);
        }

        // Scans connected clients
        scan();
    }

    /**
     * Scans the connected clients then updates the adapter and the view.
     */
    private void scan() {
        ArrayList<Client> clients = apManager.getClientList();
        this.adapter.clear();
        this.adapter.addAll(clients);
        this.adapter.notifyDataSetChanged();
    }

}