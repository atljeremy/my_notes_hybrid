package com.jeremyfox.My_Notes.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.jeremyfox.My_Notes.R;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/14/13
 * Time: 8:34 PM
 */
public class NewNoteDialog {

    private AlertDialog dialog;
    private EditText titleInput;
    private EditText detailsInput;

    public NewNoteDialog(Activity activity, String dialogTitle, EditText titleInput, EditText detailsInput, DialogInterface.OnClickListener saveListener){
        this.titleInput = titleInput;
        this.detailsInput = detailsInput;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dialogTitle)
                    .setPositiveButton(activity.getString(R.string.save), saveListener)
                    .setNegativeButton(activity.getString(R.string.cancel), null);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(builder.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lp);
        linearLayout.addView(this.titleInput, lp);
        linearLayout.addView(this.detailsInput, lp);

        builder.setView(linearLayout);

        this.dialog = builder.create();
    }

    public void showDialog() {
        if (null != this.dialog) {
            this.dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            this.dialog.show();
        }
    }
}
