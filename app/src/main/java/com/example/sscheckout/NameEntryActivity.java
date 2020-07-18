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

import static com.example.sscheckout.SheetHandler.REQUEST_AUTHORIZATION;
import static com.example.sscheckout.SheetHandler.createPrerequisites;
import static com.example.sscheckout.SheetHandler.mCredential;

public class NameEntryActivity extends AppCompatActivity {

    private double totalCost;
    private String name;

    private Button nameConfirm;
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private ProgressDialog progressDialog;

    private static final int CLOSE_DIALOG = 2929;
    private static final int NEXT_ACTIVITY = 1093;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case CLOSE_DIALOG:
                    progressDialog.hide();
                    break;
                case NEXT_ACTIVITY:
                    Intent intent = new Intent(NameEntryActivity.this, ResultActivity.class);
                    intent.putExtra("update result", (String) msg.obj);
                    intent.putExtra("total cost", totalCost);
                    startActivity(intent);
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_entry);

        final Intent lastIntent = getIntent();
        totalCost = lastIntent.getDoubleExtra("total cost", 0.0);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        progressDialog = new ProgressDialog(NameEntryActivity.this);
        progressDialog.setTitle("Updating Tab");
        progressDialog.setMessage("Please wait...");

        firstNameEdit = (EditText) findViewById(R.id.first_name_edit);
        lastNameEdit = (EditText) findViewById(R.id.last_name_edit);
        nameConfirm = (Button) findViewById(R.id.name_confirm_button_1);
        nameConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String firstName = firstNameEdit.getText().toString();
                String lastName = lastNameEdit.getText().toString();
                final String name = firstName+" "+lastName;
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Successfully Updated";
                        try {
                            SheetHandler.addNewTab(NameEntryActivity.this, name, -totalCost);
                        }catch(Exception e){
                            msg = e.getClass().getSimpleName()+" "+e.getMessage();
                        }
                        Log.e("GoogleProblem", msg);
                        Message msg1 = new Message();
                        msg1.what = CLOSE_DIALOG;
                        handler.sendMessage(msg1);

                        Message msg2 = new Message();
                        msg2.what = NEXT_ACTIVITY;
                        msg2.obj = msg;
                        handler.sendMessage(msg2);
                    }
                }).start();
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SheetHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(NameEntryActivity.this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    SheetHandler.createPrerequisites(NameEntryActivity.this);
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
                        createPrerequisites(NameEntryActivity.this);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    createPrerequisites(NameEntryActivity.this);
                }
                break;
        }
    }
}
