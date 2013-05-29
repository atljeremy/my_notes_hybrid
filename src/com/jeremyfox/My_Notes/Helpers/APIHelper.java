package com.jeremyfox.My_Notes.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.jeremyfox.My_Notes.Classes.MyNotesAPIResultReceiver;
import com.jeremyfox.My_Notes.Classes.ResponseObject;
import com.jeremyfox.My_Notes.Fragments.NoteDetailsFragment;
import com.jeremyfox.My_Notes.Interfaces.APIResponse;
import com.jeremyfox.My_Notes.Interfaces.Note;
import com.jeremyfox.My_Notes.Services.MyNotesAPIService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 5/28/13
 * Time: 8:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class APIHelper implements MyNotesAPIResultReceiver.Receiver {

    private MyNotesAPIResultReceiver receiver;
    private Activity activity;

    public APIHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * MyNotesAPIService GET notes request
     */
    public void requestNotesFromAPI(APIResponse response) {
        this.receiver = new MyNotesAPIResultReceiver(new Handler());
        this.receiver.setReceiver(this);
        this.receiver.setRequest(response);
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, MyNotesAPIService.class);
        intent.putExtra("receiver", this.receiver);
        intent.putExtra("action", MyNotesAPIService.GET_NOTES);
        this.activity.startService(intent);
    }

    /**
     * MyNotesAPIService POST new note request
     * @param title the title
     * @param details the details
     */
    public void saveNoteToAPI(String title, String details, APIResponse response) {
        this.receiver = new MyNotesAPIResultReceiver(new Handler());
        this.receiver.setReceiver(this);
        this.receiver.setRequest(response);
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, MyNotesAPIService.class);
        intent.putExtra("title", title);
        intent.putExtra("details", details);
        intent.putExtra("receiver", this.receiver);
        intent.putExtra("action", MyNotesAPIService.SAVE_NOTE);
        this.activity.startService(intent);
    }

    /**
     * MyNotesAPIService DELETE notes request
     * @param notesArray
     */
    public void deleteNotes(ArrayList<Note> notesArray, APIResponse response) {
        this.receiver = new MyNotesAPIResultReceiver(new Handler());
        this.receiver.setReceiver(this);
        this.receiver.setRequest(response);
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, MyNotesAPIService.class);
        intent.putParcelableArrayListExtra("notesArray", notesArray);
        intent.putExtra("receiver", this.receiver);
        intent.putExtra("action", MyNotesAPIService.DELETE_NOTES);
        this.activity.startService(intent);
    }

    /**
     * MyNotesAPIService PUT edited note request
     * @param recordID the record iD
     * @param title the title
     * @param details the details
     */
    public void updateNoteToAPI(int recordID, String title, String details, APIResponse response) {
        this.receiver = new MyNotesAPIResultReceiver(new Handler());
        this.receiver.setReceiver(this);
        this.receiver.setRequest(response);
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, MyNotesAPIService.class);
        intent.putExtra("title", title);
        intent.putExtra("details", details);
        intent.putExtra("recordID", recordID);
        intent.putExtra("receiver", this.receiver);
        intent.putExtra("action", MyNotesAPIService.EDIT_NOTES);
        this.activity.startService(intent);
    }

    /**
     * Called from the MyNotesAPIService to report the status of the request (RUNNING, FINISHED, ERROR)
     *
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData, APIResponse response) {
        ResponseObject responseObject;
        int action = resultData.getInt("action");
        switch (resultCode) {
            case MyNotesAPIService.STATUS_RUNNING:
                Log.d("MainActivity", "STATUS_RUNNING");
                break;

            case MyNotesAPIService.STATUS_FINISHED:
                responseObject = (ResponseObject)resultData.getSerializable("result");
                switch (action) {
                    case MyNotesAPIService.GET_NOTES:
                        if (responseObject.getStatus() == ResponseObject.RequestStatus.STATUS_SUCCESS) {
                            if (responseObject.getObject() instanceof JSONArray) {
                                JSONArray array = (JSONArray)responseObject.getObject();
                                response.requestNotesSuccessfulResponse(array);
                            }
                        } else {
                            response.requestNotesFailureResponse();
                        }
                        break;

                    case MyNotesAPIService.SAVE_NOTE:
                        if (responseObject.getStatus() == ResponseObject.RequestStatus.STATUS_SUCCESS) {
                            if (responseObject.getObject() instanceof JSONObject) {
                                response.saveNoteSuccessfulResponse();
                            }
                        } else {
                            response.saveNoteFailureResponse();
                        }
                        break;

                    case MyNotesAPIService.EDIT_NOTES:
                        NoteDetailsFragment.FRAGMENT.dismissDialog();
                        if (responseObject.getStatus() == ResponseObject.RequestStatus.STATUS_SUCCESS) {
                            if (responseObject.getObject() instanceof JSONObject) {
                                JSONObject jsonObject = (JSONObject)responseObject.getObject();
                                response.editNoteSuccessfulResponse(jsonObject);
                            }
                        } else {

                        }
                        break;

                    case MyNotesAPIService.DELETE_NOTES:
                        if (responseObject.getStatus() == ResponseObject.RequestStatus.STATUS_SUCCESS) {
                            response.deleteNotesSuccessfulResponse();
                        } else {
                            response.deleteNotesFailureResponse();
                        }
                        break;
                }
                Log.d("MainActivity", "STATUS_FINISHED");
                break;

            case MyNotesAPIService.STATUS_ERROR:
                Log.d("MainActivity", "STATUS_ERROR");
                response.serviceError();
                break;

            default:
                Log.d("default", "default");
                break;
        }
    }

}
