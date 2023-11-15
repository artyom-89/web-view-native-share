package com.example.webviewnativeshare.webview.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.webviewnativeshare.webview.SampleWebViewManager;

public class ShareBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Integer shareId = null;

        SampleWebViewManager webViewManager = SampleWebViewManager.getInstance();

        if (webViewManager != null) {
            shareId = webViewManager.getCurrentShareId();

            if (shareId != null) {
                webViewManager.shareComplete(shareId);
            }
        }
    }
}
