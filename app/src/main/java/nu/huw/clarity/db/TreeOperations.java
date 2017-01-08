package nu.huw.clarity.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Date;
import nu.huw.clarity.BuildConfig;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Tasks;

/**
 * There are a few columns of the SQLite Database that are generated after the sync has completed.
 * They require complex tree operations which, since we have to support API 19, we can't do with
 * the SQL WITH statement, and must be done in code.
 *
 * They are (in no particular order):
 * - Update the 'count' columns with the total number of descendants
 * - Update the 'effective' columns with dates and attributes of parents
 * - Update the 'blocked' columns if a context or project is on hold/dropped
 * - Calculate due dates and overdues
 *
 * This class replaces RecursiveColumnUpdater in order to be cleaner, and perform the function in
 * a more modular pattern (which will be necessary for updating subtrees in the future).
 */
public class TreeOperations {

  public static final int DAY_IN_MILLISECONDS = 86400000;
  private static final String TAG = TreeOperations.class.getSimpleName();
  private DatabaseHelper dbHelper;

  public TreeOperations(Context context) {

    // Strict mode will detect memory problems (very important!)

    if (BuildConfig.DEBUG) {
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    }

    dbHelper = new DatabaseHelper(context);
  }

  /**
   * Update the 'blocked' column for all tasks with an inactive context
   * TODO: Active and ActiveEffective?
   *
   * @param contextID If passed, will only update 'blocked' for that context
   */
  public void updateAttributesForContext(@Nullable String contextID) {

    SQLiteDatabase db = dbHelper.getWritableDatabase();

    if (contextID == null) {
      db.execSQL("UPDATE " + Tasks.TABLE_NAME + " SET " + Tasks.COLUMN_BLOCKED + "='1' WHERE "
          + Tasks.COLUMN_CONTEXT + " IN (SELECT " + Contexts.COLUMN_ID + " FROM "
          + Contexts.TABLE_NAME + " WHERE " + Contexts.COLUMN_ON_HOLD + "='1')");
    } else {
      db.execSQL("UPDATE " + Tasks.TABLE_NAME + " SET " + Tasks.COLUMN_BLOCKED + "='1' WHERE "
          + Tasks.COLUMN_CONTEXT + " IN (SELECT " + Contexts.COLUMN_ID + " FROM "
          + Contexts.TABLE_NAME + " WHERE " + Contexts.COLUMN_ON_HOLD + "='1' AND "
          + Contexts.COLUMN_ID + "=" + DatabaseUtils.sqlEscapeString(contextID) + ")");
    }

    db.close();
  }

