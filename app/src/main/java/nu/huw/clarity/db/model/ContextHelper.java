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

  private static final String NO_PARENT = Contexts.COLUMN_PARENT_ID + " IS NULL";
  private static final String PARENT_ARG = Contexts.COLUMN_PARENT_ID + " = ?";
  private static final String ID_ARG = Contexts.COLUMN_ID + " = ?";
  private DatabaseHelper dbHelper;
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
   * Get the context with the specified ID
   *
   * @param id ID of a context
   */
  Context getContextFromID(String id) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor =
        dbHelper.query(db, Contexts.TABLE_NAME, Contexts.columns, ID_ARG, new String[]{id});

    cursor.moveToFirst();
    Context result = getContextFromCursor(cursor);

    cursor.close();
    db.close();
    return result;
  }

  /**
   * Gets all contexts matching a given SQL selection
   *
   * @param selection SQL selection matching a list of contexts
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

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Context noContext = new Context();

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
    context.id = dbHelper.getString(cursor, Base.COLUMN_ID);
    context.dateAdded = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED);
    context.dateModified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED);

    // Entries methods
    context.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE);
    context.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN);
    context.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED);
    context.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON);
    context.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE);
    context.name = dbHelper.getString(cursor, Entries.COLUMN_NAME);
    context.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID);
    context.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK);

    // Context methods
    context.altitude = dbHelper.getLong(cursor, Contexts.COLUMN_ALTITUDE);
    context.dropped = dbHelper.getBoolean(cursor, Contexts.COLUMN_DROPPED);
    context.droppedEffective = dbHelper.getBoolean(cursor, Contexts.COLUMN_DROPPED_EFFECTIVE);
    context.latitude = dbHelper.getLong(cursor, Contexts.COLUMN_LATITUDE);
    context.locationName = dbHelper.getString(cursor, Contexts.COLUMN_LOCATION_NAME);
    context.longitude = dbHelper.getLong(cursor, Contexts.COLUMN_LONGITUDE);
    context.onHold = dbHelper.getBoolean(cursor, Contexts.COLUMN_ON_HOLD);
    context.radius = dbHelper.getLong(cursor, Contexts.COLUMN_RADIUS);

    return context;
  }
}
