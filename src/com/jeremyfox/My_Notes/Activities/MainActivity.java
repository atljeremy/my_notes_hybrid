package com.jeremyfox.My_Notes.Activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.jeremyfox.My_Notes.Adapters.NotesAdapter;
import com.jeremyfox.My_Notes.Classes.BasicNote;
import com.jeremyfox.My_Notes.Classes.MyNotesAPIResultReceiver;
import com.jeremyfox.My_Notes.Classes.ResponseObject;
import com.jeremyfox.My_Notes.Dialogs.NewNoteDialog;
import com.jeremyfox.My_Notes.Fragments.NoteDetailsFragment;
import com.jeremyfox.My_Notes.Fragments.NotesListFragment;
import com.jeremyfox.My_Notes.Helpers.APIHelper;
import com.jeremyfox.My_Notes.Helpers.PrefsHelper;
import com.jeremyfox.My_Notes.Interfaces.APIResponse;
import com.jeremyfox.My_Notes.Interfaces.NetworkCallback;
import com.jeremyfox.My_Notes.Interfaces.Note;
import com.jeremyfox.My_Notes.Managers.AnalyticsManager;
import com.jeremyfox.My_Notes.Managers.NetworkManager;
import com.jeremyfox.My_Notes.Managers.NotesManager;
import com.jeremyfox.My_Notes.R;
import com.jeremyfox.My_Notes.Services.MyNotesAPIService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Main activity.
 */
public class MainActivity extends Activity implements NotesListFragment.NotesListListener, NoteDetailsFragment.NoteDetailsListener {

