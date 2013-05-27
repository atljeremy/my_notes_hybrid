package com.jeremyfox.My_Notes.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.jeremyfox.My_Notes.Activities.MainActivity;
import com.jeremyfox.My_Notes.Adapters.NotesAdapter;
import com.jeremyfox.My_Notes.Classes.BasicNote;
import com.jeremyfox.My_Notes.Helpers.PrefsHelper;
import com.jeremyfox.My_Notes.Interfaces.NetworkCallback;
import com.jeremyfox.My_Notes.Interfaces.Note;
import com.jeremyfox.My_Notes.Managers.AnalyticsManager;
import com.jeremyfox.My_Notes.Managers.NotesManager;
import com.jeremyfox.My_Notes.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/14/13
 * Time: 12:47 PM
 */
public class NotesListFragment extends Fragment {

    /**
     * The interface Notes list listener.
     */
    public interface NotesListListener {
        /**
         * Show note details.
         *
         * @param index the index
         * @param dualMode the dual mode
         */
        public void showNoteDetails(int index, boolean dualMode);

        /**
         * New note action.
         */
        public void newNoteAction();

        /**
         * Register with aPI.
         *
         * @param callback the callback
         */
        public void registerWithAPI(NetworkCallback callback);

        /**
         * Request notes from aPI.
         */
        public void requestNotesFromAPI();

        /**
         * Delete notes.
         *
         * @param notesArray the notes array
         */
        public void deleteNotes(ArrayList<Note> notesArray);

        /**
         * Query content provider.
         *
         * @return the cursor
         */
        public Cursor queryContentProvider();
    }

    private NotesListListener listener;
    private boolean dualMode;
    private int curCheckPosition = 0;
    private static final int DEFAULT_HOME_VIEW = 0;
    private static final int NOTES_VIEW = 1;
    private ViewFlipper viewFlipper;
    private GridView gridView;
    private ProgressDialog dialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.gridView = (GridView)getActivity().findViewById(R.id.gridview);
        this.viewFlipper = (ViewFlipper)getActivity().findViewById(R.id.ViewFlipper);

