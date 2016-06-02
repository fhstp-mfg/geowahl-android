package com.geowahl.geowahl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Response.ErrorListener, Response.Listener<JSONObject> {

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";

    // URL to get contacts JSON
    private static String url = "http://geowahl.suits.at/elections";

    // JSON Node names
    private static final String TAG_NAME = "name";
    private static final String TAG_SLUG = "slug";

    String electionName;
    String electionSlug;


    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getElections(url);
    }

    public void getElections(String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);



        JsonArrayRequest req = new JsonArrayRequest(url_to_api,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject obj = (JSONObject) response.get(i);
                        Log.d("wahl",obj.getString(TAG_NAME).toString());

                        electionName = obj.getString(TAG_NAME);
                        electionSlug = obj.getString(TAG_SLUG);

                        HashMap<String, String> d = new HashMap<>();
                        d.put("name", electionName);
                        d.put("slug", electionSlug);

                        arrayList.add(d);

                        ListAdapter adapter = new SimpleAdapter(
                                MainActivity.this, arrayList,
                                R.layout.activity_listview, new String[]{TAG_NAME}, new int[]{R.id.name});

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {

                                //ausgewählte Wahl
                                Log.d("array", arrayList.get((int)id).get(TAG_NAME));

                                Intent i = new Intent(MainActivity.this, State.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("wahlSlug",arrayList.get((int)id).get(TAG_SLUG));
                                bundle.putString("electionSlug",arrayList.get((int)id).get(electionSlug));
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
                VolleyLog.d("error", "Error: " + error.getMessage());

            }
        });

        queue.add(req);
    }




    @Override
    public void onErrorResponse(VolleyError error) {
        //mTextView.setText(error.getMessage());
        Log.d("error",error.getMessage());
    }



    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
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

    @Override
    public void onResponse(JSONObject response) {

    }
}