  /**
   * Recursively update 'attributes' (defined as non-count columns) of a subtree, or all projects if
   * no parentID is supplied. This is a helper function for a private one with a larger method
   * signature, which calls itself recursively.
   *
   * The function will update the following:
   * - dateDeferEffective, dateDueEffective & flaggedEffective by passing the value to all children
   * - dateDefer, dateDue and flagged by checking if the item already has them set
   * - blocked and blockedByDefer by checking the appropriate conditions (various)
   * - projectStatus and projectID by passing them down the chain with no checks
   * - type by checking first to see if it's set
   * - dueSoon and overdue by checking the passed due/defer dates first
   *
   * @param parentID ID of the subtree's root node to update
   */
  public void updateAttributesForSubtree(@Nullable String parentID) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    updateAttributesForSubtree(db, parentID, null, null, null, null, null, null);
    db.close();
  }

  /**
   * This includes a _lot_ of verbose documentation to pad out the code and make it more
   * understandable. This is just about the worst algorithm I've ever had to write, even after
   * cleaning it up some. I hate it.
   *
   * But unfortunately, it's pretty much the only way. I've looked through different database
   * structures to make this easier (none), and other ways of doing these SQL queries (unsupported),
   * so it looks like we're stuck with this.
   */
  private void updateAttributesForSubtree(@NonNull SQLiteDatabase db,
      @Nullable final String parentID, @Nullable final String parentDateDefer,
      @Nullable final String parentDateDue, @Nullable final String parentFlagged,
      @Nullable final String parentType, @Nullable String projectID,
      @Nullable String projectStatus) {

    // Used to track which task is next for this parent
    // This is done using a standard algorithm to find the minimum rank, with some small
    // modifications for the conditions here. See below.

    long nextRank = Long.MAX_VALUE;
    String nextID = null;

    // Iterate through all children of the parent, or all projects if null

    String[] columns = new String[]{Tasks.COLUMN_ID, Tasks.COLUMN_BLOCKED,
        Tasks.COLUMN_BLOCKED_BY_DEFER, Tasks.COLUMN_DATE_COMPLETED, Tasks.COLUMN_DATE_DEFER,
        Tasks.COLUMN_DATE_DUE, Tasks.COLUMN_FLAGGED, Tasks.COLUMN_PROJECT,
        Tasks.COLUMN_PROJECT_STATUS, Tasks.COLUMN_RANK, Tasks.COLUMN_TYPE};

    Cursor results;
    if (parentID == null) {
      results = db
          .query(Tasks.TABLE_NAME, columns, Tasks.COLUMN_PROJECT + "='1'", null, null, null, null);
    } else {
      results = db
          .query(Tasks.TABLE_NAME, columns, Tasks.COLUMN_PARENT_ID + "=?", new String[]{parentID},
              null, null, null);
    }

    while (results.moveToNext()) {

      // Ordinarily, we'd use a getIndex() thing, but it's more efficient to hardcode this

      if (results.getInt(7) > 0) {

        // These two values should only be set once, at the project level
        // The 7th cursor index is the isProject column, guaranteeing this is a project

        projectID = results.getString(0);
        projectStatus = results.getString(8);
      }

      String childID = results.getString(0);
      boolean childBlocked = results.getInt(1) > 0;
      boolean childBlockedByDefer = results.getInt(2) > 0;
      String childDateCompleted = results.getString(3);
      String childDateDefer = results.getString(4);
      String childDateDue = results.getString(5);
      String childFlagged = results.getString(6);
      long childRank = results.getLong(9);
      String childType = results.getString(10);

      // Update each of the child's attributes appropriately

      ContentValues childValues = new ContentValues();

      // childDateDefer, etc. will be saved into the 'effective' column and passed down.
      // If the child has a defer date already set, then it should be saved and passed down,
      // otherwise the parent's version should override it.

      if (childDateDefer == null) {

        // Remember, set the child version instead of overriding the parent version

        childDateDefer = parentDateDefer;
      }

      if (childDateDue == null) {
        childDateDue = parentDateDue;
      }

      if (childFlagged == null) {
        childFlagged = parentFlagged;
      }

      if (childType == null) {
        childType = parentType;
      }

      // blocked
      // Note that some of this is handled by the context updater function above,
      // and more of it is handled by the 'next task' subroutine below

      if (projectStatus != null &&
          (projectStatus.equals("inactive") || projectStatus.equals("dropped"))) {
        childValues.put(Tasks.COLUMN_BLOCKED, true);
        childBlocked = true;
      }

      // blockedByDefer is handled by a separate function:

      if (childDateDefer != null) {
        if (getBlockedByDefer(Long.valueOf(childDateDefer))) childBlockedByDefer = true;
        childValues.put(Tasks.COLUMN_BLOCKED_BY_DEFER, childBlockedByDefer);
      }

      // dueSoon and overdue are also handled by a separate function:

      if (childDateCompleted == null && childDateDue != null) {
        childValues.putAll(getDueValues(Long.valueOf(childDateDue)));
      }

      // Now save the appropriate values from the parent/project/child into the database

      childValues.put(Tasks.COLUMN_DATE_DEFER_EFFECTIVE, childDateDefer);
      childValues.put(Tasks.COLUMN_DATE_DUE_EFFECTIVE, childDateDue);
      childValues.put(Tasks.COLUMN_FLAGGED_EFFECTIVE, childFlagged);
      childValues.put(Tasks.COLUMN_TYPE, childType);
      childValues.put(Tasks.COLUMN_PROJECT_ID, projectID);
      childValues.put(Tasks.COLUMN_PROJECT_STATUS, projectStatus);

      db.update(Tasks.TABLE_NAME, childValues, Tasks.COLUMN_ID + "=?", new String[]{childID});

      // This recursive call then goes back up to the top, and gets a list of this task's children,
      // using the while loop we're in now. Then it updates all of them. Then it goes into that
      // task's children and does the whole thing over again. Simple.

      updateAttributesForSubtree(db, childID, childDateDefer, childDateDue, childFlagged, childType,
          projectID, projectStatus);

      // The 'next' task in a _sequential_ project is the _first_ _incomplete_, _unblocked_ task.

      if (childRank < nextRank && parentType != null && parentType.equals("sequential")
          && childDateCompleted == null && !childBlocked && !childBlockedByDefer) {
        nextID = childID;
        nextRank = childRank;
      }
    }

    results.close();

    // We only have to deal with the 'next task' column and its consequences if the parent is a
    // sequential project/task.

    if (parentType != null && parentType.equals("sequential")) {

      // Update the 'next task' column for the parent
      // This is primarily used to determine whether the children should be blocked, and it only
      // applies in certain situations.

      ContentValues values = new ContentValues();

      if (nextID != null) {
        values.put(Tasks.COLUMN_NEXT, nextID);
      }

      if (values.size() > 0) {
        db.update(Tasks.TABLE_NAME, values, Tasks.COLUMN_ID + "=?", new String[]{parentID});
      }

      // Update all children that aren't the 'next task' to be blocked

      values.clear();
      values.put(Tasks.COLUMN_BLOCKED, true);
      String selection = Tasks.COLUMN_PARENT_ID + "=? AND " + Tasks.COLUMN_ID + "!=?";
      String[] args = new String[]{parentID, nextID};
      db.update(Tasks.TABLE_NAME, values, selection, args);
    }

    // Don't close the database here because recursion, instead close it in the parent function
  }

  /**
   * Given a number from the column Tasks.DATE_DEFER, determine whether that date is in the future.
   *
   * @param defer Time in milliseconds since the UNIX epoch
   * @return Whether the task should be blocked by its defer date
   */
  private boolean getBlockedByDefer(long defer) {

    if (defer == 0) {
      throw new NullPointerException("Date passed cannot be null");
    }

    ContentValues values = new ContentValues();
    long now = new Date().getTime();

    return defer > now;
  }

  /**
   * Given a number from the column Tasks.DATE_DUE, return a set of ContentValues that can be used
   * to update a database row for that due date.
   * TODO: Determine the 'due soon' date based on user settings
   *
   * @param due Time in milliseconds since the UNIX epoch
   * @return Values for the columns Tasks.OVERDUE or Tasks.DUE_SOON. May be empty.
   */
  private ContentValues getDueValues(long due) {

    if (due == 0) {
      throw new NullPointerException("Date passed cannot be null");
    }

    ContentValues values = new ContentValues();
    long now = new Date().getTime();

    // This used to be done with date objects, but what's the point?
    // We're comparing accurate representations using integers. May as well use math.
    // So this pretty simply decides if the task is overdue, then subtracts 7 days
    // and updates dueSoon if the new 'due' is before now.

    if (now > due) {
      values.put(Tasks.COLUMN_OVERDUE, true);
    } else {
      due -= DAY_IN_MILLISECONDS * 7;
      values.put(Tasks.COLUMN_DUE_SOON, now > due);
    }

    return values;
  }
}
