package nu.huw.clarity.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Entry;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;

/**
 * Like DatabaseHelper, DataModelHelper contains a number of helper classes for interacting with
 * the database, but only those specific to interacting with the data model (for views). Getters
 * and setters are all welcome.
 */
public class DataModelHelper {

    private static final String TAG = DataModelHelper.class.getSimpleName();
    private DatabaseHelper dbHelper;

    public DataModelHelper() {

        dbHelper = new DatabaseHelper();
    }

    /**
     * Given the ID of an attachment, get it into the data model
     */
    public Attachment getAttachment(String id) {

        SQLiteDatabase db            = dbHelper.getWritableDatabase();
        String         selection     = Attachments.COLUMN_ID + " = ?";
        String[]       selectionArgs = {id};

        Cursor cursor = dbHelper.query(db, Attachments.TABLE_NAME, Attachments.columns, selection,
                                       selectionArgs);
        cursor.moveToFirst();

        Attachment result = getAttachmentFromCursor(cursor);

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Fetch attachment data from a cursor.
     */
    private Attachment getAttachmentFromCursor(Cursor cursor) {

        Attachment attachment = new Attachment();

        attachment.id = dbHelper.getString(cursor, Attachments.COLUMN_ID.name);
        attachment.name = dbHelper.getString(cursor, Attachments.COLUMN_NAME.name);
        attachment.parentID = dbHelper.getString(cursor, Attachments.COLUMN_PARENT_ID.name);
        attachment.path = dbHelper.getString(cursor, Attachments.COLUMN_PATH.name);
        // attachment.preview = dbHelper.getString(cursor, Attachments.COLUMN_PNG_PREVIEW.name);
        // TODO
        attachment.added = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_ADDED.name);
        attachment.modified = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_MODIFIED.name);

