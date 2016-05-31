package com.geowahl.geowahl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listview = (ListView) findViewById(R.id.listView);
        String[] values = new String[] { "Gemeinderatswahlen", "Bundespräsidentenwahlen" };


        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, values);

        listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Intent i = new Intent(MainActivity.this, State.class);
                startActivity(i);
                overridePendingTransition(R.animator.activity_in, R.animator.activity_out);
            }

        });
    }
}