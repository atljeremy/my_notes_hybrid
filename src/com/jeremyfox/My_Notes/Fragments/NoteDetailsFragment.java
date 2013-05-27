package com.jeremyfox.My_Notes.Fragments;

import android.app.*;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.jeremyfox.My_Notes.Interfaces.Note;
import com.jeremyfox.My_Notes.Managers.AnalyticsManager;
import com.jeremyfox.My_Notes.Managers.NotesManager;
import com.jeremyfox.My_Notes.R;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/14/13
 * Time: 12:55 PM
 */
public class NoteDetailsFragment extends Fragment {

    /**
     * The interface Note details listener.
     */
    public interface NoteDetailsListener {
        /**
         * Dismiss note.
         */
        public void dismissNote();

        /**
         * Delete note.
         *
         * @param recordID the record iD
         */
        public void deleteNote(int recordID);

        /**
         * Edit note.
         *
         * @param recordID the record iD
         * @param title the title
         * @param details the details
         */
        public void editNote(int recordID, TextView title, TextView details);

        /**
         * Share note.
         *
         * @param shareIntent the share intent
         */
        public void shareNote(Intent shareIntent);

        /**
         * Sets note.
         *
         * @param note the note
         */
        public void setNote(Note note);
    }

    private NoteDetailsListener listener;
    private TextView noteTitle;
    private TextView noteDetails;
    private String title;
    private String details;
    private int recordID;
    private ProgressDialog dialog;
    private Note note;
    public static NoteDetailsFragment FRAGMENT;

    /**
     * New instance.
     *
     * @param index the index
     * @return the note details fragment
     */
    public static NoteDetailsFragment newInstance(int index) {
        NoteDetailsFragment f = new NoteDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        setNoteDetails(args);
        FRAGMENT = this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean dualMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (dualMode) {
            getActivity().findViewById(R.id.dismiss_note_button).setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflator, container, savedInstanceState);

        ScrollView scrollView = (ScrollView) inflator.inflate(R.layout.note_details, container, false);

        this.noteTitle = (TextView)scrollView.findViewById(R.id.note_title);
        this.noteDetails = (TextView)scrollView.findViewById(R.id.note_details);
        Button dismissNoteButton = (Button) scrollView.findViewById(R.id.dismiss_note_button);
        Button editNoteButton = (Button) scrollView.findViewById(R.id.edit_note_button);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Dakota-Regular.ttf");
        this.noteTitle.setTypeface(typeface);
        this.noteDetails.setTypeface(typeface);
        dismissNoteButton.setTypeface(typeface);
        editNoteButton.setTypeface(typeface);

        this.noteTitle.setText(this.title);
        this.noteDetails.setText(this.details);

        dismissNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnalyticsManager.getInstance().fireEvent("dismiss note", null);
                listener.dismissNote();
            }
        });
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnalyticsManager.getInstance().fireEvent("edit note from note details view", null);
                listener.editNote(NoteDetailsFragment.this.recordID, NoteDetailsFragment.this.noteTitle, NoteDetailsFragment.this.noteDetails);
            }
        });

        return scrollView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (NoteDetailsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoteDetailsListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case R.id.share_note:
                AnalyticsManager.getInstance().fireEvent("share note from note details view", null);
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getNote().getTitle());
                shareIntent.putExtra(Intent.EXTRA_TITLE, getNote().getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, getNote().getDetails());
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;

            case R.id.note_details_trash:
                AnalyticsManager.getInstance().fireEvent("delete note from note details view", null);
                showDeletingNoteDialog();
                listener.deleteNote(getNote().getRecordID());
                break;

            case android.R.id.home:
                listener.dismissNote();
                return true;
        }
        return true;
    }

    /**
     * Gets shown index.
     *
     * @return the shown index
     */
    public int getShownIndex() {
        int shownIndex;
        Bundle args = getArguments();
        if (null != args) {
            shownIndex = args.getInt("index", 0);
        } else {
            shownIndex = 0;
        }
        return shownIndex;
    }

    /**
     * Sets note details.
     *
     * @param args the args
     */
    public void setNoteDetails(Bundle args) {
        Note note = NotesManager.getInstance().getFirstNote();
        if (null != args) {
            int index = args.getInt("index", 0);
            try {
                JSONArray notes = NotesManager.getInstance().getNotes();
                if (null != notes && notes.length() > 0 && notes.length() > index) {
                    note = (Note) notes.get(index);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (null != note) {
            this.title = note.getTitle();
            this.details = note.getDetails();
            this.recordID = note.getRecordID();
        }

        listener.setNote(note);
        setNote(note);
    }

    /**
     * Gets note.
     *
     * @return the note
     */
    public Note getNote() {
        return note;
    }

    /**
     * Sets note.
     *
     * @param note the note
     */
    public void setNote(Note note) {
        this.note = note;
    }

    public void updateCurrentNote(String title, String details) {
        this.title = title;
        this.details = details;
        this.noteTitle.setText(title);
        this.noteDetails.setText(details);
    }

    /**
     * Show editing note dialog.
     */
    public void showEditingNoteDialog() {
        if (null == this.dialog) this.dialog = new ProgressDialog(getActivity());
        this.dialog.setMessage(getString(R.string.savingNote));
        this.dialog.setCancelable(false);
        this.dialog.show();
        AnalyticsManager.getInstance().fireEvent("showed editing note dialog", null);
    }

    /**
     * Show deleting note dialog.
     */
    public void showDeletingNoteDialog() {
        if (null == this.dialog) this.dialog = new ProgressDialog(getActivity());
        this.dialog.setMessage(getString(R.string.deleting_note));
        this.dialog.setCancelable(false);
        this.dialog.show();
        AnalyticsManager.getInstance().fireEvent("showed deleting note dialog", null);
    }

    /**
     * Dismiss dialog.
     */
    public void dismissDialog() {
        if (null != this.dialog) {
            this.dialog.dismiss();
        }
    }

    /**
     * Show loading error.
     */
    public void showLoadingError() {
        this.dialog.dismiss();

        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage("Please check your network connection and try again.")
                .setNegativeButton("Ok", null)
                .create()
                .show();
    }

}
