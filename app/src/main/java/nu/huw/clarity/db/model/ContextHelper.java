package nu.huw.clarity.db.model;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Entries;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Header;

class ContextHelper {

  private static final String NO_PARENT = Contexts.COLUMN_PARENT_ID + " IS NULL";
  private static final String PARENT_ARG = Contexts.COLUMN_PARENT_ID + " = ?";
  private static final String ID_ARG = Contexts.COLUMN_ID + " = ?";
  private DatabaseHelper dbHelper;

  ContextHelper(DatabaseHelper dbHelper) {

    this.dbHelper = dbHelper;
  }

  /**
   * Gets a list of child contexts matching the given parent context
   *
   * @param parent Any Context or null, where null will return all top-level contexts
   */
  List<Context> getContextsFromParent(Context parent, android.content.Context androidContext) {

    if (parent == null) {

      // Add 'No Context' to the top of the top-level context list

      List<Context> result = new ArrayList<>();
      result.add(getNoContext(androidContext));
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

  /**
   * Gets a context object representing the tasks with no listed context. It opens a database
   * connection to find the numbers of tasks without this context.
   *
   * @return A context object representing the tasks with no context
   */
  private Context getNoContext(android.content.Context androidContext) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Context noContext = new Context();

    noContext.name = androidContext.getString(R.string.listitem_nocontext);
    noContext.countChildren = DatabaseUtils
        .queryNumEntries(db, Tasks.TABLE_NAME, Tasks.COLUMN_CONTEXT + " IS NULL");
    noContext.countAvailable = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
        Tasks.COLUMN_CONTEXT + " IS NULL AND " + Tasks.COLUMN_DATE_COMPLETED + " IS NULL AND "
            + Tasks.COLUMN_BLOCKED + "=0 AND " + Tasks.COLUMN_DEFERRED + "=0 AND "
            + Tasks.COLUMN_DROPPED + "=0");
    noContext.countCompleted = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
        Tasks.COLUMN_CONTEXT + " IS NULL AND (" + Tasks.COLUMN_DATE_COMPLETED + " IS NOT NULL OR "
            + Tasks.COLUMN_DROPPED + "=1)");
    noContext.countDueSoon = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
        Tasks.COLUMN_CONTEXT + " IS NULL AND " + Tasks.COLUMN_DUE_SOON + "=1");
    noContext.countOverdue = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
        Tasks.COLUMN_CONTEXT + " IS NULL AND " + Tasks.COLUMN_OVERDUE + "=1");
    noContext.countRemaining = noContext.countChildren - noContext.countCompleted;
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
    context.countRemaining = context.countChildren - context.countCompleted;
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

  /**
   * Get a list of Header objects representing all contexts and IDs
   */
  List<Header> getContextHeaders() {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String[] columns = new String[]{Contexts.COLUMN_ID, Contexts.COLUMN_NAME};
    Cursor cursor = db.query(Contexts.TABLE_NAME, columns, null, null, null, null, null);

    List<Header> result = new ArrayList<>();
    while (cursor.moveToNext()) {

      Header header = new Header(cursor.getString(1));
      header.contextID = cursor.getString(0);
      result.add(header);

    }

    cursor.close();
    db.close();
    return result;
  }
}
