package com.donatien.customaccesspoint.AccessPoint;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.donatien.customaccesspoint.R;

/**
 * Class used to set SSID and password to access point.
 * Displays an AlertDialog.
 */
public class AccessPointSettings {

    private Activity activity;
    private AccessPointManager apManager;

    /**
     * Constructor.
     * @param activity the activity where to alert
     * @param apManager the access point to modify
     */
    public AccessPointSettings(Activity activity, AccessPointManager apManager) {
        this.activity = activity;
        this.apManager = apManager;
    }

    /**
     * Shows a layout in an AlertDialogue.
     * Asks for SSID and password.
     * If form isn't valid (SSID equal to "" or password's length less than 8), SSID and password seted to default.
     */
    public void showLayout() {
        /* Inflates layout */
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setTitle("Settings");
        View viewInflated = LayoutInflater.from(this.activity).inflate(
                R.layout.input_settings,
                (ViewGroup) this.activity.findViewById(android.R.id.content),
                false
        );

        final EditText input_ssid = (EditText) viewInflated.findViewById(R.id.input_ssid);
        final EditText input_password = (EditText) viewInflated.findViewById(R.id.input_password);

        /*
        * When form is submitted, checks if it's valid and sets settings
        * Turns access point off
        */
        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input_ssid.getText().toString().equals("")) {
                    // Default settings
                    apManager.setWifiConfig(apManager.DEFAULT_SSID, apManager.DEFAULT_PASSWORD);
                    Toast.makeText(activity, "SSID cannot be empty.", Toast.LENGTH_LONG).show();
                    apManager.stopWifiTethering();
                    dialog.dismiss();
                } else if (input_password.getText().length() < 8) {
                    // Default settings
                    apManager.setWifiConfig(apManager.DEFAULT_SSID, apManager.DEFAULT_PASSWORD);
                    Toast.makeText(activity, "Password must be at least 8 characters long.", Toast.LENGTH_LONG).show();
                    apManager.stopWifiTethering();
                    dialog.dismiss();
                } else {
                    // Inputed settings
                    apManager.setWifiConfig(input_ssid.getText().toString(), input_password.getText().toString());
                    Toast.makeText(activity, "SSID and Pasword changed to given values.", Toast.LENGTH_LONG).show();
                    apManager.stopWifiTethering();
                    dialog.dismiss();
                }
            }
        });
        /*
        * When form is cancelled, dismisses the dialog
        * Keeps access point on
        */
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
