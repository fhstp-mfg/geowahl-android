package com.geowahl.geowahl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.Gesture;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private TextView districtTV, stateTV, partyTV;
    private WatchViewStub stub;

    String district,state,party,color,percent = null;
    ArrayList<String> colorList,partyList,nameList,percentList = new ArrayList<>();
    ArrayList<Integer> votesList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                districtTV = (TextView) stub.findViewById(R.id.district);
                stateTV = (TextView) stub.findViewById(R.id.state);
                partyTV = (TextView) stub.findViewById(R.id.partei);


            }
        });

        stub.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:

                        Intent i = new Intent(MainActivity.this, DrawActivty.class);


                        final Bundle bundle = new Bundle();
                        bundle.putStringArrayList("colorList",colorList);
                        bundle.putStringArrayList("partyList",partyList);
                        bundle.putStringArrayList("nameList",nameList);
                        bundle.putIntegerArrayList("votesList",votesList);
                        bundle.putStringArrayList("percentList",percentList);

                        i.putExtras(bundle);
                        startActivity(i);
                        break;
                }
                return true;
            }
        });


        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getBundleExtra("datamap");


            district = data.getString("district"); //Name vom Bezirk
            state = data.getString("statename"); //Name vom Bundesland
            party = data.getString("maxParty"); //GewinnerPartei Name
            color = data.getString("maxColor"); //GewinnerPartei Farbe
            percent = data.getString("maxPercent"); //gewinnerPartei %

            stub.setBackgroundColor(Color.parseColor(color));
            //Für die richtigen Farben & Parteien (pro Wahl sind die ja verschieden) hab ich einen extra Call gemacht
            //die Farben und Parteien die es in der akteuell ausgewählten Wahl gibt sind in colorList und partyList drin
            //beim durchiterieren passt also die Farbe an erster Stelle zur Partei an erster Stelle usw

            //nameList, votesList und percentList sind die Ergebnisse für den jeweiligen District (egal ob über Standort oder Liste ausgewählt)
            //weil ich nicht gewusst habe ob die vl in einer anderen Reihenfolge kommen, gibt es also 2 verschiedene Listen mit den Parteinamen


            //also müsste das an abgeglichen werden
            //die Partei an Stelle 0 aus nameList hat also die Farbe von der Stelle an der der Name von nameList und partylList übereinstimmen
            //also für alle in einer Schleife:
            /*
            for (i = 0;jede Partei aus nameList){
                for(j = 0;jede Partei aus partyList){
                    if(partyListe(j) == nameList(i)){
                        //ZEICHNE Donutteil mit der Farbe colorList(j) -->weil für colorList und partyList ja die Indizes zusammenpassen
                        //Farben sind übrigens HEX Werte
                    }
                }
            }
            */

            colorList = data.getStringArrayList("colorList"); //Liste aller Farben
            partyList = data.getStringArrayList("partyList"); //Liste aller Parteien
            nameList = data.getStringArrayList("nameList"); //Liste aller Parteien aus dem JSON
            votesList = data.getIntegerArrayList("votesList"); //Liste aller Stimmen pro Partei aus dem JSON
            percentList = data.getStringArrayList("percentList"); //LIste aller % pro Parteio aus dem JSON

            districtTV.setText(district);
            stateTV.setText(state);
            partyTV.setText(party);

        }

    }
}
