package com.jeremyfox.My_Notes.ContentProviders;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.jeremyfox.My_Notes.Helpers.FileStorageHelper;
import com.jeremyfox.My_Notes.Interfaces.Note;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/23/13
 * Time: 9:39 PM
 */
public class NotesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.jeremyfox.My_Notes.notesprovider";

    public static class NotesData implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/items");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jeremyfox.My_Notes.item";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jeremyfox.My_Notes.item";
        public static final String RECORD_ID_COLUMN = "record_id";
        public static final String TITLE_COLUMN = "title";
        public static final String DETAILS_COLUMN = "details";
        public static final String[] PROJECTION = {"_Id", RECORD_ID_COLUMN, TITLE_COLUMN, DETAILS_COLUMN};

        private NotesData() {}
    }

    public static final int NOTES = 1;
    public static final int NOTES_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "items/", NOTES);
        uriMatcher.addURI(AUTHORITY, "items/#", NOTES_ID);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {

        MatrixCursor cursor = new MatrixCursor(NotesData.PROJECTION);

        String jsonString = FileStorageHelper.readStringFromFile(getContext(), FileStorageHelper.NOTES_FILE_PATH);
        JSONArray jsonArray = null;
        JSONObject field = null;

        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (null != jsonArray) {
            switch (uriMatcher.match(uri)) {
                case NOTES:
                    for (int i=0; i<jsonArray.length(); i++) {
                        try {
                            field = (JSONObject) jsonArray.get(i);
                            cursor.addRow(new Object[] {
                                    i + 1,
                                    field.get(Note.RECORD_ID_KEY),
                                    field.get(Note.RECORD_TITLE_KEY),
                                    field.get(Note.RECORD_DETAILS_KEY)
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case NOTES_ID:
                    String itemID = uri.getLastPathSegment();
                    int index = -1;
                    try {
                        index = Integer.parseInt(itemID);
                    } catch (NumberFormatException e) {
                        Log.e("NotesProvider", "Invalid Index: " + itemID);
                    }

                    if (index > 0 && index <= jsonArray.length()) {
                        try {
                            field = (JSONObject) jsonArray.get(index - 1);
                            cursor.addRow(new Object[] {
                                    index,
                                    field.get(Note.RECORD_ID_KEY),
                                    field.get(Note.RECORD_TITLE_KEY),
                                    field.get(Note.RECORD_DETAILS_KEY)
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    break;

                default:
                    Log.e("NotesProvider", "Invalid URI: " + uri.toString());
                    break;
            }
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case NOTES:
                return NotesData.CONTENT_TYPE;

            case NOTES_ID:
                return NotesData.CONTENT_ITEM_TYPE;
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }
}