    /**
     * The constant ACTIVITY.
     */
    public static Activity ACTIVITY;
    private GridView gridView;
    public NotesListFragment notesListFragment;
    private APIHelper apiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_list_fragment);

        this.apiHelper = new APIHelper(this);
        this.ACTIVITY = this;
        this.gridView = (GridView)findViewById(R.id.gridview);
        this.notesListFragment = (NotesListFragment)getFragmentManager().findFragmentById(R.id.notes_list_fragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        AnalyticsManager.getInstance().fireEvent("application started", null);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance().fireEvent("application resumed", null);
    }

    @Override
    public void onDestroy() {
        AnalyticsManager.getInstance().fireEvent("application shutdown", null);
        AnalyticsManager.getInstance().flushEvents();
        super.onDestroy();
    }

    @Override
    public void newNoteAction() {
        NotesAdapter notesAdapter = (NotesAdapter)this.gridView.getAdapter();
        if (null != notesAdapter) notesAdapter.setShouldIncrementCounter(true);
        AnalyticsManager.getInstance().fireEvent("selected new note option", null);
        Intent newNoteIntent = new Intent(this, NewNoteActivity.class);
        startActivity(newNoteIntent);
    }

    @Override
    public void registerWithAPI(final NetworkCallback callback) {
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.executePostRequest(MainActivity.this, NetworkManager.API_HOST + "/users.json", null, new NetworkCallback() {
            @Override
            public void onSuccess(Object json) {
                try {
                    String unique_id = ((JSONObject) json).getString(getString(R.string.unique_id));
                    PrefsHelper.setPref(getBaseContext(), getString(R.string.user_id), unique_id);
                    AnalyticsManager.getInstance().registerSuperProperty("user API key", unique_id);
                    callback.onSuccess(null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AnalyticsManager.getInstance().fireEvent("successful API registration", null);
            }

            @Override
            public void onFailure(int statusCode) {
                HashMap map = new HashMap<String, String>();
                map.put("status_code", Integer.toString(statusCode));
                AnalyticsManager.getInstance().fireEvent("failed API registration", map);
                callback.onFailure(statusCode);
            }
        });
    }

    @Override
    public void requestNotesFromAPI() {
        this.apiHelper.requestNotesFromAPI(new APIResponse() {
            @Override
            public void requestNotesSuccessfulResponse(JSONArray array) {
                NotesManager.getInstance().setNotes(MainActivity.this, array);
                MainActivity.this.notesListFragment.createGridView();
                AnalyticsManager.getInstance().fireEvent("successfully retrieved notes from API", null);
            }

            @Override
            public void requestNotesFailureResponse() {
                MainActivity.this.notesListFragment.showLoadingError();
                AnalyticsManager.getInstance().fireEvent("error retrieving notes from API", null);
            }

            @Override
            public void saveNoteSuccessfulResponse() {
            }
            @Override
            public void saveNoteFailureResponse() {
            }
            @Override
            public void editNoteSuccessfulResponse(JSONObject object) {
            }
            @Override
            public void editNoteFailureResponse() {
            }
            @Override
            public void deleteNotesSuccessfulResponse() {
            }
            @Override
            public void deleteNotesFailureResponse() {
            }

            @Override
            public void serviceError() {
                MainActivity.this.notesListFragment.showLoadingError();
            }
        });
    }

    @Override
    public void deleteNotes(ArrayList<Note> notesArray) {
        this.apiHelper.deleteNotes(notesArray, new APIResponse() {
            @Override
            public void requestNotesSuccessfulResponse(JSONArray array) {
            }
            @Override
            public void requestNotesFailureResponse() {
            }
            @Override
            public void saveNoteSuccessfulResponse() {
            }
            @Override
            public void saveNoteFailureResponse() {
            }
            @Override
            public void editNoteSuccessfulResponse(JSONObject object) {
            }
            @Override
            public void editNoteFailureResponse() {
            }

            @Override
            public void deleteNotesSuccessfulResponse() {
                MainActivity.this.notesListFragment.setGridViewItems();
                Toast.makeText(MainActivity.this, getString(R.string.notesDeleted), Toast.LENGTH_SHORT).show();
                AnalyticsManager.getInstance().fireEvent("successfully deleted note from API", null);
            }

            @Override
            public void deleteNotesFailureResponse() {
                MainActivity.this.notesListFragment.showSavingError();
                AnalyticsManager.getInstance().fireEvent("error deleting note from API", null);
            }

            @Override
            public void serviceError() {
                MainActivity.this.notesListFragment.showLoadingError();
            }
        });
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
                        Toast.makeText(MainActivity.this, getString(R.string.allFeildsRequired), Toast.LENGTH_SHORT).show();
                    } else {
                        final String title = titleInput.getText().toString();
                        final String details = detailsInput.getText().toString();
                        MainActivity.this.apiHelper.updateNoteToAPI(note.getRecordID(), title, details, new APIResponse() {
                            @Override
                            public void requestNotesSuccessfulResponse(JSONArray array) {
                            }

                            @Override
                            public void requestNotesFailureResponse() {
                            }

                            @Override
                            public void saveNoteSuccessfulResponse() {
                            }

                            @Override
                            public void saveNoteFailureResponse() {
                            }

                            @Override
                            public void editNoteSuccessfulResponse(JSONObject jsonObject) {
                                String title = null;
                                String details = null;
                                try {
                                    title = jsonObject.getString("title");
                                    details = jsonObject.getString("details");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (null != title && null != details) {
                                    NoteDetailsFragment.FRAGMENT.updateCurrentNote(title, details);
                                }

                                Toast.makeText(MainActivity.this, getString(R.string.notesUpdated), Toast.LENGTH_SHORT).show();
                                AnalyticsManager.getInstance().fireEvent("note updated successfully", null);
                            }

                            @Override
                            public void editNoteFailureResponse() {
                                NoteDetailsFragment.FRAGMENT.showLoadingError();
                                AnalyticsManager.getInstance().fireEvent("error updating note to API", null);
                            }

                            @Override
                            public void deleteNotesSuccessfulResponse() {
                            }

                            @Override
                            public void deleteNotesFailureResponse() {
                            }

                            @Override
                            public void serviceError() {
                                MainActivity.this.notesListFragment.showLoadingError();
                            }

                        });
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
    }

    @Override
    public void showNoteDetails(int index, boolean dualMode) {

        if (dualMode) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            this.gridView.setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            NoteDetailsFragment details = (NoteDetailsFragment)getFragmentManager().findFragmentById(R.id.note_details_fragment);
            if (details == null || details.getShownIndex() != index) {
                // Make new fragment to show this selection.
                details = NoteDetailsFragment.newInstance(index);

                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.note_details_fragment, details);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            JSONArray notes = NotesManager.getInstance().getNotes();
            BasicNote note = null;
            try {
                note = (BasicNote)notes.get(index);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent noteDetailsIntent = new Intent(MainActivity.this, NoteDetailsActivity.class);
            noteDetailsIntent.putExtra("title", note.getTitle());
            noteDetailsIntent.putExtra("details", note.getDetails());
            noteDetailsIntent.putExtra("id", note.getRecordID());
            noteDetailsIntent.putExtra("index", index);
            startActivity(noteDetailsIntent, null);
            AnalyticsManager.getInstance().fireEvent("opened a note", null);
        }
    }
}
