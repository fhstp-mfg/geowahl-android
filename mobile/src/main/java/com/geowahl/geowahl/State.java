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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class State extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG_NAME = "name";
    private static final String TAG_SLUG = "slug";
    private static final String TAG_VOTES = "votes";
    private static final String TAG_PERCENT = "percent";
    private static final String TAG_EXACT = "exact";
    private static String url_part1 = "http://geowahl.suits.at/";
    private static String url_part2 = "/states";

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";
    Button location;
    GPSTracker gps;
    String wahlslug,electionslug,locationurl;

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        Bundle b = new Bundle();
        b = getIntent().getExtras();
        wahlslug = b.getString("wahlSlug");
        Log.d("slug", wahlslug);
        electionslug = b.getString("electionSlug");

        location = (Button) findViewById(R.id.location);
        listView = (ListView) findViewById(R.id.listView);

        location.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        gps = new GPSTracker(State.this);

                        if(gps.canGetLocation()){
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();

                            Log.d("lat", String.valueOf(latitude));
                            Log.d("lon", String.valueOf(longitude));
                            locationurl = url_part1+electionslug+"/"+latitude+","+longitude;
                            getResult(locationurl);

                        } else {
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

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url_to_api, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject  response) {

                try {

                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                    JSONArray array = response.getJSONArray("states");
                    Log.d("states",array.toString());
                    for (int i = 0; i < array.length(); i++) {

                        JSONObject object = array.getJSONObject(i);

                        String stateName = object.getString(TAG_NAME);
                        String stateSlug = object.getString(TAG_SLUG);

                        HashMap<String, String> d = new HashMap<>();
                        d.put("name", stateName);
                        d.put("slug", stateSlug);

                        arrayList.add(d);

                        Log.d("wahlslug",">"+ wahlslug);

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

                                Intent i = new Intent(State.this, District.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("wahlSlug",wahlslug);
                                bundle.putString("electionSlug",electionslug);
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

    public void getResult(final String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url_to_api, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("respns",response.toString());

                try {
                    JSONObject district = response.getJSONObject("district");
                    String districtname = district.getString("name");

                    JSONArray results1 = district.getJSONArray("results");

                    dataMap.putString("districtname", districtname);

                    for(int i=0;i < results1.length();i++){
                        JSONObject obj = results1.getJSONObject(i);

                        dataMap.putString("name_"+i, obj.getString(TAG_NAME));
                        dataMap.putString("votes_"+i, obj.getString(TAG_VOTES));
                    }

                    JSONObject state = response.getJSONObject("state");
                    String statename = district.getString("name");
                    JSONArray results2 = district.getJSONArray("results");

                    dataMap.putString("statename", statename);

                    for(int i=0;i < results2.length();i++){
                        JSONObject obj = results2.getJSONObject(i);

                        dataMap.putString("name_"+i, obj.getString(TAG_NAME));
                        dataMap.putString("votes_"+i, obj.getString(TAG_VOTES));
                        dataMap.putString("percent_"+i, obj.getString(TAG_PERCENT));
                        dataMap.putString("exact_"+i, obj.getString(TAG_EXACT));
                    }
                    new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                Log.d("response", response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("test",error.getMessage());
                VolleyLog.d("error", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
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

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "DataMap: " + dataMap + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send DataMap to data layer");
            }
        }
    }
}

