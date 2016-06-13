package com.geowahl.geowahl;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawActivty extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle b = getIntent().getExtras();
        ArrayList<String> colorList = b.getStringArrayList("colorList");
        ArrayList<String> partyList = b.getStringArrayList("partyList");
        ArrayList<String> nameList = b.getStringArrayList("nameList");
        ArrayList<String> votesList = b.getStringArrayList("votesList");
        ArrayList<String> percentList = b.getStringArrayList("percentList");


        setContentView(new DrawView(this,colorList,partyList,nameList,votesList,percentList));

    }
}
