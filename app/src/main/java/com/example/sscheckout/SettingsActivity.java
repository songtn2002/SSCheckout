package com.example.sscheckout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

import static com.example.sscheckout.SheetHandler.REQUEST_AUTHORIZATION;
import static com.example.sscheckout.SheetHandler.createPrerequisites;
import static com.example.sscheckout.SheetHandler.mCredential;

public class SettingsActivity extends AppCompatActivity {

    public static HashMap<String, ItemInfo> merchList = new HashMap<>();
    public static String spreadsheetId;

    private EditText spreadsheetIdEdit;
    private ProgressDialog progressDialog;

    private static final int CLOSE_DIALOG = 3939;
    private static final int TOAST_TEXT = 3890;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case CLOSE_DIALOG:
                    progressDialog.hide();
                    break;
                case TOAST_TEXT:
                    Toast.makeText(SettingsActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    static{
        ItemInfo excelIQ = new ItemInfo("Excell IQ", 20.0);
        merchList.put("9400514010501", excelIQ);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        SettingsActivity.spreadsheetId = pref.getString("spreadsheet ID", "");

        spreadsheetIdEdit = (EditText) findViewById(R.id.spreadsheet_id);
        spreadsheetIdEdit.setText(spreadsheetId);
        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setTitle("Loading Merchandise Info");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button loadButton = (Button) findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                progressDialog.show();
                SettingsActivity.spreadsheetId = spreadsheetIdEdit.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Successfully Loaded";
                        try {
                            loadMerchInfo(spreadsheetId);
                        }catch(Exception e){
                            msg = "Error: "+e.getClass().getSimpleName()+" "+e.getMessage();
                        }
                        Message msg1 = new Message();
                        msg1.what = CLOSE_DIALOG;
                        handler.sendMessage(msg1);
                        Message msg2 = new Message();
                        msg2.what = TOAST_TEXT;
                        msg2.obj = msg;
                        handler.sendMessage(msg2);
                    }
                }).start();
            }
        });
    }

    private void loadMerchInfo (String spreadsheetId) throws IOException, GeneralSecurityException, Exception {
        // say we have 10 items, we can use C11 (first row is names) for lastCell. We can also use C12, C13 etc. (anything greater than 11), the result is the same.
        int numItems = SheetHandler.getNumOfItems(SettingsActivity.this);
        String lastCell = "C"+String.valueOf(2+numItems-1);
        Log.d("GoogleProblem", "Last Cell is "+lastCell);
        List<List<Object>> data = SheetHandler.getData(SettingsActivity.this, spreadsheetId, "Items", lastCell);
        if (data == null) Log.d("GoogleProblem", "null object");
        for(List<Object> item : data)
        {
            String serialCode = (String) item.get(0);
            String name = (String) item.get(1);
            double price = Double.parseDouble((String) item.get(2));
            ItemInfo info = new ItemInfo(name, price);
            merchList.put(serialCode, info);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SettingsActivity.spreadsheetId = this.spreadsheetIdEdit.getText().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("spreadsheet ID", SettingsActivity.spreadsheetId);
        editor.apply();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SheetHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                   Toast.makeText(SettingsActivity.this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    SheetHandler.createPrerequisites(SettingsActivity.this);
                }
                break;
            case SheetHandler.REQUEST_ACCOUNT_PICKER:
                Log.d("GoogleProblem", "account pick returned");
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(SheetHandler.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        Log.d("GoogleProblem", "accouunt pick set");
                        mCredential.setSelectedAccountName(accountName);
                        createPrerequisites(SettingsActivity.this);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    createPrerequisites(SettingsActivity.this);
                }
                break;
        }
    }
}
