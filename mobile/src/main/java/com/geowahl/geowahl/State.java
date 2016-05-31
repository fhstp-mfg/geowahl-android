package com.geowahl.geowahl;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class State extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);


        ListView listview = (ListView) findViewById(R.id.listView);
        String[] values = new String[]{"Standort", "Wien", "Niederösterreich"};


        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, values);

        listview.setAdapter(adapter);

        listview.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.darkblue));


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Intent i = new Intent(State.this, District.class);
                startActivity(i);
                overridePendingTransition(R.animator.activity_in, R.animator.activity_out);
            }

        });
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

