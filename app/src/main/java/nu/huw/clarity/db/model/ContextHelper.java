package nu.huw.clarity.db.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Entries;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Context;

class ContextHelper {

    private static final String NO_PARENT  = Contexts.COLUMN_PARENT_ID + " IS NULL";
    private static final String PARENT_ARG = Contexts.COLUMN_PARENT_ID + " = ?";
    private DatabaseHelper          dbHelper;
    private android.content.Context androidContext;

    ContextHelper(DatabaseHelper dbHelper, android.content.Context context) {

        this.dbHelper = dbHelper;
        this.androidContext = context;
    }

    /**
     * Gets a list of child contexts matching the given parent context
     *
     * @param parent Any Context or null, where null will return all top-level contexts
     */
    List<Context> getContextsFromParent(Context parent) {

        if (parent == null) {

            // Add 'No Context' to the top of the top-level context list

            List<Context> result = new ArrayList<>();
            result.add(getNoContext());
            result.addAll(getContextsFromSelection(NO_PARENT, null));
            return result;
        } else {

            // Otherwise, get contexts belonging to this one

            return getContextsFromSelection(PARENT_ARG, new String[]{parent.id});
        }
    }

    /**
     * Gets all contexts matching a given SQL selection
     *
     * @param selection     SQL selection matching a list of contexts
     * @param selectionArgs Arguments for the selection
     */
    private List<Context> getContextsFromSelection(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Contexts.TABLE_NAME, Contexts.columns, selection, selectionArgs);

        List<Context> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getContextFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    // TODO: Fix after dynamically calculating child counts
    private Context getNoContext() {

        SQLiteDatabase db        = dbHelper.getReadableDatabase();
        Context        noContext = new Context();

        noContext.name = androidContext.getString(R.string.no_context);
        /*noContext.countAvailable = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + AVAILABLE);
        noContext.countDueSoon = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + DUE_SOON);
        noContext.countOverdue = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + OVERDUE);*/
        noContext.id = "NO_CONTEXT";

        db.close();
        return noContext;
    }

    /**
     * Fetch a Context object from a given cursor
     *
     * @param cursor A database cursor containing a context object at the current pointer
     */
    private Context getContextFromCursor(Cursor cursor) {

        Context context = new Context();

        // Base methods
        context.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        context.dateAdded = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        context.dateModified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        context.active = dbHelper.getBoolean(cursor, Entries.COLUMN_ACTIVE.name);
        context.activeEffective = dbHelper.getBoolean(cursor, Entries.COLUMN_ACTIVE_EFFECTIVE.name);
        context.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE.name);
        context.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN.name);
        context.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED.name);
        context.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON.name);
        context.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        context.countRemaining = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_REMAINING.name);
        context.hasChildren = dbHelper.getBoolean(cursor, Entries.COLUMN_HAS_CHILDREN.name);
        context.name = dbHelper.getString(cursor, Entries.COLUMN_NAME.name);
        context.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID.name);
        context.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK.name);

        // Context methods
        context.altitude = dbHelper.getLong(cursor, Contexts.COLUMN_ALTITUDE.name);
        context.latitude = dbHelper.getLong(cursor, Contexts.COLUMN_LATITUDE.name);
        context.locationName = dbHelper.getString(cursor, Contexts.COLUMN_LOCATION_NAME.name);
        context.longitude = dbHelper.getLong(cursor, Contexts.COLUMN_LONGITUDE.name);
        context.onHold = dbHelper.getBoolean(cursor, Contexts.COLUMN_ON_HOLD.name);
        context.radius = dbHelper.getLong(cursor, Contexts.COLUMN_RADIUS.name);

        return context;
    }
}
