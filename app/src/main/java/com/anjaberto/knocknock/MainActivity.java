package com.anjaberto.knocknock;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    NfcAdapter myAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;

    ArrayList<String> s = new ArrayList<>();
    String uri = new String();

    String[][] techListsArray;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent myIntent = new Intent();
        myIntent = getIntent();

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter td = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        intentFiltersArray = new IntentFilter[] {
                ndef, td
        };

        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };

        onNewIntent(myIntent);


    }

    public void onPause() {
        super.onPause();
        myAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        myAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    public void onNewIntent(Intent intent) {


        //Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                    NdefRecord[] recs = ((NdefMessage) data[i]).getRecords();
                    for (int j = 0; j < recs.length; j++) {
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            s.add(new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding));
                        }
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(recs[j].getType(), NdefRecord.RTD_URI)) {
                            //Log.d("URI", "reading");
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;

                            uri += new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding);
                            //Log.d("URI", uri);
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        EditText name = (EditText) findViewById(R.id.userName);
        EditText email = (EditText) findViewById(R.id.userEmail);
        EditText department = (EditText) findViewById(R.id.userDept);
        EditText office = (EditText) findViewById(R.id.userOffice);
        EditText hours = (EditText) findViewById(R.id.userHours);

        name.setText(s.get(0));
        email.setText(uri);
        department.setText(s.get(1));
        office.setText(s.get(2));
        hours.setText(s.get(3));

    }
}
