package com.geowahl.geowahl;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ListActivity{

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listview = (ListView) findViewById(R.id.listView);
        String[] values = new String[] { "Gemeinderatswahlen", "Bundespräsidentenwahlen" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, values);

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
    }*/

    // URL to get contacts JSON
    //private static String url = "http://geowahl.suits.at/elections";
    //private static String url = "http://192.168.8.105:8888/geowahl-web/elections";
    private static String url = "http://flock-0867.students.fhstp.ac.at/geowahl/elections.json";

    // JSON Node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Calling async task to get json
        new GetData().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetData extends AsyncTask<Void, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> arrayList;
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

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, arrayList,
                    R.layout.activity_listview, new String[]{TAG_NAME}, new int[]{R.id.name});

            setListAdapter(adapter);

            /*String[] name = new String[]{TAG_NAME};*/
            //Toast.makeText(getApplicationContext(), arrayList.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private ArrayList<HashMap<String, String>> ParseJSON(String json) {
        if (json != null) {
            try {
                // Hashmap for ListView
                ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();

                JSONObject jsonObj = new JSONObject(json);

                // looping through All data
                for (int i = 0; i < jsonObj.length(); i++) {
                    JSONObject obj = (JSONObject) jsonObj.getJSONArray("elections").get(i);
                    String name =  obj.getString(TAG_NAME);
                    String id =  obj.getString(TAG_ID);
                    //Log.v("test",name);

                    // tmp hashmap for single data
                    HashMap<String, String> data = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    data.put(TAG_ID, id);
                    data.put(TAG_NAME, name);

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
}