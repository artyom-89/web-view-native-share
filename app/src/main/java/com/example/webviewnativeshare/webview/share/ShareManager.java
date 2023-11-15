package com.example.webviewnativeshare.webview.share;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareManager {

    public static final int SHARE_EVENT = 909;

    private <T extends BroadcastReceiver> void sendIntent(
            Context context,
            Intent sendIntent,
            Class<T> receiver
    ) {
        int shareFlag = FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            shareFlag |= FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getBroadcast(context, SHARE_EVENT,
                new Intent(context, receiver),
                shareFlag);
        Intent finalIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            finalIntent = Intent.createChooser(sendIntent, null, pi.getIntentSender());
        } else {
            finalIntent = Intent.createChooser(sendIntent, null);
        }
        finalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(finalIntent);
    }

    public List<Uri> getUrisFromShareData(Context context, List<String> filePaths) {
        ArrayList<Uri> files = new ArrayList<>();
        for (String path : filePaths) {
            files.add(
                    FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".com.example.fileprovider",
                            new File(path)
                    )
            );
        }
        return files;
    }

    private Intent prepareIntent(Context context, String url, String text, String title, List<String> filePaths) {
        final Intent sendingIntent = new Intent();
        sendingIntent.setAction(Intent.ACTION_SEND);

        // todo text, title

        if (url != null) {
            sendingIntent.putExtra(Intent.EXTRA_TEXT, url);
        }
        List<Uri> files = getUrisFromShareData(context, filePaths);
        if (files.isEmpty()) {
            sendingIntent.setType("text/plain");
        } else {
            sendingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendingIntent.setType("image/*");
            if (files.size() > 1) {
                sendingIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                sendingIntent.putParcelableArrayListExtra(
                        Intent.EXTRA_STREAM,
                        new ArrayList<>(files)
                );
            } else {
                sendingIntent.putExtra(Intent.EXTRA_STREAM, files.get(0));
            }
        }
        return sendingIntent;
    }


    public <T extends BroadcastReceiver> void shareDefault(
            Class<T> receiver,
            Context context,
            String url, String text, String title, List<String> filePaths) {
        Intent sendingIntent = prepareIntent(context, url, text, title, filePaths);
        sendIntent(context, sendingIntent, receiver);
    }


}