        View detailsFrame = getActivity().findViewById(R.id.note_details_fragment);
        dualMode = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            curCheckPosition = savedInstanceState.getInt("curNote", 0);
            boolean wasShowingDialog = savedInstanceState.getBoolean("dialogVisibility");
            if (wasShowingDialog) {
                showLoadingDialog();
            }
        }

        if (dualMode) {
            GridView gridView = (GridView)getActivity().findViewById(R.id.gridview);
            gridView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            showNoteDetails(curCheckPosition);
        }

        String user_id = PrefsHelper.getPref(getActivity(), getActivity().getString(R.string.user_id));
        if (null == user_id || user_id.length() == 0) {
            AnalyticsManager.getInstance().fireEvent("new user", null);
            listener.registerWithAPI(new NetworkCallback() {
                @Override
                public void onSuccess(Object json) {
                    requestNotes();
                }

                @Override
                public void onFailure(int statusCode) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Sorry!")
                            .setMessage("We were unable to register you with the API at this time. Please try again later by simply relaunching the application.")
                            .setNegativeButton("Ok", null)
                            .create()
                            .show();
                }
            });
        } else {
            AnalyticsManager.getInstance().fireEvent("returning user", null);
            requestNotes();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (NotesManager.getInstance().getNotes().length() > 0)
            requestNotes();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curNote", curCheckPosition);
        outState.putBoolean("dialogVisibility", dialog.isShowing());
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflator, container, savedInstanceState);

        LinearLayout view = (LinearLayout) inflator.inflate(R.layout.main, container, false);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (NotesListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NotesListListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case R.id.load_from_content_provider:
                Cursor cursor = listener.queryContentProvider();
                if (null != cursor) {
                    JSONArray notesFromProvider = new JSONArray();
                    while (cursor.moveToNext()) {
                        int recordID   = cursor.getInt(1);
                        String title   = cursor.getString(2);
                        String details = cursor.getString(3);

                        notesFromProvider.put(new BasicNote(title, details, recordID));
                    }

                    if (notesFromProvider.length() > 0) {
                        NotesManager.getInstance().setNotesFromProvider(getActivity(), notesFromProvider);
                        setGridViewItems();
                        showLoadedNotesFromProviderDialog();
                    }
                }
                break;

            case R.id.new_note:
                listener.newNoteAction();
                break;

            case R.id.sync_notes:
                AnalyticsManager.getInstance().fireEvent("selected sync notes option", null);
                requestNotes();
                break;
        }
        return true;
    }

    private void showNoteDetails(int index) {
        curCheckPosition = index;
        listener.showNoteDetails(index, dualMode);
    }

    /**
     * Creates the notes grid view, retrieves notes from API, then displays the grid view of notes
     */
    public void createGridView() {
        final GridView grid = NotesListFragment.this.gridView;
        if (dualMode) {
            grid.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        } else {
            grid.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        }

        grid.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                NotesAdapter notesAdapter = (NotesAdapter)grid.getAdapter();
                int count = grid.getCheckedItemCount();
                if (count > 0) {
                    notesAdapter.setShouldIncrementCounter(false);
                } else {
                    notesAdapter.setShouldIncrementCounter(true);
                }
                mode.setTitle(count + " selected");
                BasicNote note = (BasicNote)grid.getItemAtPosition(position);
                note.setSelected(checked);
                grid.invalidateViews();
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.trash:
                        int numNotesSelectedForDelete = deleteSelectedNotes();
                        HashMap deletedmap = new HashMap<String, String>();
                        deletedmap.put("selected for delete", Integer.toString(numNotesSelectedForDelete));
                        AnalyticsManager.getInstance().fireEvent("deleted notes", deletedmap);
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        setGridViewItems();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showNoteDetails(position);
            }
        });

        dialog.dismiss();
    }

    /**
     * Request notes from the API.
     */
    public void requestNotes() {
        showLoadingDialog();
        listener.requestNotesFromAPI();
    }

    /**
     * Updates the gird view
     */
    public void setGridViewItems() {
        dialog.dismiss();
        JSONArray jsonArray = NotesManager.getInstance().getNotes();
        if (jsonArray.length() > 0) {
            ArrayList<BasicNote> notes = new ArrayList<BasicNote>(jsonArray.length());
            for (int i=0; i<jsonArray.length(); i++) {
                try {
                    BasicNote note = (BasicNote)jsonArray.get(i);
                    notes.add(note);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (null == notes) notes = new ArrayList<BasicNote>();
            NotesAdapter notesAdapter = new NotesAdapter(MainActivity.ACTIVITY, R.id.title, notes);
            this.gridView.setAdapter(notesAdapter);
            this.viewFlipper.setDisplayedChild(NOTES_VIEW);
            AnalyticsManager.getInstance().fireEvent("showed notes view", null);
        } else {
            NotesAdapter notesAdapter = new NotesAdapter(getActivity(), R.id.title, new ArrayList<BasicNote>());
            this.gridView.setAdapter(notesAdapter);
            this.viewFlipper.setDisplayedChild(DEFAULT_HOME_VIEW);
            AnalyticsManager.getInstance().fireEvent("showed default home view", null);
        }

        this.gridView.invalidateViews();
    }

    /**
     * Deletes the selected notes from the API
     * @return int the total number of notes that were deleted
     */
    private int deleteSelectedNotes() {
        showDeletingNotesDialog();
        ArrayList<Note> notesArray = new ArrayList<Note>();
        NotesAdapter notesAdapter = (NotesAdapter)this.gridView.getAdapter();
        notesAdapter.setShouldIncrementCounter(true);
        SparseBooleanArray checked = this.gridView.getCheckedItemPositions();
        for (int i = 0; i < this.gridView.getCount(); i++) {
            if (checked.get(i)) {
                final BasicNote note = (BasicNote)this.gridView.getItemAtPosition(i);
                notesArray.add(note);
            }
        }

        if (notesArray.size() > 0) {
            listener.deleteNotes(notesArray);
        }

        return notesArray.size();
    }

    /**
     * Shows the loading spinner dialog
     * @return ProgressDialog the progress dialog that will be displayed while loading notes from the API
     */
    private void showLoadingDialog() {
        if (null == this.dialog) this.dialog = new ProgressDialog(getActivity());
        this.dialog.setMessage("Loading Notes...");
        this.dialog.setCancelable(false);
        this.dialog.show();
        AnalyticsManager.getInstance().fireEvent("showed loading dialog", null);
    }

    /**
     * Show loading error.
     */
    public void showLoadingError() {
        this.dialog.dismiss();

        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage("Couldn't load your notes. Please check your network connection and try again.")
                .setNegativeButton("Ok", null)
                .create()
                .show();
    }

    /**
     * Show loading error.
     */
    public void showSavingError() {
        this.dialog.dismiss();

        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage("Couldn't save your note. Please check your network connection and try again.")
                .setNegativeButton("Ok", null)
                .create()
                .show();
    }

    /**
     * Show Deleting Notes dialog
     */
    private void showDeletingNotesDialog() {
        if (null == this.dialog) this.dialog = new ProgressDialog(getActivity());
        this.dialog.setMessage(getString(R.string.deleting_note));
        this.dialog.setCancelable(false);
        this.dialog.show();
        AnalyticsManager.getInstance().fireEvent("showed deleting note dialog", null);
    }

    /**
     * Show loading error.
     */
    public void showLoadedNotesFromProviderDialog() {
        this.dialog.dismiss();

        new AlertDialog.Builder(getActivity())
                .setTitle("Process Complete!")
                .setMessage("The current notes in the grid view have been loaded via the My Notes content provider." +
                        "\n See the NotesListFragment.java from line 190 to 207 for the code that is mkaing this happen.")
                .setNegativeButton("Ok", null)
                .create()
                .show();
    }

}
