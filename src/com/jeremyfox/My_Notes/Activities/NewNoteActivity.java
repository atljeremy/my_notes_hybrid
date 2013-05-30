package com.jeremyfox.My_Notes.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    private ProgressDialog dialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        showLoadingDialog();

        this.apiHelper = new APIHelper(this);
        this.webView = (WebView)findViewById(R.id.newNoteWebView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new JSInterface(this, this), "MyNotesNative");
        this.webView.loadUrl("http://192.168.1.115:3000/new_note");
        this.webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                NewNoteActivity.this.dialog.dismiss();
            }
        });
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
        if ((null == title || title.length() == 0 || title.equals("undefined"))
                && (null == description || description.length() == 0 || description.equals("undefined"))) {
            showRequiredFieldsError();
            return;
        }
        showSavingDialog();
        this.apiHelper.saveNoteToAPI(title, description, new APIResponse() {
            @Override
            public void requestNotesSuccessfulResponse(JSONArray array) {
            }
            @Override
            public void requestNotesFailureResponse() {
            }

            @Override
            public void saveNoteSuccessfulResponse() {
                NewNoteActivity.this.dialog.dismiss();
                AnalyticsManager.getInstance().fireEvent("new note created successfully", null);
                Toast.makeText(NewNoteActivity.this, getString(R.string.noteSaved), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void saveNoteFailureResponse() {
                NewNoteActivity.this.dialog.dismiss();
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
                NewNoteActivity.this.dialog.dismiss();
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

    /**
     * Show required fields error.
     */
    public void showRequiredFieldsError() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Title and Details are required feilds.")
                .setNegativeButton("Ok", null)
                .create()
                .show();
    }

    /**
     * Shows the loading spinner dialog
     * @return ProgressDialog the progress dialog that will be displayed while loading notes from the API
     */
    private void showSavingDialog() {
        if (null == this.dialog) this.dialog = new ProgressDialog(this);
        this.dialog.setMessage("Saving Note...");
        this.dialog.setCancelable(false);
        this.dialog.show();
        AnalyticsManager.getInstance().fireEvent("showed saving note dialog", null);
    }

    /**
     * Shows the loading spinner dialog
     * @return ProgressDialog the progress dialog that will be displayed while loading notes from the API
     */
    private void showLoadingDialog() {
        if (null == this.dialog) this.dialog = new ProgressDialog(this);
        this.dialog.setMessage("Loading...");
        this.dialog.setCancelable(false);
        this.dialog.show();
        AnalyticsManager.getInstance().fireEvent("showed loading new note screen dialog", null);
    }
}