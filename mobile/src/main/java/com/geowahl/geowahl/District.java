package com.geowahl.geowahl;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class District extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";

    private static String url_part1 = "http://geowahl.suits.at/";
    private static String url_part2 = "/districts";
    String stateslug, electionslug, statename;

    String districtName,districtId;

    ArrayList<String> colorList = new ArrayList<>();
    ArrayList<String> partyList = new ArrayList<>();

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(true);

        listView = (ListView) findViewById(R.id.listView);

        Bundle b = getIntent().getExtras();
        stateslug = b.getString("stateSlug");
        electionslug = b.getString("electionSlug");
        statename = b.getString("statename");
        colorList = b.getStringArrayList("colorList");
        partyList = b.getStringArrayList("partyList");

        String url = url_part1 +electionslug+"/"+stateslug+url_part2;
        Log.d("uuurl",url);
        Log.d("wahlslug",">"+ electionslug);

        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getDistricts(url,stateslug);
    }

    public void getDistricts(String url_to_api, final String state) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url_to_api, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
                    final JSONArray array = response.getJSONArray("districts");

                    for (int j = 0; j < array.length(); j++) {

                        JSONObject object = array.getJSONObject(j);
                        //Log.d("district",object.getString(TAG_NAME).toString());

                        districtName = object.getString(Config.TAG_NAME);
                        districtId = object.getString("id");
                        Log.d("DistrictID", districtId);

                        HashMap<String, String> d = new HashMap<>();
                        d.put("name", districtName);
                        d.put("id", districtId);
                        arrayList.add(d);


                        ListAdapter adapter = new SimpleAdapter(
                                District.this, arrayList,
                                R.layout.activity_listview, new String[]{Config.TAG_NAME, "id"}, new int[]{R.id.name});

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {

                                String selectedName = arrayList.get((int)id).get(Config.TAG_NAME);
                                Log.d("selectedName", selectedName);

                                String selectedId = arrayList.get((int)id).get("id");
                                Log.d("selectedId", selectedId);

                                String urlToShowChart = url_part1 +electionslug+"/"+state+"/"+selectedId;
                                //Log.d("tfouzg",state+electionslug+selectedId);


                                //wearable
                                dataMap.putString("district", selectedName);
                                dataMap.putString("statename", statename);
                                Log.d("urltoshowchatrts",urlToShowChart);
                                getVotes(urlToShowChart);

                                Intent i = new Intent(District.this, Webview.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("url",urlToShowChart);
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


    public void getVotes(String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url_to_api, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("response", response.toString());

                try {
                    JSONObject district = response.getJSONObject("district");
                    JSONArray results = district.getJSONArray("results");

                    ArrayList<Integer> voteslist = new ArrayList<>();
                    ArrayList<String> percentlist = new ArrayList<>();
                    ArrayList<String> namelist = new ArrayList<>();
                    for(int i=0;i < results.length();i++) {
                        JSONObject obj = results.getJSONObject(i);
                        Integer votes = Integer.parseInt(obj.getString(Config.TAG_VOTES));
                        voteslist.add(votes);

                        String name = obj.getString(Config.TAG_NAME);
                        namelist.add(name);

                        String percent = obj.getString(Config.TAG_PERCENT);
                        percentlist.add(percent);

                    }

                    Integer maxVotes = Collections.max(voteslist);
                    String maxParty = null;
                    String maxPercent = null;
                    for(int i=0; i < voteslist.size(); i++) {
                        if (voteslist.get(i) == maxVotes) {
                            maxParty = results.getJSONObject(i).getString(Config.TAG_NAME);
                            maxPercent = results.getJSONObject(i).getString(Config.TAG_PERCENT);
                        }
                    }

                    String maxColor = null;
                    for(int i = 0; i < partyList.size(); i++){
                        if(partyList.get(i).equalsIgnoreCase(maxParty)){
                            maxColor = colorList.get(i);
                        }
                    }

                    dataMap.putString("maxParty", maxParty);
                    dataMap.putString("maxColor", maxColor);
                    dataMap.putString("maxPercent", maxPercent);
                    dataMap.putStringArrayList("colorList", colorList); //alle Farben
                    dataMap.putStringArrayList("partyList", partyList); // alle Parteien
                    dataMap.putStringArrayList("nameList", namelist); //Namen aller Parteien aus dem JSON
                    dataMap.putIntegerArrayList("votesList", voteslist); //Votes aller Parteien aus dem JSON
                    dataMap.putStringArrayList("percentList", percentlist); //% aller Parteien aus dem JSON
                    Log.d("dataMap",dataMap.toString());

                    new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();

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
