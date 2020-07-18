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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.jar.Attributes;

import static com.example.sscheckout.SheetHandler.REQUEST_AUTHORIZATION;
import static com.example.sscheckout.SheetHandler.createPrerequisites;
import static com.example.sscheckout.SheetHandler.mCredential;

public class NameChoiceActivity extends AppCompatActivity {

    private List<TabInfo> tabList;

    private double totalCost;
    private TabInfo selected;

    private ProgressDialog loadDialog;
    private ProgressDialog updateDialog;
    private ListView nameListView;
    private Button nameConfirmButton2;


    private static final int LOAD_LIST = 6892;
    private static final int TO_RES_ACTIVITY = 4039;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case LOAD_LIST:
                    loadDialog.hide();
                    nameListView.setAdapter(new NameAdapter(NameChoiceActivity.this, R.layout.name_item, tabList));
                    break;
                case TO_RES_ACTIVITY:
                    loadDialog.hide();
                    Intent intent = new Intent(NameChoiceActivity.this, ResultActivity.class);
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
        setContentView(R.layout.activity_name_choice);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        final Intent lastIntent = getIntent();
        totalCost = lastIntent.getDoubleExtra("total cost", 0.0);

        loadDialog = new ProgressDialog(NameChoiceActivity.this);
        loadDialog.setTitle("Loading Tabs");
        loadDialog.setMessage("Please wait...");
        loadDialog.setCancelable(false);

        updateDialog = new ProgressDialog(NameChoiceActivity.this);
        updateDialog.setTitle("Updating Tabs");
        updateDialog.setMessage("Please wait...");
        updateDialog.setCancelable(false);

        nameListView = (ListView) NameChoiceActivity.this.findViewById(R.id.name_choice_listview);
        nameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selected = (TabInfo) nameListView.getItemAtPosition(position);
                    //Log.d("ListView", selected.toString());
            }
        });

        nameConfirmButton2 = (Button) findViewById(R.id.name_confirm_button_2);
        nameConfirmButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.show();
                selected.setTab(selected.getTab()-totalCost);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Successfully Updated";
                        try {
                            SheetHandler.updateTab(NameChoiceActivity.this, selected);
                        }catch(Exception e){
                            msg = e.getClass().getSimpleName()+" "+e.getMessage();
                        }
                        Log.e("GoogleProblem", msg);

                        Message msg1 = new Message();
                        msg1.what = TO_RES_ACTIVITY;
                        msg1.obj = msg;
                        handler.sendMessage(msg1);
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg = "Successfully Loaded";
                try {
                    NameChoiceActivity.this.tabList = SheetHandler.getTabs(NameChoiceActivity.this);
                }catch(Exception e){
                    msg = e.getClass().getSimpleName()+" "+e.getMessage();
                }
                if (msg == "Successfully Loaded"){
                    Message msg1 = new Message();
                    msg1.what = LOAD_LIST;
                    handler.sendMessage(msg1);
                }else{
                    Message msg1 = new Message();
                    msg1.what = TO_RES_ACTIVITY;
                    msg1.obj = msg;
                    handler.sendMessage(msg1);
                }
            }
        }).start();
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
                    Toast.makeText(NameChoiceActivity.this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    SheetHandler.createPrerequisites(NameChoiceActivity.this);
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
                        createPrerequisites(NameChoiceActivity.this);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    createPrerequisites(NameChoiceActivity.this);
                }
                break;
        }
    }
}
