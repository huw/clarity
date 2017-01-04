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

    private static final String NO_PARENT      = Tasks.COLUMN_PARENT_ID + " IS NULL";
    private static final String NO_CONTEXT     = Tasks.COLUMN_CONTEXT + " IS NULL";
    private static final String PARENT_ARG     = Tasks.COLUMN_PARENT_ID + " = ?";
    private static final String CONTEXT_ARG    = Tasks.COLUMN_CONTEXT + " = ?";
    private static final String IS_PROJECT     = Tasks.COLUMN_PROJECT + " =  1";
    private static final String IS_NOT_PROJECT = Tasks.COLUMN_PROJECT + " =  0";
    private static final String IN_INBOX       = Tasks.COLUMN_INBOX + " = 1";
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
     * @param context Any Task, null (where null will return no tasks), or "NO_CONTEXT", which
     *                will return all tasks with no context.
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
     * Gets all tasks matching a given SQL selection
     *
     * @param selection     SQL selection matching a list of tasks
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
        task.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        task.dateAdded = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        task.dateModified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        task.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE.name);
        task.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN.name);
        task.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED.name);
        task.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON.name);
        task.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        task.countRemaining = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_REMAINING.name);
        task.hasChildren = dbHelper.getBoolean(cursor, Entries.COLUMN_HAS_CHILDREN.name);
        task.name = dbHelper.getString(cursor, Entries.COLUMN_NAME.name);
        task.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID.name);
        task.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK.name);

        // Task methods
        task.blocked = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED.name);
        task.blockedByDefer = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED_BY_DEFER.name);
        task.completeWithChildren =
                dbHelper.getBoolean(cursor, Tasks.COLUMN_COMPLETE_WITH_CHILDREN.name);
        task.context = dbHelper.getString(cursor, Tasks.COLUMN_CONTEXT.name);
        task.dateCompleted = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_COMPLETED.name);
        task.dateDefer = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DEFER.name);
        task.dateDeferEffective = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DEFER_EFFECTIVE.name);
        task.dateDue = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DUE.name);
        task.dateDueEffective = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DUE_EFFECTIVE.name);
        task.dueSoon = dbHelper.getBoolean(cursor, Tasks.COLUMN_DUE_SOON.name);
        task.dropped = dbHelper.getBoolean(cursor, Tasks.COLUMN_DROPPED.name);
        task.estimatedTime = dbHelper.getInt(cursor, Tasks.COLUMN_ESTIMATED_TIME.name);
        task.flagged = dbHelper.getBoolean(cursor, Tasks.COLUMN_FLAGGED.name);
        task.flaggedEffective = dbHelper.getBoolean(cursor, Tasks.COLUMN_FLAGGED_EFFECTIVE.name);
        task.inInbox = dbHelper.getBoolean(cursor, Tasks.COLUMN_INBOX.name);
        task.next = dbHelper.getString(cursor, Tasks.COLUMN_NEXT.name);
        task.notePlaintext = dbHelper.getString(cursor, Tasks.COLUMN_NOTE_PLAINTEXT.name);
        task.noteXML = dbHelper.getString(cursor, Tasks.COLUMN_NOTE_XML.name);
        task.overdue = dbHelper.getBoolean(cursor, Tasks.COLUMN_OVERDUE.name);
        task.project = dbHelper.getBoolean(cursor, Tasks.COLUMN_PROJECT.name);
        task.projectID = dbHelper.getString(cursor, Tasks.COLUMN_PROJECT_ID.name);
        task.lastReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_LAST_REVIEW.name);
        task.nextReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_NEXT_REVIEW.name);
        task.reviewInterval = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_REPEAT_REVIEW.name);
        task.status = dbHelper.getString(cursor, Tasks.COLUMN_PROJECT_STATUS.name);
        task.repetitionMethod = dbHelper.getString(cursor, Tasks.COLUMN_REPETITION_METHOD.name);
        task.repetitionRule = dbHelper.getString(cursor, Tasks.COLUMN_REPETITION_RULE.name);
        task.type = dbHelper.getString(cursor, Tasks.COLUMN_TYPE.name);

        return task;
    }
}
