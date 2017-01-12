package nu.huw.clarity.db.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Entries;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;

class TaskHelper {

  private static final String NO_PARENT = Tasks.COLUMN_PARENT_ID + " IS NULL";
  private static final String NO_CONTEXT = Tasks.COLUMN_CONTEXT + " IS NULL";
  private static final String PARENT_ARG = Tasks.COLUMN_PARENT_ID + " = ?";
  private static final String CONTEXT_ARG = Tasks.COLUMN_CONTEXT + " = ?";
  private static final String ID_ARG = Tasks.COLUMN_ID + " = ?";
  private static final String IS_PROJECT = Tasks.COLUMN_PROJECT + " =  1";
  private static final String IS_NOT_PROJECT = Tasks.COLUMN_PROJECT + " =  0";
  private static final String IN_INBOX = Tasks.COLUMN_INBOX + " = 1";
  private DatabaseHelper dbHelper;

  TaskHelper(DatabaseHelper dbHelper) {

    this.dbHelper = dbHelper;
  }

  /**
   * Returns a list of all tasks.
   */
  List<Task> getAllTasks() {

    return getTasksFromSelection(IS_NOT_PROJECT, null);
  }

  /**
   * Returns a list of any tasks in in the inbox. Very simple.
   */
  List<Task> getTasksInInbox() {

    return getTasksFromSelection(IN_INBOX, null);
  }

  /**
   * Gets a list of child projects matching the given parent folder. Note that projects can
   * only have folders as parents, and will have no child projects.
   *
   * @param parent Any Folder or null, where null will return all top-level projects
   */
  List<Task> getProjectsFromParent(Folder parent) {

    if (parent == null) {
      return getTasksFromSelection(IS_PROJECT + " AND " + NO_PARENT, null);
    } else {
      return getTasksFromSelection(IS_PROJECT + " AND " + PARENT_ARG,
          new String[]{parent.id});
    }
  }

  /**
   * Gets a list of child tasks matching the given parent entry. Note that tasks can only have
   * a project or task parent, or be free-standing (presumably in the inbox but this isn't
   * always the case).
   *
   * @param parent Any Task or null, where null will return all top-level tasks
   */
  List<Task> getTasksFromParent(Task parent) {

    if (parent == null) {
      return getTasksFromSelection(IS_NOT_PROJECT + " AND " + NO_PARENT, null);
    } else {
      return getTasksFromSelection(IS_NOT_PROJECT + " AND " + PARENT_ARG,
          new String[]{parent.id});
    }
  }

  /**
   * Gets a list of child tasks matching the given context.
   *
   * @param context Any Task, null (where null will return no tasks), or "NO_CONTEXT", which will
   * return all tasks with no context.
   */
  List<Task> getTasksFromContext(Context context) {

    if (context == null) {
      return new ArrayList<>();
    } else if (context.id.equals("NO_CONTEXT")) {
      return getTasksFromSelection(IS_NOT_PROJECT + " AND " + NO_CONTEXT, null);
    } else {
      return getTasksFromSelection(IS_NOT_PROJECT + " AND " + CONTEXT_ARG,
          new String[]{context.id});
    }
  }

  /**
   * Get the task with the specified ID
   *
   * @param id ID of a task
   */
  Task getTaskFromID(String id) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor =
        dbHelper.query(db, Tasks.TABLE_NAME, Tasks.columns, ID_ARG, new String[]{id});

    cursor.moveToFirst();
    Task result = getTaskFromCursor(cursor);

    cursor.close();
    db.close();
    return result;
  }

  /**
   * Gets all tasks matching a given SQL selection
   *
   * @param selection SQL selection matching a list of tasks
   * @param selectionArgs Arguments for the selection
   */
  private List<Task> getTasksFromSelection(String selection, String[] selectionArgs) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor =
        dbHelper.query(db, Tasks.TABLE_NAME, Tasks.columns, selection, selectionArgs);

    List<Task> result = new ArrayList<>();
    while (cursor.moveToNext()) {
      result.add(getTaskFromCursor(cursor));
    }

    cursor.close();
    db.close();
    return result;
  }

  /**
   * Fetch a Task object from a given cursor
   *
   * @param cursor A database cursor containing a task object at the current pointer
   */
  private Task getTaskFromCursor(Cursor cursor) {

    Task task = new Task();

    // Base methods
    task.id = dbHelper.getString(cursor, Base.COLUMN_ID);
    task.dateAdded = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED);
    task.dateModified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED);

    // Entries methods
    task.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE);
    task.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN);
    task.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED);
    task.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON);
    task.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE);
    task.countRemaining = task.countChildren - task.countCompleted;
    task.name = dbHelper.getString(cursor, Entries.COLUMN_NAME);
    task.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID);
    task.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK);

    // Task methods
    task.blocked = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED);
    task.deferred = dbHelper.getBoolean(cursor, Tasks.COLUMN_DEFERRED);
    task.completeWithChildren = dbHelper.getBoolean(cursor, Tasks.COLUMN_COMPLETE_WITH_CHILDREN);
    task.contextID = dbHelper.getString(cursor, Tasks.COLUMN_CONTEXT);
    task.dateCompleted = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_COMPLETED);
    task.dateDefer = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DEFER);
    task.dateDeferEffective = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DEFER_EFFECTIVE);
    task.dateDue = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DUE);
    task.dateDueEffective = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DUE_EFFECTIVE);
    task.dueSoon = dbHelper.getBoolean(cursor, Tasks.COLUMN_DUE_SOON);
    task.dropped = dbHelper.getBoolean(cursor, Tasks.COLUMN_DROPPED);
    task.estimatedTime = dbHelper.getDuration(cursor, Tasks.COLUMN_ESTIMATED_TIME);
    task.flagged = dbHelper.getBoolean(cursor, Tasks.COLUMN_FLAGGED);
    task.flaggedEffective = dbHelper.getBoolean(cursor, Tasks.COLUMN_FLAGGED_EFFECTIVE);
    task.inInbox = dbHelper.getBoolean(cursor, Tasks.COLUMN_INBOX);
    task.isProject = dbHelper.getBoolean(cursor, Tasks.COLUMN_PROJECT);
    task.next = dbHelper.getString(cursor, Tasks.COLUMN_NEXT);
    task.notePlaintext = dbHelper.getString(cursor, Tasks.COLUMN_NOTE_PLAINTEXT);
    task.noteXML = dbHelper.getString(cursor, Tasks.COLUMN_NOTE_XML);
    task.overdue = dbHelper.getBoolean(cursor, Tasks.COLUMN_OVERDUE);
    task.projectID = dbHelper.getString(cursor, Tasks.COLUMN_PROJECT_ID);
    task.lastReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_LAST_REVIEW);
    task.nextReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_NEXT_REVIEW);
    task.reviewInterval = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_REPEAT_REVIEW);
    task.status = dbHelper.getString(cursor, Tasks.COLUMN_PROJECT_STATUS);
    task.repetitionMethod = dbHelper.getString(cursor, Tasks.COLUMN_REPETITION_METHOD);
    task.repetitionRule = dbHelper.getString(cursor, Tasks.COLUMN_REPETITION_RULE);
    task.type = dbHelper.getString(cursor, Tasks.COLUMN_TYPE);

    return task;
  }
}