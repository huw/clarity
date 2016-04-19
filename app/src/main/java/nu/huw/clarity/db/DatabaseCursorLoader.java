package nu.huw.clarity.db;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * An override of the AsyncLoader to utilise my DatabaseHelper,
 * which then loads and returns a Cursor in the background.
 *
 * The rabbit hole goes deeper and deeper with this app.
 */
public class DatabaseCursorLoader extends AsyncTaskLoader<Cursor> {

    DatabaseHelper mDBHelper;

    // Any of these are nullable:
    String mTableName;
    String[] mColumns;
    String mSelection;
    String[] mSelectionArgs;

    public DatabaseCursorLoader(Context context, String tableName, String[] columns,
                                String selection, String[] selectionArgs) {
        super(context);

        mDBHelper = new DatabaseHelper();
        mTableName = tableName;
        mColumns = columns;
        mSelection = selection;
        mSelectionArgs = selectionArgs;

        // Kick everything off, because the class doesn't do it automatically
        onContentChanged();
    }

    /**
     * It needs these two methods because the original code is a little
     * unintuitive and doesn't call the right stuff...
     */
    @Override
    protected void onStartLoading() {

        if (takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    /**
     * This is wrapped in an AsyncTask and should return a cursor
     */
    @Override
    public Cursor loadInBackground() {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        return db.query(mTableName, mColumns, mSelection, mSelectionArgs, null, null, null);
    }
}
