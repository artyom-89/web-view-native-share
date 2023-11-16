package com.example.webviewnativeshare.webview.share;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathFromBase64 implements Callable<ArrayList<String>> {
    Context context;
    String[] shareFiles;


    public FilePathFromBase64(Context context,
                              String[] shareFiles) {
        this.context = context;
        this.shareFiles = shareFiles;
    }

    @Nullable
    public String createFile(Context context, String base64String, String fileName) {
        // data:image/png;base64,
        String regexp = "^data:(.*);base64,(.*)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(base64String);

        if (matcher.matches()) {
            String mimeType = matcher.group(1);
            String base64 = matcher.group(2);

            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

            byte[] fileAsBytes = Base64.decode(base64, 0);

            File folder = new File(context.getCacheDir(), "share_files");
            try {
                folder.mkdirs();
                File file = new File(folder + "/" + fileName + "." + ext);
                FileOutputStream os = null;

                os = new FileOutputStream(file, false);
                os.write(fileAsBytes);
                os.flush();
                os.close();

                return file.getAbsolutePath();

            } catch (IOException e) {
                Log.d("webViewTag", e.toString());
//                throw new RuntimeException(e);
                return null;
            }
        } else {
//            throw new RuntimeException();
            Log.d("webViewTag", "Incorrect base64String");
            return null;
        }
    }


    @Override
    public ArrayList<String> call() throws Exception {
        ArrayList<String> response = new ArrayList<>();
        int i = 0;
        for (String shareFile : shareFiles) {
            String uri = createFile(context, shareFile, Integer.toString(i));
            if (uri != null) {
                response.add(uri);
            }
            ++i;
        }
        return response;
    }
}
