package com.donatien.customaccesspoint.AccessPoint;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Class used to manage Android access point functionality.
 * Uses java reflection api to access hidden methods.
 * Build for android api 24.
 */
public class AccessPointManager implements Serializable{

    private static final String TAG = "AccessPointManager";

    public static final String DEFAULT_SSID = "DONA";
    public static final String DEFAULT_PASSWORD = "SUPERSECRET";

    private WifiManager wifiManager;
    private WifiConfiguration wifiConfig;
    private FireBaseClientDatabase firebase;

    private Context context;

    /**
     * Default constructor.
     * Gets the system's WifiManager.
     * Instantiates a new FireBase client.
     * @param context the context
     */
    public AccessPointManager(Context context) {
        this.context = context;
        // We get the system WifiManager instance
        this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        this.firebase = new FireBaseClientDatabase(context);
    }

    /**
     * Shows new activity to allow writing settings.
     * @param force to force permission settings even if system access is already granted
     */
    public void showWritePermissionSettings(boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (force || !Settings.System.canWrite(this.context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.context.startActivity(intent);
            }
        }
    }

    /**
     * Checks if the access point is enabled.
     * This methods uses java reflection to access hidden method in API 24.
     * @return true if wifi access point is enable
     */
    public boolean isWifiApEnabled() {
        try {
            // We get the "isWifiApEnabled" hidden method and store it into a variable
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            // We return the result of the method
            return (Boolean) method.invoke(wifiManager);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the WifiConfiguration of the access point.
     * This methods uses java reflection to access hidden method in API 24.
     * @return the WifiConfiguration instance
     */
    public WifiConfiguration getWifiApConfiguration() {
        try {
            // Same principle here
            Method method = this.wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            return (WifiConfiguration) method.invoke(this.wifiManager);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new wifi configuration.
     * @param ssid the SSID
     * @param password the pre-shared key
     */
    public void setWifiConfig(String ssid, String password) {
        WifiConfiguration myWifiConfiguration = new WifiConfiguration();
        myWifiConfiguration.SSID = ssid;
        myWifiConfiguration.preSharedKey = password;
        myWifiConfiguration.allowedKeyManagement.set(4); // Sets WPA2_PSK (hidden field)
        myWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        this.wifiConfig = myWifiConfiguration;
    }

    /**
     * Enable or disable the wifi access point.
     * This methods uses java reflection to access hidden method in API 24.
     * @param wifiConfiguration the wifi configuration used to set access point
     * @param enabled is true to enable access point and false to disable access point
     * @return true if succeed
     */
    private boolean setWifiApEnabled(WifiConfiguration wifiConfiguration, boolean enabled) {
        if(enabled) {
            this.wifiManager.setWifiEnabled(false);
        }
        try {
            // We get the "isWifiApEnabled" hidden method and store it into a variable
            // We have to pass WifiConfiguration class and boolean class because "setWifiApEnabled" takes args of that type
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.setAccessible(true);
            // We invoke the method, applied to wifiManager, with the following args
            return (Boolean) method.invoke(wifiManager, wifiConfiguration, enabled);
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getCause().getMessage()); // Java wraps exception in InvocationTargetExeption
            return false;
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    /**
     * Starts wifi tethering.
     * @return true if succeed
     */
    public boolean startWifiTethering() {
        if(this.wifiConfig == null) {
            setWifiConfig(DEFAULT_SSID, DEFAULT_PASSWORD);
        }
        return (Boolean) setWifiApEnabled(this.wifiConfig, true);
    }

    /**
     * Stops wifi tethering.
     * @return true if succeed
     */
    public boolean stopWifiTethering() {
        try {
            return (Boolean) setWifiApEnabled(this.wifiConfig, false);
        } catch (Exception e) {
            WifiConfiguration default_config = new WifiConfiguration();
            default_config.SSID = DEFAULT_SSID;
            default_config.allowedAuthAlgorithms.set(0);
            return (Boolean) setWifiApEnabled(default_config, false);
        }
    }

    /**
     * Gets a list of the clients connected to the Hotspot.
     * Updates that list with FireBase Database.
     * @return ArrayList of {@link Client}
     */
    public ArrayList<Client> getClientList() {
        /* Gets the list of connected clients */
        BufferedReader br = null;
        ArrayList<Client> result = null;
        try {
            result = new ArrayList<Client>();
            // Connected clients are stored in "/proc/net/arp"
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            // We read the file
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");

                if ((splitted != null) && (splitted.length >= 4)) {
                    String mac = splitted[3];

                    if (mac.matches("..:..:..:..:..:..")) {
                        result.add(new Client(splitted[0], splitted[3]));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        /* Updates informations according to firebase */
        for(Client client : result) {
            updateClientInfos(client);
        }
        return result;
    }

    /**
     * Updates a client by checking if it's registered in database.
     * @param client the client to update
     * @return the updated client
     */
    public Client updateClientInfos(Client client) {
        ArrayList<Client> registeredClients = this.firebase.getClients();
        for(Client registeredClient : registeredClients) {
            // if Physical address is known, we set name and status
            // We can do that because the database is indexed by MAC addr (so every MAC address is unique)
            if(client.getHwAddr().equals(registeredClient.getHwAddr())) {
                client.setName(registeredClient.getName());
                client.setStatus(registeredClient.getStatus());
            }
        }
        return client;
    }

    /**
     * Gets the wifi configuration.
     * @return
     */
    public WifiConfiguration getWifiConfig() {
        return wifiConfig;
    }

    /**
     * Adds a client to FireBase database.
     * @param name the name
     * @param ipAddr the IP address
     * @param hwAddr the MAC address
     * @param status the status
     */
    public void addKnownClient(String name, String ipAddr, String hwAddr, int status) {
        this.firebase.writeNewClient(name, ipAddr, hwAddr, status);
    }

}

