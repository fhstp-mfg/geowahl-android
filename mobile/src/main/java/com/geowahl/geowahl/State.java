package com.geowahl.geowahl;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class State extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG_NAME = "name";
    private static final String TAG_SLUG = "slug";
    private static String url_part1 = "http://geowahl.suits.at/";
    private static String url_part2 = "/states";

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";
    Button location;
    GPSTracker gps;
    String wahlslug;

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        Bundle b = new Bundle();
        b = getIntent().getExtras();
        wahlslug = b.getString("wahlSlug");

        location = (Button) findViewById(R.id.location);

        location.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        gps = new GPSTracker(State.this);

                        if(gps.canGetLocation()){
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();

                            Log.d("lat", String.valueOf(latitude));
                            Log.d("lon", String.valueOf(longitude));
                        }else {
                            gps.showSettingAlerts();
                        }
                    }
                }
        );

        String url = url_part1+wahlslug+url_part2;
        //Log.d("url",url);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getStates(url);
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

    public void getStates(String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest req = new JsonArrayRequest(url_to_api,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {

                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject obj = (JSONObject) response.get(i);
                        //Log.d("state",obj.getString(TAG_NAME).toString());

                        String stateName = obj.getString(TAG_NAME);
                        String stateSlug = obj.getString(TAG_SLUG);

                        Log.d("stateName",stateName);
                        Log.d("stateSlug",stateSlug);
                        HashMap<String, String> d = new HashMap<>();
                        d.put("name", stateName);
                        d.put("slug", stateSlug);

                        arrayList.add(d);

                        ListAdapter adapter = new SimpleAdapter(
                                State.this, arrayList,
                                R.layout.activity_listview, new String[]{TAG_NAME}, new int[]{R.id.name});

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {

                                //ausgewählte Wahl
                                Log.d("array", arrayList.get((int)id).get(TAG_NAME));

                                Intent i = new Intent(State.this, State.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("wahlSlug",arrayList.get((int)id).get(wahlslug));
                                bundle.putString("stateSlug",arrayList.get((int)id).get(TAG_SLUG));
                                i.putExtras(bundle);
                                startActivity(i);
                                overridePendingTransition(R.animator.activity_in, R.animator.activity_out);
                            }

                        });

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(State.this, error.getMessage(),
                        Toast.LENGTH_LONG).show();
                VolleyLog.d("error", "Error: " + error.getMessage());

            }
        });

        queue.add(req);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

