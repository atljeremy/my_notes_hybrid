package com.jeremyfox.My_Notes.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jeremyfox.My_Notes.Classes.BasicNote;
import com.jeremyfox.My_Notes.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/20/13
 * Time: 10:27 PM
 */
public class NotesAdapter extends ArrayAdapter<BasicNote> {

    private ArrayList<BasicNote> notes;
    private boolean shouldIncrementCounter;
    private int counter;

    public NotesAdapter(Context context, int textViewResourceId, ArrayList<BasicNote> notes) {
        super(context, textViewResourceId, notes);
        this.notes = notes;
        setShouldIncrementCounter(true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.my_notes_list_item, null);
        } else {
            view.findViewById(R.id.main_item_container).setBackground(getContext().getResources().getDrawable(R.drawable.grid_item_1_background));
        }

        if (counter >= 3) {
            view.findViewById(R.id.main_item_container).setBackground(getContext().getResources().getDrawable(R.drawable.grid_item_2_background));
            counter = 0;
        } else if (counter < 3) {
            if (shouldIncrementCounter()) {
                counter++;
            }
        }

        BasicNote note = notes.get(position);
        if (note != null) {
            ImageView checkmark = (ImageView)view.findViewById(R.id.checkmark);
            if (note.isSelected()) {
                checkmark.setVisibility(View.VISIBLE);
            } else {
                checkmark.setVisibility(View.GONE);
            }
            TextView title = (TextView) view.findViewById(R.id.title);
            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Dakota-Regular.ttf");
            title.setTypeface(typeface);
            title.setText(note.getTitle());
        }
        return view;
    }


    public boolean shouldIncrementCounter() {
        return this.shouldIncrementCounter;
    }

    public void setShouldIncrementCounter(boolean shouldIncrementCounter) {
        this.shouldIncrementCounter = shouldIncrementCounter;
    }
}