        return attachment;
    }

    /**
     * Gets all contexts given a selection string and argument. Usually should be accessed by
     * other methods.
     */
    public List<Context> getContexts(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
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
     * Shorthand for empty argument, will get all contexts
     */
    public List<Context> getContexts() {

        return getContexts(null, null);
    }

    /**
     * Given a context ID, get all child contexts
     */
    public List<Context> getContexts(String parentID) {

        return getContexts(Contexts.COLUMN_PARENT_ID + " = ?", new String[]{parentID});
    }

    public List<Context> getTopLevelContexts() {

        String selection = Contexts.COLUMN_PARENT_ID + " IS NULL AND " + Contexts.COLUMN_ACTIVE +
                           " = 1 AND " + Contexts.COLUMN_ACTIVE_EFFECTIVE + " = 1";
        return getContexts(selection, null);
    }

    private Context getContextFromCursor(Cursor cursor) {

        Context context = new Context();

        // Base methods
        context.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        context.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        context.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entry methods
        context.active = dbHelper.getBoolean(cursor, Entry.COLUMN_ACTIVE.name);
        context.activeEffective = dbHelper.getBoolean(cursor, Entry.COLUMN_ACTIVE_EFFECTIVE.name);
        context.countAvailable = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_AVAILABLE.name);
        context.countChildren = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_CHILDREN.name);
        context.countCompleted = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_COMPLETED.name);
        context.countDueSoon = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_DUE_SOON.name);
        context.countOverdue = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_OVERDUE.name);
        context.countRemaining = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_REMAINING.name);
        context.hasChildren = dbHelper.getBoolean(cursor, Entry.COLUMN_HAS_CHILDREN.name);
        context.name = dbHelper.getString(cursor, Entry.COLUMN_NAME.name);
        context.parentID = dbHelper.getString(cursor, Entry.COLUMN_PARENT_ID.name);
        context.rank = dbHelper.getLong(cursor, Entry.COLUMN_RANK.name);

        // Context methods
        context.altitude = dbHelper.getLong(cursor, Contexts.COLUMN_ALTITUDE.name);
        context.latitude = dbHelper.getLong(cursor, Contexts.COLUMN_LATITUDE.name);
        context.locationName = dbHelper.getString(cursor, Contexts.COLUMN_LOCATION_NAME.name);
        context.longitude = dbHelper.getLong(cursor, Contexts.COLUMN_LONGITUDE.name);
        context.onHold = dbHelper.getBoolean(cursor, Contexts.COLUMN_ON_HOLD.name);
        context.radius = dbHelper.getLong(cursor, Contexts.COLUMN_RADIUS.name);

        return context;
    }

    public List<Folder> getFolders(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor =
                dbHelper.query(db, Folders.TABLE_NAME, Folders.columns, selection, selectionArgs);

        List<Folder> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getFolderFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<Folder> getFolders(String parentID) {

        return getFolders(Folders.COLUMN_PARENT_ID + " = ?", new String[]{parentID});
    }

    public List<Folder> getFolders() {

        return getFolders(null, null);
    }

    private Folder getFolderFromCursor(Cursor cursor) {

        Folder folder = new Folder();

        // Base methods
        folder.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        folder.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        folder.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entry methods
        folder.active = dbHelper.getBoolean(cursor, Entry.COLUMN_ACTIVE.name);
        folder.activeEffective = dbHelper.getBoolean(cursor, Entry.COLUMN_ACTIVE_EFFECTIVE.name);
        folder.countAvailable = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_AVAILABLE.name);
        folder.countChildren = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_CHILDREN.name);
        folder.countCompleted = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_COMPLETED.name);
        folder.countDueSoon = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_DUE_SOON.name);
        folder.countOverdue = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_OVERDUE.name);
        folder.countRemaining = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_REMAINING.name);
        folder.hasChildren = dbHelper.getBoolean(cursor, Entry.COLUMN_HAS_CHILDREN.name);
        folder.name = dbHelper.getString(cursor, Entry.COLUMN_NAME.name);
        folder.parentID = dbHelper.getString(cursor, Entry.COLUMN_PARENT_ID.name);
        folder.rank = dbHelper.getLong(cursor, Entry.COLUMN_RANK.name);

        return folder;
    }

    public List<Task> getTasks(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
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

    public List<Task> getTasks(String parentID) {

        return getTasks(Tasks.COLUMN_PARENT_ID + " = ?", new String[]{parentID});
    }

    public List<Task> getTasks() {

        return getTasks(null, null);
    }

    public List<Task> getTasksInInbox() {

        return getTasks(Tasks.COLUMN_INBOX + " = 1 AND " + Tasks.COLUMN_DATE_COMPLETED + " IS NULL",
                        null);
    }

    public List<Task> getTopLevelProjects() {

        String selection = Tasks.COLUMN_PROJECT + " = 1 AND " + Tasks.COLUMN_DATE_COMPLETED + " " +
                           "IS NULL AND " + Tasks.COLUMN_PARENT_ID + " IS NULL AND " +
                           Tasks.COLUMN_PROJECT_STATUS + " = 'active' ORDER BY " +
                           Tasks.COLUMN_RANK;
        return getTasks(selection, null);
    }

    public List<Task> getFlagged() {

        String selection =
                "(" + Tasks.COLUMN_FLAGGED + " = 1 OR " + Tasks.COLUMN_FLAGGED_EFFECTIVE +
                " = 1) AND " + Tasks.COLUMN_DATE_COMPLETED + " IS " + "NULL AND " +
                Tasks.COLUMN_BLOCKED + " = 0 AND " + Tasks.COLUMN_BLOCKED_BY_DEFER.name + " = 0 " +
                "AND " + Tasks.COLUMN_INBOX + " = 0";
        return getTasks(selection, null);
    }

    private Task getTaskFromCursor(Cursor cursor) {

        Task task = new Task();

        // Base methods
        task.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        task.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        task.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entry methods
        task.countAvailable = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_AVAILABLE.name);
        task.countChildren = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_CHILDREN.name);
        task.countCompleted = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_COMPLETED.name);
        task.countDueSoon = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_DUE_SOON.name);
        task.countOverdue = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_OVERDUE.name);
        task.countRemaining = dbHelper.getInt(cursor, Entry.COLUMN_COUNT_REMAINING.name);
        task.hasChildren = dbHelper.getBoolean(cursor, Entry.COLUMN_HAS_CHILDREN.name);
        task.name = dbHelper.getString(cursor, Entry.COLUMN_NAME.name);
        task.parentID = dbHelper.getString(cursor, Entry.COLUMN_PARENT_ID.name);
        task.rank = dbHelper.getLong(cursor, Entry.COLUMN_RANK.name);

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

    public Map<String, String> getContextNameMap() {

        SQLiteDatabase db      = dbHelper.getWritableDatabase();
        String[]       columns = {Contexts.COLUMN_ID.name, Contexts.COLUMN_NAME.name};

        Cursor cursor = dbHelper.query(db, Contexts.TABLE_NAME, columns, null, null);

        Map<String, String> result = new HashMap<>();
        while (cursor.moveToNext()) {
            String id   = dbHelper.getString(cursor, Base.COLUMN_ID.name);
            String name = dbHelper.getString(cursor, Entry.COLUMN_NAME.name);
            result.put(id, name);
        }

        cursor.close();
        db.close();
        return result;
    }
}
