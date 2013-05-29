package com.jeremyfox.My_Notes.Interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 5/28/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface APIResponse {

    /**
     * Request Notes
     */
    public void requestNotesSuccessfulResponse(JSONArray array);
    public void requestNotesFailureResponse();

    /**
     * Save a Note
     */
    public void saveNoteSuccessfulResponse();
    public void saveNoteFailureResponse();

    /**
     * Edit a Note
     */
    public void editNoteSuccessfulResponse(JSONObject object);
    public void editNoteFailureResponse();

    /**
     * Delete Notes
     */
    public void deleteNotesSuccessfulResponse();
    public void deleteNotesFailureResponse();

    /**
     * Service Error
     */
    public void serviceError();

}
