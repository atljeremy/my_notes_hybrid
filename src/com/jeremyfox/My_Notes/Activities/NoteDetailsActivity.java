package com.jeremyfox.My_Notes.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.jeremyfox.My_Notes.Classes.MyNotesAPIResultReceiver;
import com.jeremyfox.My_Notes.Classes.ResponseObject;
import com.jeremyfox.My_Notes.Dialogs.NewNoteDialog;
import com.jeremyfox.My_Notes.Fragments.NoteDetailsFragment;
import com.jeremyfox.My_Notes.Interfaces.Note;
import com.jeremyfox.My_Notes.Managers.AnalyticsManager;
import com.jeremyfox.My_Notes.Managers.NotesManager;
import com.jeremyfox.My_Notes.R;
import com.jeremyfox.My_Notes.Services.MyNotesAPIService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/9/13
 * Time: 5:01 PM
 */
public class NoteDetailsActivity extends Activity implements NoteDetailsFragment.NoteDetailsListener, MyNotesAPIResultReceiver.Receiver {

    private NoteDetailsFragment noteDetailsFragment;
    private Note note;
    private MyNotesAPIResultReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_details_fragment);
        Bundle extras = getIntent().getExtras();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, show the
            // details in-line with the list so we don't need this activity.
            finish();
            return;
        }

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            NoteDetailsFragment details = new NoteDetailsFragment();
            details.setArguments(extras);
            getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);

        this.noteDetailsFragment = (NoteDetailsFragment)getFragmentManager().findFragmentById(R.id.note_details_fragment);
    }

    @Override
    public void editNote(int recordID, final TextView title, final TextView details) {
        final Note note = NotesManager.getInstance().getNote(recordID);
        if (null != note) {
            final EditText titleInput = new EditText(this);
            titleInput.setText(note.getTitle());
            final EditText detailsInput = new EditText(this);
            detailsInput.setText(note.getDetails());

            NewNoteDialog newNoteDialog = new NewNoteDialog(this, getString(R.string.editNote), titleInput, detailsInput, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    boolean titleEmpty = titleInput.getText().toString().length() == 0;
                    boolean detailsEmpty = detailsInput.getText().toString().length() == 0;
                    if (titleEmpty || detailsEmpty) {
                        Toast.makeText(NoteDetailsActivity.this, getString(R.string.allFeildsRequired), Toast.LENGTH_SHORT).show();
                    } else {
                        final String title = titleInput.getText().toString();
                        final String details = detailsInput.getText().toString();
                        updateNoteToAPI(note.getRecordID(), title, details);
                    }
                }
            });
            newNoteDialog.showDialog();
        } else {
            Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void shareNote(Intent shareIntent) {
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public void setNote(Note note) {
        this.note = note;
    }

    @Override
    public void deleteNote(int recordID) {
        AnalyticsManager.getInstance().fireEvent("delete note from note details view", null);
        final Note note = NotesManager.getInstance().getNote(recordID);
        ArrayList<Note> notesArray = new ArrayList<Note>();
        notesArray.add(note);
        deleteNotes(notesArray);
    }

    @Override
    public void dismissNote() {
        finish();
    }

    /**
     * MyNotesAPIService DELETE notes request
     * @param notesArray
     */
    public void deleteNotes(ArrayList<Note> notesArray) {
        this.receiver = new MyNotesAPIResultReceiver(new Handler());
        this.receiver.setReceiver(this);
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, MyNotesAPIService.class);
        intent.putParcelableArrayListExtra("notesArray", notesArray);
        intent.putExtra("receiver", this.receiver);
        intent.putExtra("action", MyNotesAPIService.DELETE_NOTES);
        startService(intent);
    }

    /**
     * MyNotesAPIService PUT edited note request
     */
    public void updateNoteToAPI(int recordID, String title, String details) {
        this.receiver = new MyNotesAPIResultReceiver(new Handler());
        this.receiver.setReceiver(this);
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, MyNotesAPIService.class);
        intent.putExtra("title", title);
        intent.putExtra("details", details);
        intent.putExtra("recordID", recordID);
        intent.putExtra("receiver", this.receiver);
        intent.putExtra("action", MyNotesAPIService.EDIT_NOTES);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
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
                        break;

                    case MyNotesAPIService.SAVE_NOTE:
                        break;

                    case MyNotesAPIService.EDIT_NOTES:
                        NoteDetailsActivity.this.noteDetailsFragment.dismissDialog();
                        if (responseObject.getStatus() == ResponseObject.RequestStatus.STATUS_SUCCESS) {
                            if (responseObject.getObject() instanceof JSONObject) {
                                JSONObject jsonObject = (JSONObject)responseObject.getObject();
                                String title = null;
                                String details = null;
                                try {
                                    title = jsonObject.getString("title");
                                    details = jsonObject.getString("details");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (null != title && null != details) {
                                    NoteDetailsActivity.this.noteDetailsFragment.updateCurrentNote(title, details);
                                    dismissNote();
                                }

                                Toast.makeText(NoteDetailsActivity.this, getString(R.string.notesUpdated), Toast.LENGTH_SHORT).show();
                                AnalyticsManager.getInstance().fireEvent("note updated successfully", null);
                            }
                        } else {
                            NoteDetailsActivity.this.noteDetailsFragment.showLoadingError();
                            AnalyticsManager.getInstance().fireEvent("error updating note to API", null);
                        }
                        break;

                    case MyNotesAPIService.DELETE_NOTES:
                        NoteDetailsActivity.this.noteDetailsFragment.dismissDialog();
                        if (responseObject.getStatus() == ResponseObject.RequestStatus.STATUS_SUCCESS) {
                            dismissNote();
                            AnalyticsManager.getInstance().fireEvent("successfully deleted note from API", null);
                        } else {
                            NoteDetailsActivity.this.noteDetailsFragment.showLoadingError();
                            AnalyticsManager.getInstance().fireEvent("error deleting note from API", null);
                        }
                        break;
                }
                Log.d("MainActivity", "STATUS_FINISHED");
                break;

            case MyNotesAPIService.STATUS_ERROR:
                Log.d("MainActivity", "STATUS_ERROR");
                NoteDetailsActivity.this.noteDetailsFragment.showLoadingError();
                break;

            default:
                Log.d("default", "default");
                break;
        }
    }
}