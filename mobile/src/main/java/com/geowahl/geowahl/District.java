package com.geowahl.geowahl;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class District extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";
    String name;

    private static final String TAG_NAME = "name";
    private static final String TAG_SLUG = "slug";
    private static String url_part1 = "http://geowahl.suits.at/";
    private static String url_part2 = "/districts";
    String wahlslug, stateslug, electionslug;

    String districtName;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(true);

        listView = (ListView) findViewById(R.id.listView);

        Bundle b = new Bundle();
        b = getIntent().getExtras();
        wahlslug = b.getString("wahlSlug");
        stateslug = b.getString("stateSlug");
        electionslug = b.getString("electionSlug");

        String url = url_part1+wahlslug+"/"+stateslug+url_part2;
        Log.d("url",url);
        Log.d("wahlslug",">"+ wahlslug);

        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getDistricts(url);
    }

    public void getDistricts(String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest req = new JsonArrayRequest(url_to_api,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                    for (int j = 0; j < response.length(); j++) {

                        JSONObject obj = (JSONObject) response.get(j);
                        Log.d("district",obj.getString(TAG_NAME).toString());

                        districtName = obj.getString(TAG_NAME);


                        HashMap<String, String> d = new HashMap<>();
                        d.put("name", districtName);
                        arrayList.add(d);

                        ListAdapter adapter = new SimpleAdapter(
                                District.this, arrayList,
                                R.layout.activity_listview, new String[]{TAG_NAME}, new int[]{R.id.name});

                        listView.setAdapter(adapter);
                        final int finalJ = j;
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {

                                //ausgewählte Wahl
                                Log.d("array", arrayList.get((int)id).get(TAG_NAME));

                                //dataMap.putString("district", districtName);
                                dataMap.putString("district", arrayList.get((int)id).get(TAG_NAME));
                                new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();

                                Intent i = new Intent(District.this, Webview.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("stateSlug",stateslug);
                                bundle.putString("electionSlug",electionslug);
                                bundle.putInt("districtId", finalJ);
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
                Toast.makeText(District.this, error.getMessage(),
                        Toast.LENGTH_LONG).show();
                VolleyLog.d("error", "Error: " + error.getMessage());

            }
        });

        queue.add(req);
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
                this.finish();
                //NavUtils.navigateUpFromSameTask(this);
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, intent);
                overridePendingTransition(R.animator.activity_back_in, R.animator.activity_back_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {


        // Create a DataMap object and send it to the data layer

        //dataMap.putLong("time", new Date().getTime());
        //dataMap.putString("district", name);
        //dataMap.putString("electionslug", electionslug);
        //dataMap.putString("stateslug", stateslug);
        //dataMap.putString("front", "250");
        //dataMap.putString("middle", "260");
        //dataMap.putString("back", "270");
        //Requires a new thread to avoid blocking the UI


    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
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
