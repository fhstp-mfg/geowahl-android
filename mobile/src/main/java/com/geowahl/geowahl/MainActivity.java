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
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    // URL to get contacts JSON
    private static String url = "http://geowahl.suits.at/elections";

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);

        Log.d("ElectionsUrl",url);
        getElections(url);
    }

    public void getElections(String url_to_api) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url_to_api, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                    JSONArray array = response.getJSONArray("elections");
                    Log.d("elections",array.toString());

                    for(int i = 0;i< array.length();i++){
                        JSONObject object = array.getJSONObject(i);
                        //Log.d("election",object.getString("name"));

                        String electionName = object.getString(Config.TAG_NAME);
                        String electionSlug = object.getString(Config.TAG_SLUG);

                        JSONArray partyArray = object.getJSONArray("parties");
                        final ArrayList<String> colorList = new ArrayList<>();

                        for(int x=0; x<partyArray.length(); x++){
                            JSONObject hexObj = partyArray.getJSONObject(x);
                            String hex = hexObj.getString("hex");
                            Log.d("hexcode", hex);
                            colorList.add(hex);
                        }

                        final HashMap<String, String> d = new HashMap<>();
                        d.put("name", electionName);
                        d.put("slug", electionSlug);

                        arrayList.add(d);

                        ListAdapter adapter = new SimpleAdapter(
                                MainActivity.this, arrayList,
                                R.layout.activity_listview, new String[]{Config.TAG_NAME}, new int[]{R.id.name});

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {

                                //ausgewählte Wahl
                                Log.d("array", arrayList.get((int)id).get(Config.TAG_NAME));

                                Intent i = new Intent(MainActivity.this, State.class);

                                final Bundle bundle = new Bundle();
                                bundle.putStringArrayList("colorList", colorList);
                                bundle.putString("electionSlug",arrayList.get((int)id).get(Config.TAG_SLUG));
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
                Log.d("test",error.getMessage());
                VolleyLog.d("error", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(req);
    }

}