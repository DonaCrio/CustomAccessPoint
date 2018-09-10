package com.donatien.customaccesspoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.donatien.customaccesspoint.AccessPoint.Client;
import com.donatien.customaccesspoint.AccessPoint.ClientStatus;

public class ClientActivity extends AppCompatActivity {

    public static final int ACTIVITY_CODE = 1;
    private static final String TAG = "ClientActivity";

    public static final String CLIENT_NAME = "name";
    public static final String CLIENT_IP = "ip";
    public static final String CLIENT_HW = "hw";
    public static final String CLIENT_STATUS = "status";

    TextView textView_name, textView_ip, textView_hw, textView_status;
    EditText editText_name;
    RadioGroup radioGroup;
    RadioButton radioButton_approved, radioButton_dangerous, radioButton_unknown;
    Button button_cancel, button_update;

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        /* Retrieves client's informations in the intent then construct*/
        Intent intent = getIntent();
        String name = intent.getStringExtra(CLIENT_NAME);
        String ip = intent.getStringExtra(CLIENT_IP);
        String hw = intent.getStringExtra(CLIENT_HW);
        int status = intent.getIntExtra(CLIENT_STATUS, 0);
        client = new Client(name, ip, hw, status);

        /* Initializes Views */
        textView_name = (TextView) findViewById(R.id.textView_name);
        textView_name.setText(client.getName());

        textView_ip = (TextView) findViewById(R.id.textView_ip);
        textView_ip.setText(client.getIpAddr());

        textView_hw = (TextView) findViewById(R.id.textView_hw);
        textView_hw.setText(client.getHwAddr());

        textView_status = (TextView) findViewById(R.id.textView_status);

        editText_name = (EditText) findViewById(R.id.editText_name);
        editText_name.setText(client.getName());

        /* Initializes radio group */
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // We update client's status according to the checked radio button
                if (checkedId == R.id.radioButton_approved) {
                    client.setStatus(ClientStatus.APPROVED);
                } else if (checkedId == R.id.radioButton_dangerous) {
                    client.setStatus(ClientStatus.DANGEROUS);
                } else {
                    client.setStatus(ClientStatus.UNKNOWN);
                }
            }
        });

        radioButton_approved = (RadioButton) findViewById(R.id.radioButton_approved);
        radioButton_dangerous = (RadioButton) findViewById(R.id.radioButton_dangerous);
        radioButton_unknown = (RadioButton) findViewById(R.id.radioButton_unknown);

        /* Sets status related Views and buttons */
        switch(client.getStatus().toInteger()) {
            case -1:
                textView_status.setText("Dangerous");
                radioGroup.check(radioButton_dangerous.getId());
                break;
            case 0:
                textView_status.setText("Unknown");
                radioGroup.check(radioButton_unknown.getId());
                break;
            case 1:
                textView_status.setText("Approved");
                radioGroup.check(radioButton_approved.getId());
                break;
            default:
                textView_status.setText("Unknown");
                radioGroup.check(radioButton_unknown.getId());
                break;
        }

        button_cancel = (Button) findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button_update = (Button) findViewById(R.id.button_update);
        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Sends back Client fields into intent */
                Intent resultIntent = new Intent();
                resultIntent.putExtra(CLIENT_NAME, editText_name.getText().toString());
                resultIntent.putExtra(CLIENT_IP, client.getIpAddr());
                resultIntent.putExtra(CLIENT_HW, client.getHwAddr());
                resultIntent.putExtra(CLIENT_STATUS, client.getStatus().toInteger());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
