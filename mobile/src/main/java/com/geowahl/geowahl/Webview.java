package com.geowahl.geowahl;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Webview extends AppCompatActivity {

    String url;
    private static String url_part1 = "http://geowahl.suits.at/";
    private String donutChart = "/donut-chart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Bundle b = getIntent().getExtras();
        String jsonUrl = b.getString("url");

        url = jsonUrl+donutChart;
        Log.d("url",jsonUrl);
        WebView webView = (WebView) findViewById(R.id.webview);
        loadWebViewLoad(webView);

        /*
        if(b.getString("donuturl") != "") {
            url = b.getString("donuturl");
        }
        else if(b.getString("stateSlug") != "" && b.getString("electionSlug") != "" && b.getString("districtId") != ""){
            url = url_part1+ b.getString("electionSlug")+"/"+ b.getString("stateSlug")+"/"+b.getString("districtId");
            /*bundle.putString("stateSlug",stateslug);
            bundle.putString("electionSlug",electionslug);
            bundle.putString("districtId", districtId);*/
        /*

        }else{
            Log.d("test","zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");

        }
        */
    }

    private void loadWebViewLoad(WebView webview) {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadUrl(url);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                //this.finish();
               // NavUtils.navigateUpFromSameTask(this);
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, intent);
                overridePendingTransition(R.animator.activity_back_in, R.animator.activity_back_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}