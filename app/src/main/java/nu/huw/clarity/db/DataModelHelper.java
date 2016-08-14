package nu.huw.clarity.db;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Entries;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;

/**
 * Like DatabaseHelper, DataModelHelper contains a number of helper classes for interacting with
 * the database, but only those specific to interacting with the data model (for views). Getters
 * and setters are all welcome.
 */
public class DataModelHelper {

    private static final String TAG           = DataModelHelper.class.getSimpleName();
    // Entries selections
    private static       String AND           = " AND ";
    private static       String ORDER_BY_RANK = " ORDER BY " + DatabaseContract.Entries.COLUMN_RANK;
    private static       String TOP_LEVEL     = Entries.COLUMN_PARENT_ID + " IS NULL";
    private static       String ACTIVE        =
            DatabaseContract.Entries.COLUMN_ACTIVE + " = 1 AND " + Entries.COLUMN_ACTIVE_EFFECTIVE +
            " = 1";
    // Task selections
    private static       String REMAINING     = Tasks.COLUMN_DATE_COMPLETED + " IS NULL";
    private static       String IN_INBOX      = Tasks.COLUMN_INBOX + " = 1";
    private static       String IS_PROJECT    = Tasks.COLUMN_PROJECT + " = 1";
    private static       String AVAILABLE     = REMAINING + AND + Tasks.COLUMN_BLOCKED + " = 0" +
                                                AND + Tasks.COLUMN_BLOCKED_BY_DEFER.name + " = 0";
    private static       String NO_CONTEXT    = Tasks.COLUMN_CONTEXT + " IS NULL";
    private static       String DUE_SOON      = Tasks.COLUMN_DUE_SOON + " = 1";
    private static       String OVERDUE       = Tasks.COLUMN_OVERDUE + " = 1";
    private DatabaseHelper          dbHelper;
    private android.content.Context mContext;

    public DataModelHelper(android.content.Context context) {

        dbHelper = new DatabaseHelper();
        mContext = context;
    }

    /**
     * Given the ID of any entry, get it into the data model
     */
    public Entry getEntryFromID(String id, String tableName) {

        switch (tableName) {
            case Contexts.TABLE_NAME:
                return getContext(id);
            case Folders.TABLE_NAME:
                return getFolder(id);
            case Tasks.TABLE_NAME:
                return getTask(id);
        }

        return null;
    }

    /**
     * Given the ID of an attachment, get it into the data model
     */
    public Attachment getAttachment(String id) {

        SQLiteDatabase db            = dbHelper.getReadableDatabase();
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
     * Just gets the name of a context, given its ID
     */
    public String getContextName(String id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Contexts.TABLE_NAME, new String[]{Contexts.COLUMN_NAME.name},
                               Contexts.COLUMN_ID + " = ?", new String[]{id});

        cursor.moveToFirst();
        String name = dbHelper.getString(cursor, Contexts.COLUMN_NAME.name);

        cursor.close();
        db.close();

        return name;
    }

    /**
     * Gets all contexts given a selection string and argument. Usually should be accessed by
     * other methods.
     */
    public List<Entry> getContexts(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Contexts.TABLE_NAME, Contexts.columns, selection, selectionArgs);

        List<Entry> result = new ArrayList<>();
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
    public List<Entry> getContexts() {

        return getContexts(null, null);
    }

    /**
     * Given a context ID, get all child contexts
     */
    public List<Entry> getContexts(String parentID) {

        return getContexts(ACTIVE + AND + Contexts.COLUMN_PARENT_ID + " = ?",
                           new String[]{parentID});
    }

    public Entry getHeaderContext(String parentID) {

        Entry context = getContexts(Contexts.COLUMN_ID + " = ?", new String[]{parentID}).get(0);
        context.headerRow = true;
        return context;
    }

    public List<Entry> getContextChildren(String parentID) {

        List<Entry> result = new ArrayList<>();

        result.add(getHeaderContext(parentID));

        result.addAll(getContexts(parentID));
        result.addAll(getTasksWithContext(parentID));

        return result;
    }

    public List<Entry> getTopLevelContexts() {

        List<Entry> result = new ArrayList<>();

        String selection = TOP_LEVEL + AND + ACTIVE;
        result.add(getNoContext());
        result.addAll(getContexts(selection, null));

        return result;
    }

