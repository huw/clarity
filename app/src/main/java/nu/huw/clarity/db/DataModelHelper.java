package nu.huw.clarity.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Contexts;
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
     * Given a context parent, get all direct children of that context.
     */
    public List<Context> getContexts(String parentID) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = parentID.isEmpty() ? Contexts.COLUMN_PARENT_ID + " IS NULL" :
                           Contexts.COLUMN_PARENT_ID + " = ?";
        String[] selectionArgs = {parentID};

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
     * Shorthand for empty argument, will get all top-level contexts
     */
    public List<Context> getContexts() {

        return getContexts("");
    }

    private Context getContextFromCursor(Cursor cursor) {

        Context context = new Context();

        // Base methods
        context.id = dbHelper.getString(cursor, Contexts.COLUMN_ID.name);
        context.added = dbHelper.getDate(cursor, Contexts.COLUMN_DATE_ADDED.name);
        context.modified = dbHelper.getDate(cursor, Contexts.COLUMN_DATE_MODIFIED.name);

        // Entry methods
        context.active = dbHelper.getBoolean(cursor, Contexts.COLUMN_ACTIVE.name);
        context.activeEffective =
                dbHelper.getBoolean(cursor, Contexts.COLUMN_ACTIVE_EFFECTIVE.name);
        context.countAvailable = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_AVAILABLE.name);
        context.countChildren = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_CHILDREN.name);
        context.countCompleted = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_COMPLETED.name);
        context.countDueSoon = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_DUE_SOON.name);
        context.countOverdue = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_OVERDUE.name);
        context.countRemaining = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_REMAINING.name);
        context.hasChildren = dbHelper.getBoolean(cursor, Contexts.COLUMN_HAS_CHILDREN.name);
        context.name = dbHelper.getString(cursor, Contexts.COLUMN_NAME.name);
        context.parentID = dbHelper.getString(cursor, Contexts.COLUMN_PARENT_ID.name);
        context.rank = dbHelper.getLong(cursor, Contexts.COLUMN_RANK.name);

        // Context methods
        context.altitude = dbHelper.getLong(cursor, Contexts.COLUMN_ALTITUDE.name);
        context.latitude = dbHelper.getLong(cursor, Contexts.COLUMN_LATITUDE.name);
        context.locationName = dbHelper.getString(cursor, Contexts.COLUMN_LOCATION_NAME.name);
        context.longitude = dbHelper.getLong(cursor, Contexts.COLUMN_LONGITUDE.name);
        context.onHold = dbHelper.getBoolean(cursor, Contexts.COLUMN_ON_HOLD.name);
        context.radius = dbHelper.getLong(cursor, Contexts.COLUMN_RADIUS.name);

        return context;
    }

    public List<Folder> getFolders(String parentID) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = parentID.isEmpty() ? Folders.COLUMN_PARENT_ID + " IS NULL" :
                           Folders.COLUMN_PARENT_ID + " = ?";
        String[] selectionArgs = {parentID};

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

    public List<Folder> getFolders() {

        return getFolders("");
    }

    private Folder getFolderFromCursor(Cursor cursor) {

        Folder folder = new Folder();

        // Base methods
        folder.id = dbHelper.getString(cursor, Contexts.COLUMN_ID.name);
        folder.added = dbHelper.getDate(cursor, Contexts.COLUMN_DATE_ADDED.name);
        folder.modified = dbHelper.getDate(cursor, Contexts.COLUMN_DATE_MODIFIED.name);

        // Entry methods
        folder.active = dbHelper.getBoolean(cursor, Contexts.COLUMN_ACTIVE.name);
        folder.activeEffective = dbHelper.getBoolean(cursor, Contexts.COLUMN_ACTIVE_EFFECTIVE.name);
        folder.countAvailable = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_AVAILABLE.name);
        folder.countChildren = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_CHILDREN.name);
        folder.countCompleted = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_COMPLETED.name);
        folder.countDueSoon = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_DUE_SOON.name);
        folder.countOverdue = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_OVERDUE.name);
        folder.countRemaining = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_REMAINING.name);
        folder.hasChildren = dbHelper.getBoolean(cursor, Contexts.COLUMN_HAS_CHILDREN.name);
        folder.name = dbHelper.getString(cursor, Contexts.COLUMN_NAME.name);
        folder.parentID = dbHelper.getString(cursor, Contexts.COLUMN_PARENT_ID.name);
        folder.rank = dbHelper.getLong(cursor, Contexts.COLUMN_RANK.name);

        return folder;
    }

    public List<Task> getTasks(String parentID) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = parentID.isEmpty() ? Tasks.COLUMN_PARENT_ID + " IS NULL" :
                           Tasks.COLUMN_PARENT_ID + " = ?";
        String[] selectionArgs = {parentID};

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

    public List<Task> getTasks() {

        return getTasks("");
    }

    private Task getTaskFromCursor(Cursor cursor) {

        Task task = new Task();

        // Base methods
        task.id = dbHelper.getString(cursor, Contexts.COLUMN_ID.name);
        task.added = dbHelper.getDate(cursor, Contexts.COLUMN_DATE_ADDED.name);
        task.modified = dbHelper.getDate(cursor, Contexts.COLUMN_DATE_MODIFIED.name);

        // Entry methods
        task.active = dbHelper.getBoolean(cursor, Contexts.COLUMN_ACTIVE.name);
        task.activeEffective = dbHelper.getBoolean(cursor, Contexts.COLUMN_ACTIVE_EFFECTIVE.name);
        task.countAvailable = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_AVAILABLE.name);
        task.countChildren = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_CHILDREN.name);
        task.countCompleted = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_COMPLETED.name);
        task.countDueSoon = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_DUE_SOON.name);
        task.countOverdue = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_OVERDUE.name);
        task.countRemaining = dbHelper.getInt(cursor, Contexts.COLUMN_COUNT_REMAINING.name);
        task.hasChildren = dbHelper.getBoolean(cursor, Contexts.COLUMN_HAS_CHILDREN.name);
        task.name = dbHelper.getString(cursor, Contexts.COLUMN_NAME.name);
        task.parentID = dbHelper.getString(cursor, Contexts.COLUMN_PARENT_ID.name);
        task.rank = dbHelper.getLong(cursor, Contexts.COLUMN_RANK.name);

        // Task methods
        task.blocked = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED.name);
        task.blockedByDefer = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED_BY_DEFER.name);
        task.completeWithChildren =
                dbHelper.getBoolean(cursor, Tasks.COLUMN_COMPLETE_WITH_CHILDREN.name);
        task.context = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
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
        task.next = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.notePlaintext = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.noteXML = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.overdue = dbHelper.getBoolean(cursor, Tasks.COLUMN_OVERDUE.name);
        task.project = dbHelper.getBoolean(cursor, Tasks.COLUMN_PROJECT.name);
        task.projectID = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.lastReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_LAST_REVIEW.name);
        task.nextReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_NEXT_REVIEW.name);
        task.reviewInterval = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_REPEAT_REVIEW.name);
        task.status = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.repetitionMethod = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.repetitionRule = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);
        task.type = dbHelper.getString(cursor, Tasks.COLUMN_ID.name);

        return task;
    }
}
