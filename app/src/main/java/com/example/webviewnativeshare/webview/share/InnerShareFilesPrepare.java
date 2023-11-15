package com.example.webviewnativeshare.webview.share;

import android.content.Context;
import java.util.ArrayList;

public class InnerShareFilesPrepare {
    public void prepareFiles(
            Context context,
            final ShareFilesPrepareCallback callback,
            String[] files
    ) {
        new TaskRunner().executeAsync(new FilePathFromBase64(context, files),
                new TaskRunner.Callback<ArrayList<String>>() {
                    @Override
                    public void onComplete(ArrayList<String> result) {
                        callback.onPrepared(result);
                    }
                });
    }
}
