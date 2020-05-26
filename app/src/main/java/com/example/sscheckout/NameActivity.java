package com.example.sscheckout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NameActivity extends AppCompatActivity {

    private double totalCost;
    private String name;

    private Button nameConfirm;
    private EditText firstNameEdit;
    private EditText lastNameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        Intent lastIntent = getIntent();
        totalCost = lastIntent.getDoubleExtra("total cost", 0.0);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        firstNameEdit = (EditText) findViewById(R.id.first_name_edit);
        lastNameEdit = (EditText) findViewById(R.id.last_name_edit);
        nameConfirm = (Button) findViewById(R.id.name_confirm_button);
        nameConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NameActivity.this, ResultActivity.class);
                intent.putExtra("total cost", totalCost);
                startActivity(intent);
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
}