    public Context getNoContext() {

        SQLiteDatabase db        = dbHelper.getReadableDatabase();
        Context        noContext = new Context();

        noContext.name = mContext.getString(R.string.no_context);
        noContext.countAvailable = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + AVAILABLE);
        noContext.countDueSoon = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + DUE_SOON);
        noContext.countOverdue = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + OVERDUE);
        noContext.id = "NO_CONTEXT";

        db.close();
        return noContext;
    }

    public Entry getContext(String id) {

        return getContexts(Contexts.COLUMN_ID + " = ?", new String[]{id}).get(0);
    }

    private Context getContextFromCursor(Cursor cursor) {

        Context context = new Context();

        // Base methods
        context.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        context.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        context.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        context.active = dbHelper.getBoolean(cursor, DatabaseContract.Entries.COLUMN_ACTIVE.name);
        context.activeEffective =
                dbHelper.getBoolean(cursor, DatabaseContract.Entries.COLUMN_ACTIVE_EFFECTIVE.name);
        context.countAvailable =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_AVAILABLE.name);
        context.countChildren =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_CHILDREN.name);
        context.countCompleted =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_COMPLETED.name);
        context.countDueSoon =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_DUE_SOON.name);
        context.countOverdue =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_OVERDUE.name);
        context.countRemaining =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_REMAINING.name);
        context.hasChildren =
                dbHelper.getBoolean(cursor, DatabaseContract.Entries.COLUMN_HAS_CHILDREN.name);
        context.name = dbHelper.getString(cursor, DatabaseContract.Entries.COLUMN_NAME.name);
        context.parentID =
                dbHelper.getString(cursor, DatabaseContract.Entries.COLUMN_PARENT_ID.name);
        context.rank = dbHelper.getLong(cursor, DatabaseContract.Entries.COLUMN_RANK.name);

        // Context methods
        context.altitude = dbHelper.getLong(cursor, Contexts.COLUMN_ALTITUDE.name);
        context.latitude = dbHelper.getLong(cursor, Contexts.COLUMN_LATITUDE.name);
        context.locationName = dbHelper.getString(cursor, Contexts.COLUMN_LOCATION_NAME.name);
        context.longitude = dbHelper.getLong(cursor, Contexts.COLUMN_LONGITUDE.name);
        context.onHold = dbHelper.getBoolean(cursor, Contexts.COLUMN_ON_HOLD.name);
        context.radius = dbHelper.getLong(cursor, Contexts.COLUMN_RADIUS.name);

        return context;
    }

    public List<Entry> getFolders(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Folders.TABLE_NAME, Folders.columns, selection, selectionArgs);

        List<Entry> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getFolderFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<Entry> getFolders(String parentID) {

        return getFolders(Folders.COLUMN_PARENT_ID + " = ?", new String[]{parentID});
    }

    public List<Entry> getFolders() {

        return getFolders(null, null);
    }

    public List<Entry> getTopLevelFolders() {

        return getFolders(TOP_LEVEL + ORDER_BY_RANK, null);
    }

    public Entry getFolder(String id) {

        return getFolders(Folders.COLUMN_ID + " = ?", new String[]{id}).get(0);
    }

    private Folder getFolderFromCursor(Cursor cursor) {

        Folder folder = new Folder();

        // Base methods
        folder.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        folder.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        folder.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        folder.active = dbHelper.getBoolean(cursor, DatabaseContract.Entries.COLUMN_ACTIVE.name);
        folder.activeEffective =
                dbHelper.getBoolean(cursor, DatabaseContract.Entries.COLUMN_ACTIVE_EFFECTIVE.name);
        folder.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE.name);
        folder.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN.name);
        folder.countCompleted =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_COMPLETED.name);
        folder.countDueSoon =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_DUE_SOON.name);
        folder.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        folder.countRemaining = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_REMAINING.name);
        folder.hasChildren = dbHelper.getBoolean(cursor, Entries.COLUMN_HAS_CHILDREN.name);
        folder.name = dbHelper.getString(cursor, Entries.COLUMN_NAME.name);
        folder.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID.name);
        folder.rank = dbHelper.getLong(cursor, DatabaseContract.Entries.COLUMN_RANK.name);

        return folder;
    }

    /**
     * Get project name, given id
     */
    public String getProjectName(String id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.query(db, Tasks.TABLE_NAME, new String[]{Tasks.COLUMN_NAME.name},
                                       Tasks.COLUMN_ID + " = ?", new String[]{id});

        cursor.moveToFirst();
        String name = dbHelper.getString(cursor, Tasks.COLUMN_NAME.name);

        cursor.close();
        db.close();

        return name;
    }

    public List<Entry> getTasks(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Tasks.TABLE_NAME, Tasks.columns, selection, selectionArgs);

        List<Entry> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getTaskFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<Entry> getTasks(String parentID) {

        String selection = REMAINING + AND + Entries.COLUMN_PARENT_ID + " = ?" + ORDER_BY_RANK;
        return getTasks(selection, new String[]{parentID});
    }

    public List<Entry> getTasks() {

        String selection = REMAINING + AND + Tasks.COLUMN_DATE_DUE + " IS NOT NULL ORDER BY " +
                           Tasks.COLUMN_DATE_DUE;
        return getTasks(selection, null);
    }

    public List<Entry> getTasksInInbox() {

        return getTasks(IN_INBOX + AND + REMAINING + ORDER_BY_RANK, null);
    }

    public Entry getHeaderTask(String parentID) {

        String      selection = Tasks.COLUMN_ID + " = ?";
        String[]    args      = new String[]{parentID};
        List<Entry> tasks     = getTasks(selection, args);

        Entry entry;
        if (!tasks.isEmpty()) {
            entry = tasks.get(0);
        } else {
            entry = getFolders(selection, args).get(0);
        }

        entry.headerRow = true;
        return entry;
    }

    public List<Entry> getChildren(String parentID) {

        List<Entry> entries = new ArrayList<>();

        entries.addAll(getTasks(parentID));
        entries.addAll(getFolders(parentID));

        Collections.sort(entries);

        entries.add(0, getHeaderTask(parentID));

        return entries;
    }

    public List<Entry> getTopLevelProjects() {

        // (and folders)

        List<Entry> topLevel = new ArrayList<>();

        String selection = IS_PROJECT + AND + REMAINING + AND + TOP_LEVEL + AND +
                           Tasks.COLUMN_PROJECT_STATUS + " = 'active'" + ORDER_BY_RANK;
        topLevel.addAll(getTasks(selection, null));
        topLevel.addAll(getTopLevelFolders());

        Collections.sort(topLevel);

        return topLevel;
    }

    public List<Entry> getFlagged() {

        String selection =
                "(" + Tasks.COLUMN_FLAGGED + " = 1 OR " + Tasks.COLUMN_FLAGGED_EFFECTIVE +
                " = 1)" + AND + AVAILABLE + AND + Tasks.COLUMN_INBOX + " = 0" +
                ORDER_BY_RANK;
        return getTasks(selection, null);
    }

    public List<Entry> getTasksWithContext(String contextID) {

        String selection = REMAINING + AND + Tasks.COLUMN_CONTEXT + " = ?" + ORDER_BY_RANK;
        return getTasks(selection, new String[]{contextID});
    }

    public List<Entry> getTasksWithNoContext() {

        String selection = REMAINING + AND + Tasks.COLUMN_CONTEXT + " IS NULL" + ORDER_BY_RANK;
        return getTasks(selection, null);
    }

    public Entry getTask(String id) {

        return getTasks(Tasks.COLUMN_ID + " = ?", new String[]{id}).get(0);
    }

    private Task getTaskFromCursor(Cursor cursor) {

        Task task = new Task();

        // Base methods
        task.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        task.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        task.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        task.countAvailable =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_AVAILABLE.name);
        task.countChildren =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_CHILDREN.name);
        task.countCompleted =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_COMPLETED.name);
        task.countDueSoon =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_DUE_SOON.name);
        task.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        task.countRemaining =
                dbHelper.getInt(cursor, DatabaseContract.Entries.COLUMN_COUNT_REMAINING.name);
        task.hasChildren =
                dbHelper.getBoolean(cursor, DatabaseContract.Entries.COLUMN_HAS_CHILDREN.name);
        task.name = dbHelper.getString(cursor, DatabaseContract.Entries.COLUMN_NAME.name);
        task.parentID = dbHelper.getString(cursor, DatabaseContract.Entries.COLUMN_PARENT_ID.name);
        task.rank = dbHelper.getLong(cursor, DatabaseContract.Entries.COLUMN_RANK.name);

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

        // Project/Context names
        if (task.context != null) {
            task.contextName = getContextName(task.context);
        }
        if (task.projectID != null) {
            task.projectName = getProjectName(task.projectID);
        }

        return task;
    }
}
