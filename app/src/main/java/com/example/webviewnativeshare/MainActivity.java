package com.example.webviewnativeshare;

import android.os.Bundle;

import com.example.webviewnativeshare.webview.SampleWebViewManager;

import androidx.appcompat.app.AppCompatActivity;



import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;


public class MainActivity extends AppCompatActivity {

    SampleWebViewManager mWebViewManager;
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebViewManager = new SampleWebViewManager();
        mWebView = mWebViewManager.createViewInstance(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(mWebView, lp);

        mWebViewManager.setSource(mWebView, "file:///android_asset/index.html");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebViewManager.onDropViewInstance(mWebView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

