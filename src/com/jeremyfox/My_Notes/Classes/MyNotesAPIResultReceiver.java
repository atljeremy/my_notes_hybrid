package com.jeremyfox.My_Notes.Classes;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.jeremyfox.My_Notes.Interfaces.APIResponse;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/21/13
 * Time: 9:52 PM
 */
public class MyNotesAPIResultReceiver extends ResultReceiver {
    private Receiver receiver;
    private APIResponse request;

    public MyNotesAPIResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public void setRequest(APIResponse request) {
        this.request = request;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData, APIResponse request);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData, request);
        }
    }
}