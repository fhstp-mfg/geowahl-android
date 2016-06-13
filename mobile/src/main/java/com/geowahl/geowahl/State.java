package com.geowahl.geowahl;

import android.content.Intent;
import android.net.Uri;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class State extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";


    private static String url = "http://geowahl.suits.at/";
    private static String url_part2 = "/states";

    Button location;
    GPSTracker gps;
    String electionslug,locationurl;;

    ArrayList<String> colorList;

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        // Build a new GoogleApiClient
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();

        Bundle b = getIntent().getExtras();
        electionslug = b.getString("electionSlug");
        String url = State.url + electionslug + url_part2;
        Log.d("slug", electionslug);
        colorList = b.getStringArrayList("colorList");

        location = (Button) findViewById(R.id.location);
        listView = (ListView) findViewById(R.id.listView);

        getStates(url);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGPS();
            }
        });
    }

    public void getGPS() {
        gps = new GPSTracker(State.this);

        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Log.d("lat", String.valueOf(latitude));
            Log.d("lon", String.valueOf(longitude));
            locationurl = url + electionslug + "/" + latitude + "," + longitude;

            Log.d("locationurl", locationurl);
            getResult(locationurl);

        } else {
            gps.showSettingAlerts();
        }
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

    public void getStates(String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url_to_api, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject  response) {

                try {

                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                    final JSONArray array = response.getJSONArray("states");
                    Log.d("states",array.toString());
                    for (int i = 0; i < array.length(); i++) {

                        JSONObject object = array.getJSONObject(i);

                        String stateName = object.getString(Config.TAG_NAME);
                        String stateSlug = object.getString(Config.TAG_SLUG);

                        HashMap<String, String> d = new HashMap<>();
                        d.put("name", stateName);
                        d.put("slug", stateSlug);

                        arrayList.add(d);

                        Log.d("wahlslug",">"+ electionslug);

                        ListAdapter adapter = new SimpleAdapter(
                                State.this, arrayList,
                                R.layout.activity_listview, new String[]{Config.TAG_NAME}, new int[]{R.id.name});

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {

                                //ausgewählte Wahl
                                Log.d("array", arrayList.get((int)id).get(Config.TAG_NAME));

                                Intent i = new Intent(State.this, District.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("electionSlug",electionslug);
                                bundle.putString("stateSlug",arrayList.get((int)id).get(Config.TAG_SLUG));
                                bundle.putString("statename", arrayList.get((int)id).get(Config.TAG_NAME));
                                bundle.putStringArrayList("colorList", colorList);

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
                Log.d("response",response.toString());

                try {
                    JSONObject district = response.getJSONObject("district");
                    String districtname = district.getString("name");
                    String districtid = district.getString("id");
                    JSONObject state = response.getJSONObject("state");
                    String stateslug = state.getString("slug");
                    String statename = state.getString("name");
                    Log.d("slug",stateslug);

                    JSONArray results1 = district.getJSONArray("results");
                    String url = State.url +electionslug+"/"+stateslug+"/"+districtid;

                    ArrayList<Integer> voteslist = new ArrayList<>();
                    for(int i=0;i < results1.length();i++) {
                        JSONObject obj = results1.getJSONObject(i);
                        Integer votes = Integer.parseInt(obj.getString(Config.TAG_VOTES));
                        String name = obj.getString(Config.TAG_NAME);

                        voteslist.add(votes);

                    }

                    Integer maxVotes = Collections.max(voteslist);
                    String maxParty = null;
                    for(int i=0; i < voteslist.size(); i++) {
                        if (voteslist.get(i) == maxVotes) {
                            maxParty = results1.getJSONObject(i).getString(Config.TAG_NAME);
                        }
                    }


                    //wearable
                    dataMap.putString("district", districtname);
                    dataMap.putString("statename", statename);
                    dataMap.putString("maxParty", maxParty);
                    dataMap.putStringArrayList("colorList", colorList);
                    new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();


                    Intent i = new Intent(State.this, Webview.class);
                    Bundle bundle = new Bundle();
                    Log.d("locationurl", url);
                    bundle.putString("url", url);
                    i.putExtras(bundle);
                    startActivity(i);
                    overridePendingTransition(R.animator.activity_in, R.animator.activity_out);

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

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "State Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.geowahl.geowahl/http/host/path")
        );
        AppIndex.AppIndexApi.start(googleClient, viewAction);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "State Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.geowahl.geowahl/http/host/path")
        );
        AppIndex.AppIndexApi.end(googleClient, viewAction);
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
