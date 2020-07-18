package com.example.sscheckout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    private Button backHomeButton;
    private TextView resultText;
    private TextView messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultText = (TextView) findViewById(R.id.result_text);
        messageText = (TextView) findViewById(R.id.message_text);

        final Intent lastIntent = getIntent();
        String message = lastIntent.getStringExtra("update result");
        if (message.equals("Successfully Updated")){
            resultText.setText("Hey! Success");
            double totalCost = lastIntent.getDoubleExtra("total cost", 0.0);
            String displayMsg = "$"+String.valueOf(totalCost)+" added to your tab";
            messageText.setText(displayMsg);
        }else{
            resultText.setText("Oops... Error");
            messageText.setText(message);
        }

        backHomeButton = (Button) findViewById(R.id.back_home_button);
        backHomeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //nothing done when back pressed
    }
}
