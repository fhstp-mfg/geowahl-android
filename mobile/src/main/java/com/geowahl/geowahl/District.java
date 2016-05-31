package com.geowahl.geowahl;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class District extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        ListView listview = (ListView) findViewById(R.id.listView);
       // String[] values = new String[] {"alle", "Innere Stadt", "Leopoldstadt", "Landstraße"};
        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

        ArrayList<String> items = new ArrayList<String>();

        try {
            JSONArray arr = new JSONArray(loadJSONFromAsset());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jObj = arr.getJSONObject(i);
                String name = jObj.getString("name");
                items.add(name);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.activity_listview, items);

                listview.setAdapter(adapter);

                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view,
                                            int position, long id) {
                        Intent i = new Intent(District.this, Webview.class);
                        startActivity(i);
                        overridePendingTransition(R.animator.activity_in, R.animator.activity_out);
                    }

                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Load Json from Assets
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("Wien.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                //this.finish();
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.animator.activity_back_in, R.animator.activity_back_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
