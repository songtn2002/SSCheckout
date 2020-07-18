package com.example.sscheckout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private TextView totalText;

    private static final String TAG = "CartActivity";

    private List<ItemInfo> cart = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, ScannerActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        listView = (ListView) findViewById(R.id.cart_list_view);
        totalText = (TextView) findViewById(R.id.total_text);
        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, QuestionActivity.class);
                intent.putExtra("total cost", getTotalCost());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    ItemInfo addedItem = (ItemInfo) data.getSerializableExtra("item_info");
                    cart.add(addedItem);
                    MerchItemAdapter adapter = new MerchItemAdapter(CartActivity.this, R.layout.merch_item, cart);
                    listView.setAdapter(adapter);
                    totalText.setText(String.format("Total: $%.2f", getTotalCost()));
                    Log.d(TAG, "listView Reloaded");
                }
                break;
            default:
                break;
        }

    }

    private double getTotalCost (){
        double result = 0;
        for (ItemInfo item: cart){
            result += item.getPrice();
        }
        return result;
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
