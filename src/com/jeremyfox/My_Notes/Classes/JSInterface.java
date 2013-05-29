package com.jeremyfox.My_Notes.Classes;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 5/27/13
 * Time: 3:26 PM
 */
public class JSInterface {

    public interface Receiver {
        public void onSaveNote(String title, String description);
    }

    private Context context;
    private Receiver receiver;

    protected JSInterface(){}

    public JSInterface(Context context) {
        this.context = context;
    }

    public JSInterface(Receiver receiver) {
        this.receiver = receiver;
    }

    @JavascriptInterface
    public void savenote(String title, String description){
        Log.d("TITLE: ", title);
        Log.d("TITLE: ", description);

        if (null != this.receiver) {
            this.receiver.onSaveNote(title, description);
        }

    }

}
