package com.jeremyfox.My_Notes.Classes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 5/27/13
 * Time: 3:26 PM
 */
public class JSInterface {

    public interface Receiver {
        public void onSaveNote(String title, String description, final Boolean shouldFinish);
    }

    private Context context;
    private Receiver receiver;

    protected JSInterface(){}

    public JSInterface(Context context) {
        this.context = context;
    }

    public JSInterface(Context context, Receiver receiver) {
        this.context = context;
        this.receiver = receiver;
    }

    @JavascriptInterface
    public void saveNote(String title, String description){
        Log.d("TITLE: ", title);
        Log.d("TITLE: ", description);

        notifyReceivedOfNewNote(title, description, true);
    }

    @JavascriptInterface
    public void saveAndShareNote(String title, String description) {

        notifyReceivedOfNewNote(title, description, false);

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, description);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(Intent.createChooser(emailIntent, title));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyReceivedOfNewNote(String title, String description, final Boolean shouldFinish) {
        if (null != this.receiver) {
            this.receiver.onSaveNote(title, description, shouldFinish);
        }
    }

}
