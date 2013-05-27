package com.jeremyfox.My_Notes.Managers;

import android.content.Context;
import com.jeremyfox.My_Notes.Classes.BasicNote;
import com.jeremyfox.My_Notes.Helpers.FileStorageHelper;
import com.jeremyfox.My_Notes.Interfaces.Note;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/10/13
 * Time: 8:38 PM
 */
public class NotesManager {

    private static NotesManager instance = null;
    private JSONArray notes = new JSONArray();

    /**
     * Instantiates a new Notes manager.
     *
     * @throws JSONException the jSON exception
     */
    protected NotesManager() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static NotesManager getInstance() {
        if(instance == null) {
            instance = new NotesManager();
        }
        return instance;
    }

    /**
     * Gets notes object.
     *
     * @return the notes object
     */
    public JSONArray getNotes() {
        return this.notes;
    }

    /**
     * Sets notes.
     *
     * @param notes the notes
     */
    public void setNotes(Context context, JSONArray notes) {
        try {
            this.notes = null;
            this.notes = new JSONArray();
            for (int i=0; i<notes.length(); i++) {
                JSONObject currentNote = notes.getJSONObject(i);
                String title = currentNote.getString("title");
                String details = currentNote.getString("details");
                int recordId = currentNote.getInt("id");
                BasicNote basicNote = new BasicNote(title, details, recordId);
                NotesManager.this.notes.put(basicNote);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FileStorageHelper.writeStringToFile(context, notes.toString(), FileStorageHelper.NOTES_FILE_PATH);
    }

    /**
     * Sets notes.
     *
     * @param notes the notes
     */
    public void setNotesFromProvider(Context context, JSONArray notes) {
        if (this.notes != notes) {
            this.notes = null;
            this.notes = notes;
        }
    }

    public Note getNote(int recordID) {
        for (int i=0; i<getNotes().length(); i++) {
            Note currentNote = null;
            try {
                currentNote = (Note)getNotes().get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (currentNote.getRecordID() == recordID) {
                return currentNote;
            }
        }
        return null;
    }

    public Note getFirstNote() {
        Note note = null;
        try {
            note = (Note)getNotes().get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return note;
    }

    /**
     * Removes the supplied note from the "notes" array.
     *
     * @param note the note to remove
     * @return the boolean
     */
    public boolean removeNote(Note note) {
        boolean removed = false;
        if (null != note) {
            JSONArray newArray = new JSONArray();
            for (int i=0; i<this.notes.length(); i++) {
                try {
                    BasicNote currentNote = ((BasicNote)this.notes.get(i));
                    if (note.getRecordID() != currentNote.getRecordID()) {
                        newArray.put(currentNote);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (newArray.length() == (this.notes.length() - 1)) {
                removed = true;
                this.notes = newArray;
            }
        }
        return removed;
    }
}
