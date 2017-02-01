package nu.huw.clarity.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import nu.huw.clarity.BuildConfig;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.model.Settings;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

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

  private static final String TAG = TreeOperations.class.getSimpleName();
  private DatabaseHelper dbHelper;
  private Duration DUE_SOON_DURATION;

  public TreeOperations(Context context) {

    // Strict mode will detect memory problems (very important!)

    if (BuildConfig.DEBUG) {
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    }

    dbHelper = new DatabaseHelper(context);
    DUE_SOON_DURATION = new Settings(context).getDueSoonDuration();
  }

  /**
   * Update the database columns for an entire subtree, based on its root node's ID. This uses the
   * other functions we've devised for the purpose.
   *
   * @param parentID ID of the root note. If null, the entire tree will be updated.
   */
  public void updateSubtree(@Nullable String parentID) {

    Log.v(TAG, "Updating attributes for contexts");
    updateAttributesForContext(parentID);
    Log.v(TAG, "Updating attributes for projects/tasks");
    updateAttributesForSubtree(parentID);
    Log.v(TAG, "Updating due soon / overdue tags");
    updateDueSoonAndOverdue();
    Log.v(TAG, "Updating counts for everything");
    updateCountsFromTop();

  }

  /**
   * Fill down the 'dropped' attributes on contexts, then update the 'blocked' attribute on all
   * tasks that reference that context, if the context is on hold (not dropped). For more on the
   * logic, see {@link #updateAttributesForSubtree(SQLiteDatabase, String, boolean, String, String,
   * String, String, String, String, String)}  updateAttributesForSubtree()}.
   *
   * @param contextID If passed, will only update for that context
   */
  public void updateAttributesForContext(@Nullable String contextID) {

    SQLiteDatabase db = dbHelper.getWritableDatabase();

    // Fill down the 'dropped' attribute first

    updateAttributesForContext(db, contextID, null);

    // Then run a quick update on all their tasks

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
   * All logic for this subroutine is in {@link #updateAttributesForSubtree(SQLiteDatabase, String,
   * boolean, String, String, String, String, String, String, String)}
   * updateAttributesForSubtree()}, refer to thatmethod first. This essentially fills down the
   * 'dropped' attribute recursively.
   */
  private void updateAttributesForContext(@NonNull SQLiteDatabase db, @Nullable String contextID,
      @Nullable String parentDropped) {

    if (parentDropped == null) {
      parentDropped = "0";
    }

    String[] columns = new String[]{Contexts.COLUMN_ID, Contexts.COLUMN_DROPPED};

    Cursor results;
    if (contextID == null) {
      results = db
          .query(Contexts.TABLE_NAME, columns, Contexts.COLUMN_PARENT_ID + " IS NULL", null, null,
              null, null);
    } else {
      results = db.query(Contexts.TABLE_NAME, columns, Contexts.COLUMN_PARENT_ID + "=?",
          new String[]{contextID}, null, null, null);
    }

    while (results.moveToNext()) {

      String childID = results.getString(0);
      String childDropped = results.getString(1);
      ContentValues childValues = new ContentValues();

      if (childDropped.equals("0")) {
        childDropped = parentDropped;
      }

      childValues.put(Contexts.COLUMN_DROPPED_EFFECTIVE, childDropped);
      db.update(Contexts.TABLE_NAME, childValues, Contexts.COLUMN_ID + "=?", new String[]{childID});

      updateAttributesForContext(db, childID, childDropped);
    }

    results.close();
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
    updateAttributesForSubtree(db, parentID, false, null, null, null, null, null, null, null);
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
      @Nullable final String parentID, final boolean parentBlocked,
      @Nullable final String parentDateDefer, @Nullable final String parentDateDue,
      @Nullable final String parentFlagged, @Nullable final String parentType,
      @Nullable String projectID, @Nullable String projectStatus, @Nullable String projectType) {

    // Used to track which task is next for this parent
    // This is done using a standard algorithm to find the minimum rank, with some small
    // modifications for the conditions here. See below.

    long nextRank = Long.MAX_VALUE;
    String nextID = null;

    // Iterate through all children of the parent, or all projects if null

    String[] columns = new String[]{Tasks.COLUMN_ID, Tasks.COLUMN_BLOCKED,
        Tasks.COLUMN_DEFERRED, Tasks.COLUMN_DATE_COMPLETED, Tasks.COLUMN_DATE_DEFER,
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
        projectType = results.getString(10);
      }

      String childID = results.getString(0);
      boolean childBlocked = results.getInt(1) > 0;
      boolean childDeferred = results.getInt(2) > 0;
      String childDateCompleted = results.getString(3);
      String childDateDefer = results.getString(4);
      String childDateDue = results.getString(5);
      String childFlagged = results.getString(6);
      long childRank = results.getLong(9);
      String childType = results.getString(10);

      boolean childDropped = false;

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

      if (childFlagged.equals("0")) {
        childFlagged = parentFlagged;
      }

      if (parentBlocked) {

        // If the parent is blocked, then so is the child

        childBlocked = true;
      }

      // blocked/dropped
      // Note that some of this is handled by the context updater function above,
      // and more of it is handled by the 'next task' subroutine below

      if (projectStatus != null && projectStatus.equals("inactive")) {
        childValues.put(Tasks.COLUMN_BLOCKED, true);
        childBlocked = true;
      }

      if (projectStatus != null && projectStatus.equals("dropped")) {
        childValues.put(Tasks.COLUMN_DROPPED, true);
        childDropped = true; // doesn't matter if we use this, not passed on
      }

      // blockedByDefer is handled by a separate function:

      if (childDateDefer != null) {
        if (getDeferred(childDateDefer)) childDeferred = true;
        childValues.put(Tasks.COLUMN_DEFERRED, childDeferred);
      }

      // Now save the appropriate values from the parent/project/child into the database

      childValues.put(Tasks.COLUMN_BLOCKED, childBlocked);
      childValues.put(Tasks.COLUMN_DATE_DEFER_EFFECTIVE, childDateDefer);
      childValues.put(Tasks.COLUMN_DATE_DUE_EFFECTIVE, childDateDue);
      childValues.put(Tasks.COLUMN_FLAGGED_EFFECTIVE, childFlagged);
      childValues.put(Tasks.COLUMN_PROJECT_ID, projectID);
      childValues.put(Tasks.COLUMN_PROJECT_STATUS, projectStatus);

      db.update(Tasks.TABLE_NAME, childValues, Tasks.COLUMN_ID + "=?", new String[]{childID});

      // This recursive call then goes back up to the top, and gets a list of this task's children,
      // using the while loop we're in now. Then it updates all of them. Then it goes into that
      // task's children and does the whole thing over again. Simple.

      updateAttributesForSubtree(db, childID, childBlocked, childDateDefer, childDateDue,
          childFlagged, childType, projectID, projectStatus, projectType);

      // The 'next' task in a _sequential_ project is the _first_ _incomplete_ task, whenever the
      // project is _not a single-action list_. Blocking and deferring actually just holds up the
      // whole 'bit'.

      if (childRank < nextRank && parentType != null && parentType.equals("sequential")
          && childDateCompleted == null && !childDropped && !parentType.equals("single action")) {
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
   * Recursively update the counts of children for everything (Projects, Folders, Contexts, Nested
   * Tasks) using a bunch of crazy SQL magic.
   *
   * But seriously, this does the following: - Simple SQL queries to get the children of each
   * project - Semi-recursive SQL queries to get the children of each context - Semi-recursive SQL
   * queries to get the children of each folder - Recursive SQL queries to get the children of each
   * nested task (P.S.: Not in that order.)
   *
   * This is an effort, and it sucks because we don't have access to useful SQLite statements since
   * we have to support API 19. But in the end, it works somehow. I'm not bothered to figure it out,
   * just know that it does a good job. I am just re-using old code so forgive how hacky it is.
   */
  public void updateCountsFromTop() {

    SQLiteDatabase db = dbHelper.getWritableDatabase();

    // PROJECTS

    String[] projectColumns = new String[]{Tasks.COLUMN_ID, Tasks.COLUMN_PARENT_ID};
    Cursor projects = db
        .query(Tasks.TABLE_NAME, projectColumns, Tasks.COLUMN_PROJECT + "='1'", null, null, null,
            null);

    HashMap<String, Long[]> folderCounts = new HashMap<>();

    while (projects.moveToNext()) {

      String id = projects.getString(0);
      updateProjectCounts(db, id);

      long countChildren = DatabaseUtils
          .queryNumEntries(db, Tasks.TABLE_NAME, Tasks.COLUMN_PROJECT_ID + "=?",
              new String[]{id});
      long countAvailable = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_PROJECT_ID + "=? AND " + Tasks.COLUMN_DATE_COMPLETED + " IS NULL AND "
              + Tasks.COLUMN_BLOCKED + "=0 AND " + Tasks.COLUMN_DEFERRED + "=0 AND "
              + Tasks.COLUMN_DROPPED + "=0",
          new String[]{id});
      long countCompleted = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_PROJECT_ID + "=? AND (" + Tasks.COLUMN_DATE_COMPLETED + " IS NOT NULL OR "
              + Tasks.COLUMN_DROPPED + "=1)",
          new String[]{id});
      long countDueSoon = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_PROJECT_ID + "=? AND " + Tasks.COLUMN_DUE_SOON + "=1",
          new String[]{id});
      long countOverdue = DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_PROJECT_ID + "=? AND " + Tasks.COLUMN_OVERDUE + "=1",
          new String[]{id});

      ContentValues values = new ContentValues();

      // There's no point wasting resources on a call which is going to zero out columns
      // which are already set to zero. This is much more efficient if we only update the
      // columns which have counts.

      if (countChildren > 0) {
        values.put(Tasks.COLUMN_COUNT_CHILDREN, countChildren);
      }
      if (countAvailable > 0) {
        values.put(Tasks.COLUMN_COUNT_AVAILABLE, countAvailable);
      }
      if (countCompleted > 0) {
        values.put(Tasks.COLUMN_COUNT_COMPLETED, countCompleted);
      }
      if (countDueSoon > 0) {
        values.put(Tasks.COLUMN_COUNT_DUE_SOON, countDueSoon);
      }
      if (countOverdue > 0) {
        values.put(Tasks.COLUMN_COUNT_OVERDUE, countOverdue);
      }

      // And if none of them have counts (an empty project, for some reason), don't update
      // anything.

      if (values.size() > 0) {
        db.update(Tasks.TABLE_NAME, values, Tasks.COLUMN_ID + "=?", new String[]{id});
      }

      String parentID = projects.getString(1);
      if (parentID != null) {

        Long[] childCounts = {countChildren, countAvailable, countCompleted, countDueSoon,
            countOverdue};

        if (folderCounts.containsKey(parentID)) {
          for (int j = 0; j < childCounts.length; j++) {
            folderCounts.get(parentID)[j] += childCounts[j];
          }
        } else {
          folderCounts.put(parentID, childCounts);
        }
      }
    }

    projects.close();

    // FOLDERS
    // We need a LinkedList because we're adding and removing objects. This is really hacky
    // because I did it at like half past midnight and I really wanted to sleep. So I kinda
    // understand how it works, but at the same time, I don't. It _is_ pretty fast though,
    // somehow.

    List<Object> array = new LinkedList<>(Arrays.asList(folderCounts.keySet().toArray()));
    while (!array.isEmpty()) {
      String id = (String) array.get(0);

      // This bit just gets the ID of the folder's parent
      Cursor cursor = db.query(Folders.TABLE_NAME, new String[]{Folders.COLUMN_PARENT_ID},
          Folders.COLUMN_ID + " = ?", new String[]{id}, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        String parentID = cursor.getString(0);

        Long[] childCounts = folderCounts.get(id);

        // Add up the child counts for the folder

        if (folderCounts.containsKey(parentID)) {
          for (int j = 0; j < childCounts.length; j++) {
            folderCounts.get(parentID)[j] += childCounts[j];
          }
        } else {

          // If this folder isn't in our HashMap yet, add it. But also add an extra
          // iteration to this loop.

          if (parentID != null) {
            folderCounts.put(parentID, childCounts);
            array.add(parentID);
          }
        }
      }

      if (cursor != null) {
        cursor.close();
      }

      // Could probably be better handled than this, but it works.
      array.remove(0);
    }

    // Simply add each Folder's new counts to the database.

    for (String id : folderCounts.keySet()) {
      Long[] childCounts = folderCounts.get(id);
      ContentValues values = new ContentValues();

      values.put(Tasks.COLUMN_COUNT_CHILDREN, childCounts[0]);
      values.put(Tasks.COLUMN_COUNT_AVAILABLE, childCounts[1]);
      values.put(Tasks.COLUMN_COUNT_COMPLETED, childCounts[2]);
      values.put(Tasks.COLUMN_COUNT_DUE_SOON, childCounts[3]);
      values.put(Tasks.COLUMN_COUNT_OVERDUE, childCounts[4]);

      db.update(Folders.TABLE_NAME, values, Folders.COLUMN_ID + " = ?", new String[]{id});
    }

    // CONTEXTS
    // See the function below (which is conveniently recursive)

    updateContextCounts(db, null);

    db.close();
  }

  /**
   * Recursive helper task used by {@link #updateCountsFromTop()}. Based on {@link
   * #updateProjectCounts(SQLiteDatabase, String)}
   */
  private void updateContextCounts(@NonNull SQLiteDatabase db, @Nullable String id) {

    String[] columns = new String[]{Tasks.COLUMN_ID, Tasks.COLUMN_COUNT_AVAILABLE,
        Tasks.COLUMN_COUNT_CHILDREN, Tasks.COLUMN_COUNT_COMPLETED, Tasks.COLUMN_COUNT_DUE_SOON,
        Tasks.COLUMN_COUNT_OVERDUE};

    Cursor children;
    if (id == null) {
      children = db
          .query(Contexts.TABLE_NAME, columns, Contexts.COLUMN_PARENT_ID + " IS NULL", null, null,
              null, null);
    } else {
      children = db.query(Contexts.TABLE_NAME, columns, Contexts.COLUMN_PARENT_ID + "=?",
          new String[]{id}, null, null, null);
    }

    int countChildren, countCompleted, countDueSoon, countOverdue, countAvailable;
    countChildren = countCompleted = countDueSoon = countOverdue = countAvailable = 0;

    ContentValues values = new ContentValues();

    while (children.moveToNext()) {

      String childID = children.getString(0);
      updateContextCounts(db, childID);

      // Add counts for children

      countChildren += children.getLong(2);
      countAvailable += children.getLong(1);
      countCompleted += children.getLong(3);
      countDueSoon += children.getLong(4);
      countOverdue += children.getLong(5);

      // Add count for this context's tasks

      countChildren += DatabaseUtils
          .queryNumEntries(db, Tasks.TABLE_NAME, Tasks.COLUMN_CONTEXT + "=?",
              new String[]{childID});
      countAvailable += DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_CONTEXT + "=? AND " + Tasks.COLUMN_DATE_COMPLETED + " IS NULL AND "
              + Tasks.COLUMN_BLOCKED + "=0 AND " + Tasks.COLUMN_DEFERRED + "=0 AND "
              + Tasks.COLUMN_DROPPED + "=0",
          new String[]{childID});
      countCompleted += DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_CONTEXT + "=? AND (" + Tasks.COLUMN_DATE_COMPLETED + " IS NOT NULL OR "
              + Tasks.COLUMN_DROPPED + "=1)",
          new String[]{childID});
      countDueSoon += DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_CONTEXT + "=? AND " + Tasks.COLUMN_DUE_SOON + "=1",
          new String[]{childID});
      countOverdue += DatabaseUtils.queryNumEntries(db, Tasks.TABLE_NAME,
          Tasks.COLUMN_CONTEXT + "=? AND " + Tasks.COLUMN_OVERDUE + "=1",
          new String[]{childID});
    }

    children.close();

    // Child counts
    if (countChildren > 0) {
      values.put(Contexts.COLUMN_COUNT_CHILDREN, countChildren);
    }
    if (countAvailable > 0) {
      values.put(Contexts.COLUMN_COUNT_AVAILABLE, countAvailable);
    }
    if (countCompleted > 0) {
      values.put(Contexts.COLUMN_COUNT_COMPLETED, countCompleted);
    }
    if (countDueSoon > 0) {
      values.put(Contexts.COLUMN_COUNT_DUE_SOON, countDueSoon);
    }
    if (countOverdue > 0) {
      values.put(Contexts.COLUMN_COUNT_OVERDUE, countOverdue);
    }

    // Update
    if (values.size() > 0) {
      db.update(Contexts.TABLE_NAME, values, Contexts.COLUMN_ID + "=?", new String[]{id});
    }
  }

  /**
   * Recursive helper task used by {@link #updateCountsFromTop()}. Also old, also hacky. Please
   * forgive.
   */
  private void updateProjectCounts(@NonNull SQLiteDatabase db, @NonNull String id) {

    String[] columns = new String[]{Tasks.COLUMN_ID, Tasks.COLUMN_BLOCKED,
        Tasks.COLUMN_DEFERRED, Tasks.COLUMN_DATE_COMPLETED, Tasks.COLUMN_DROPPED,
        Tasks.COLUMN_DUE_SOON, Tasks.COLUMN_OVERDUE, Tasks.COLUMN_COUNT_AVAILABLE,
        Tasks.COLUMN_COUNT_CHILDREN, Tasks.COLUMN_COUNT_COMPLETED, Tasks.COLUMN_COUNT_DUE_SOON,
        Tasks.COLUMN_COUNT_OVERDUE};

    Cursor children = db
        .query(Tasks.TABLE_NAME, columns, Tasks.COLUMN_PARENT_ID + "=?", new String[]{id}, null,
            null, null);

    int countChildren, countCompleted, countDueSoon, countOverdue, countAvailable;
    countChildren = countCompleted = countDueSoon = countOverdue = countAvailable = 0;

    ContentValues values = new ContentValues();

    while (children.moveToNext()) {

      String childID = children.getString(0);

      // Descend the call stack until we hit the bottom, then build our way back up.

      // Please, if you're reading this from the future, forgive me for the terrible code.
      // It's lonely and dark in here and I really have no idea what I'm doing or how I'm
      // supposed to handle this properly. Databases are confusing and weird and I'd rather
      // build my own sync service that uses some kind of hierarchical JSON but I don't
      // have much choice.

      updateProjectCounts(db, childID);

      // Initialise variables on the way up to save memory

      boolean childBlocked = children.getInt(1) > 0;
      boolean childBlockedByDefer = children.getInt(2) > 0;
      boolean childCompleted = children.getString(3) != null;
      boolean childDropped = children.getInt(4) > 0;
      int childDueSoon = children.getInt(5);
      int childOverdue = children.getInt(6);

      long childCountChildren = children.getLong(8);
      long childCountAvailable = children.getLong(7);
      long childCountCompleted = children.getLong(9);
      long childCountDueSoon = children.getLong(10);
      long childCountOverdue = children.getLong(11);

      // All of the code past the recursive call will happen on the way back out, or up the
      // tree. Once each call returns, this code runs on the level above where the code was
      // just calling to.
      //
      // Here, the level above is able to access the counts for children _of each of its
      // children_. It gets really meta and confusing. Took me like 3-4 hours to work out
      // (but, to be fair, this was more bug-fixing than algorithms).

      // Add count for self (where applicable)

      countChildren += 1;
      countDueSoon += childDueSoon;
      countOverdue += childOverdue;

      if (childCompleted || childDropped) {
        countCompleted += 1;
      }

      if (!childCompleted && !childBlocked && !childBlockedByDefer && !childDropped) {
        countAvailable += 1;
      }

      // Add counts for children
      countChildren += childCountChildren;
      countAvailable += childCountAvailable;
      countCompleted += childCountCompleted;
      countDueSoon += childCountDueSoon;
      countOverdue += childCountOverdue;
    }

    children.close();

    // Child counts
    if (countChildren > 0) {
      values.put(Tasks.COLUMN_COUNT_CHILDREN, countChildren);
    }
    if (countAvailable > 0) {
      values.put(Tasks.COLUMN_COUNT_AVAILABLE, countAvailable);
    }
    if (countCompleted > 0) {
      values.put(Tasks.COLUMN_COUNT_COMPLETED, countCompleted);
    }
    if (countDueSoon > 0) {
      values.put(Tasks.COLUMN_COUNT_DUE_SOON, countDueSoon);
    }
    if (countOverdue > 0) {
      values.put(Tasks.COLUMN_COUNT_OVERDUE, countOverdue);
    }

    // Update
    if (values.size() > 0) {
      db.update(Tasks.TABLE_NAME, values, Tasks.COLUMN_ID + "=?", new String[]{id});
    }
  }

  /**
   * Given a number from the column Tasks.DATE_DEFER, determine whether that date is in the future.
   *
   * @param deferString ISO 8601 timestamp
   * @return Whether the task should be blocked by its defer date
   */
  private boolean getDeferred(@NonNull String deferString) {

    Instant now = Instant.now();
    Instant defer = Instant.parse(deferString);

    return defer.isAfter(now);
  }

  /**
   * Based on the current time, will update the due soon and overdue flags for everything in the
   * database.
   */
  public void updateDueSoonAndOverdue() {

    // Get date strings for raw SQL comparison

    Instant now = Instant.now();
    String[] dueSoonString = new String[]{now.plus(DUE_SOON_DURATION).toString()};
    String[] overdueString = new String[]{now.toString()};

    // Setup ContentValues for updating overdue and due soon

    ContentValues dueSoonValue = new ContentValues();
    ContentValues overdueValue = new ContentValues();
    dueSoonValue.put(Tasks.COLUMN_DUE_SOON, "1");
    overdueValue.put(Tasks.COLUMN_OVERDUE, "1");

    ContentValues dueSoonClear = new ContentValues();
    ContentValues overdueClear = new ContentValues();
    dueSoonClear.put(Tasks.COLUMN_DUE_SOON, "0");
    overdueClear.put(Tasks.COLUMN_OVERDUE, "0");

    // Run queries
    // The queries here are cheaper ways of achieving an ordinary comparison. Since ISO 8601 date
    // strings are formatted left-to-right descending, they can be lexicographically compared by
    // the SQLite engine.

    SQLiteDatabase db = dbHelper.getWritableDatabase();

    db.update(Tasks.TABLE_NAME, overdueClear, null, null);
    db.update(Tasks.TABLE_NAME, dueSoonClear, null, null);

    db.update(Tasks.TABLE_NAME, overdueValue,
        Tasks.COLUMN_DATE_COMPLETED + " IS NULL AND " + Tasks.COLUMN_DATE_DUE_EFFECTIVE + " <= ?",
        overdueString);
    db.update(Tasks.TABLE_NAME, dueSoonValue,
        Tasks.COLUMN_DATE_COMPLETED + " IS NULL AND " + Tasks.COLUMN_OVERDUE + " = 0 AND "
            + Tasks.COLUMN_DATE_DUE_EFFECTIVE + " <= ?", dueSoonString);

    db.close();
  }
}
