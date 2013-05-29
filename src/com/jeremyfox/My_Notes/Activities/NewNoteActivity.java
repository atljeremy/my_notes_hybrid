package com.jeremyfox.My_Notes.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;
import com.jeremyfox.My_Notes.Classes.JSInterface;
import com.jeremyfox.My_Notes.Helpers.APIHelper;
import com.jeremyfox.My_Notes.Interfaces.APIResponse;
import com.jeremyfox.My_Notes.Managers.AnalyticsManager;
import com.jeremyfox.My_Notes.R;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/11/13
 * Time: 7:29 PM
 */
public class NewNoteActivity extends Activity implements JSInterface.Receiver {

    private WebView webView;
    private APIHelper apiHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        this.webView = (WebView)findViewById(R.id.newNoteWebView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new JSInterface((JSInterface.Receiver)this), "MyNotes");
        this.webView.loadUrl("http://www.google.com");
        this.apiHelper = new APIHelper(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    @Override
    public void onSaveNote(String title, String description) {
        this.apiHelper.saveNoteToAPI(title, description, new APIResponse() {
            @Override
            public void requestNotesSuccessfulResponse(JSONArray array) {
            }
            @Override
            public void requestNotesFailureResponse() {
            }

            @Override
            public void saveNoteSuccessfulResponse() {
                AnalyticsManager.getInstance().fireEvent("new note created successfully", null);
                Toast.makeText(NewNoteActivity.this, getString(R.string.noteSaved), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void saveNoteFailureResponse() {
                showSavingError();
                AnalyticsManager.getInstance().fireEvent("error saving new note to API", null);
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
                showSavingError();
                AnalyticsManager.getInstance().fireEvent("error saving new note to API - servie error", null);
            }
        });
    }

    /**
     * Show loading error.
     */
    public void showSavingError() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Couldn't save your note. Please check your network connection and try again.")
                .setNegativeButton("Ok", null)
                .create()
                .show();
    }
}