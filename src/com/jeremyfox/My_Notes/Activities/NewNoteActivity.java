package com.jeremyfox.My_Notes.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.jeremyfox.My_Notes.Managers.AnalyticsManager;
import com.jeremyfox.My_Notes.R;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/11/13
 * Time: 7:29 PM
 */
public class NewNoteActivity extends Activity {

    EditText title;
    EditText details;
    Button saveButton;
    Button cancelButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Dakota-Regular.ttf");
        this.title = (EditText)findViewById(R.id.new_note_title);
        this.title.setTypeface(typeface);
        this.details = (EditText)findViewById(R.id.new_note_details);
        this.details.setTypeface(typeface);
        this.saveButton = (Button)findViewById(R.id.save_note_button);
        this.cancelButton = (Button)findViewById(R.id.cancel_new_note_button);

        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean titleEmpty = NewNoteActivity.this.title.getText().toString().length() == 0;
                boolean detailsEmpty = NewNoteActivity.this.details.getText().toString().length() == 0;
                if (titleEmpty || detailsEmpty) {
                    Toast.makeText(NewNoteActivity.this, getString(R.string.allFeildsRequired), Toast.LENGTH_SHORT).show();
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(getString(R.string.titleKey), NewNoteActivity.this.title.getText().toString());
                    returnIntent.putExtra(getString(R.string.detailsKey), NewNoteActivity.this.details.getText().toString());
                    setResult(RESULT_OK,returnIntent);
                    finish();
                }
            }
        });

        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelNewNote();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                cancelNewNote();
                return true;
        }
        return true;
    }

    private void cancelNewNote() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}