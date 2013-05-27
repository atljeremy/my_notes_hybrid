package com.jeremyfox.My_Notes.Helpers;

import android.content.Context;
import android.util.Log;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/23/13
 * Time: 10:35 PM
 */
public class FileStorageHelper {

    public static final String NOTES_FILE_PATH = "notes_file";

    public static boolean writeStringToFile(Context context, String string, String file) {
        boolean written = false;
        try {
            DataOutputStream out = new DataOutputStream(context.openFileOutput(file, Context.MODE_PRIVATE));
            out.write(string.getBytes());
            out.close();
            written = true;
        } catch (IOException e) {
            Log.i("FileStorageHelper", "Output Error");
        }
        return written;
    }

    public static String readStringFromFile(Context context, String file) {
        String results = "";
        try {
            FileInputStream fis = context.openFileInput(file);
            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];

            while (fis.read(buffer) != -1) {
                fileContent.append(new String(buffer));
            }

            results = fileContent.toString();
        } catch (FileNotFoundException e) {
            Log.i("FileStorageHelper", "File Not Found");
        } catch (IOException e) {
            Log.i("FileStorageHelper", "Input Error");
        }
        return results;
    }

}
