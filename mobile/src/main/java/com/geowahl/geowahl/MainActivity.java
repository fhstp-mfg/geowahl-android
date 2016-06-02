package com.geowahl.geowahl;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient googleClient;
    DataMap dataMap = new DataMap();
    String WEARABLE_DATA_PATH = "/wearable_data";
    String name;

    // URL to get contacts JSON
    //private static String url = "http://geowahl.suits.at/elections";
    private static String url = "http://flock-0867.students.fhstp.ac.at/geowahl/elections.json";

    // JSON Node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_SLUG = "slug";
    private static final String TAG_PARTNAME = "partName";
    private static final String TAG_R = "r";
    private static final String TAG_G = "g";
    private static final String TAG_B = "b";
    private static final String TAG_A = "a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Calling async task to get json
        new GetData().execute();

        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetData extends AsyncTask<Void, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> arrayList;
        ArrayList<HashMap<String, String>> electionList;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            WebRequest webreq = new WebRequest();

            // Making a request to url and getting response
            String jsonStr = webreq.makeWebServiceCall(url, WebRequest.GET);

            Log.d("Response: ", "> " + jsonStr);

            arrayList = ParseJSON(jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

            ListView listView = (ListView)findViewById(R.id.listView);

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, arrayList,
                    R.layout.activity_listview, new String[]{TAG_PARTNAME}, new int[]{R.id.name});

            listView.setAdapter(adapter);

            Log.d("Arraylist", arrayList.toString());

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    /*
                    Intent i = new Intent(MainActivity.this, State.class);



                    i.putExtra("electionId", arrayList.get((int) id).get("id"));
                    i.putExtra("partyName", arrayList.get((int) id).get("partName"));
                    i.putExtra("r", arrayList.get((int)id).get("r"));
                    i.putExtra("g", arrayList.get((int)id).get("g"));
                    i.putExtra("b", arrayList.get((int)id).get("b"));
                    i.putExtra("a", arrayList.get((int)id).get("a"));
                    startActivity(i);
                    overridePendingTransition(R.animator.activity_in, R.animator.activity_out);
*/

                    //send to watch
                    dataMap.putString("r", arrayList.get((int) id).get("r"));
                    dataMap.putString("g", arrayList.get((int)id).get("g"));
                    dataMap.putString("b", arrayList.get((int)id).get("b"));
                    dataMap.putString("partyName", arrayList.get((int)id).get("partName"));
                    new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();

                }

            });
        }

    }

    private ArrayList<HashMap<String, String>> ParseJSON(String json) {
        if (json != null) {
            try {
                // Hashmap for ListView
                ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                JSONObject jsonObj = new JSONObject(json);

                    JSONObject obj = (JSONObject) jsonObj.getJSONArray("elections").get(0);
                    String name =  obj.getString(TAG_NAME);
                    String id =  obj.getString(TAG_ID);
                    String slug = obj.getString(TAG_SLUG);

                JSONArray part = obj.getJSONArray("parties");
                for (int i = 0; i < part.length(); i++) {
                        JSONObject partobj = (JSONObject) obj.getJSONArray("parties").get(i);

                        String partName = partobj.getString("name");
                        //String rgba = part.getString("rgba");

                        // tmp hashmap for single data
                        HashMap<String, String> data = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        data.put(TAG_ID, id);
                        data.put(TAG_SLUG, slug);
                        data.put(TAG_NAME, name);
                        data.put(TAG_PARTNAME, partName);

                        JSONObject rgb = partobj.getJSONObject("rgba");
                        String r = rgb.getString("r");
                        String g = rgb.getString("g");
                        String b = rgb.getString("b");
                        String a = rgb.getString("a");

                        data.put(TAG_R, r);
                        data.put(TAG_G, g);
                        data.put(TAG_B, b);
                        data.put(TAG_A, a);

                        // adding data to dataArray list
                        arrayList.add(data);

                    }

                return arrayList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
            return null;
        }
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