package nu.huw.clarity.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

import nu.huw.clarity.ui.MainActivity;
import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Perspectives;
import nu.huw.clarity.db.DatabaseContract.SQLKeyValue;
import nu.huw.clarity.db.DatabaseContract.Settings;
import nu.huw.clarity.db.DatabaseContract.Tasks;

/**
 * A set of helper methods for dealing with the SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "OmniFocus";

    public static String SQL_CREATE_ATTACHMENTS;
    public static String SQL_CREATE_CONTEXTS;
    public static String SQL_CREATE_FOLDERS;
    public static String SQL_CREATE_PERSPECTIVES;
    public static String SQL_CREATE_SETTINGS;
    public static String SQL_CREATE_TASKS;

    public DatabaseHelper() {
        super(MainActivity.context, DATABASE_NAME, null, DATABASE_VERSION);

        /**
         * For each table, generate a CREATE TABLE statement from the values
         * we have in the table.keys field. At the end, remove the final comma
         * with a call to substring, and move on.
         */
        SQL_CREATE_ATTACHMENTS = "CREATE TABLE " + Attachments.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Attachments.keys) {
            SQL_CREATE_ATTACHMENTS += keyValue;
            if (keyValue.name.equals("id")) { SQL_CREATE_ATTACHMENTS += " PRIMARY KEY"; }
            if (keyValue.val != null) { SQL_CREATE_ATTACHMENTS += " DEFAULT " + keyValue.val; }
            SQL_CREATE_ATTACHMENTS += ",";
        }
        SQL_CREATE_ATTACHMENTS = SQL_CREATE_ATTACHMENTS.substring(0,
                SQL_CREATE_ATTACHMENTS.length() - 1) + ")";

        SQL_CREATE_CONTEXTS = "CREATE TABLE " + Contexts.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Contexts.keys) {
            SQL_CREATE_CONTEXTS += keyValue;
            if (keyValue.name.equals("id")) { SQL_CREATE_CONTEXTS += " PRIMARY KEY"; }
            if (keyValue.val != null) { SQL_CREATE_CONTEXTS += " DEFAULT " + keyValue.val; }
            SQL_CREATE_CONTEXTS += ",";
        }
        SQL_CREATE_CONTEXTS = SQL_CREATE_CONTEXTS.substring(0, 
                SQL_CREATE_CONTEXTS.length() - 1) + ")";

        SQL_CREATE_FOLDERS = "CREATE TABLE " + Folders.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Folders.keys) {
            SQL_CREATE_FOLDERS += keyValue;
            if (keyValue.name.equals("id")) { SQL_CREATE_FOLDERS += " PRIMARY KEY"; }
            if (keyValue.val != null) { SQL_CREATE_FOLDERS += " DEFAULT " + keyValue.val; }
            SQL_CREATE_FOLDERS += ",";
        }
        SQL_CREATE_FOLDERS = SQL_CREATE_FOLDERS.substring(0, SQL_CREATE_FOLDERS.length() - 1) + ")";

        SQL_CREATE_PERSPECTIVES = "CREATE TABLE " + Perspectives.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Perspectives.keys) {
            SQL_CREATE_PERSPECTIVES += keyValue;
            if (keyValue.name.equals("id")) { SQL_CREATE_PERSPECTIVES += " PRIMARY KEY"; }
            if (keyValue.val != null) { SQL_CREATE_PERSPECTIVES += " DEFAULT " + keyValue.val; }
            SQL_CREATE_PERSPECTIVES += ",";
        }
        SQL_CREATE_PERSPECTIVES = SQL_CREATE_PERSPECTIVES.substring(0,
                SQL_CREATE_PERSPECTIVES.length() - 1) + ")";

        SQL_CREATE_SETTINGS = "CREATE TABLE " + Settings.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Settings.keys) {
            SQL_CREATE_SETTINGS += keyValue;
            if (keyValue.name.equals("id")) { SQL_CREATE_SETTINGS += " PRIMARY KEY"; }
            if (keyValue.val != null) { SQL_CREATE_SETTINGS += " DEFAULT " + keyValue.val; }
            SQL_CREATE_SETTINGS += ",";
        }
        SQL_CREATE_SETTINGS = SQL_CREATE_SETTINGS.substring(0,
                SQL_CREATE_SETTINGS.length() - 1) + ")";

        SQL_CREATE_TASKS = "CREATE TABLE " + Tasks.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Tasks.keys) {
            SQL_CREATE_TASKS += keyValue;
            if (keyValue.name.equals("id")) { SQL_CREATE_TASKS += " PRIMARY KEY"; }
            if (keyValue.val != null) { SQL_CREATE_TASKS += " DEFAULT " + keyValue.val; }
            SQL_CREATE_TASKS += ",";
        }
        SQL_CREATE_TASKS = SQL_CREATE_TASKS.substring(0, SQL_CREATE_TASKS.length() - 1) + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ATTACHMENTS);
        db.execSQL(SQL_CREATE_CONTEXTS);
        db.execSQL(SQL_CREATE_FOLDERS);
        db.execSQL(SQL_CREATE_PERSPECTIVES);
        db.execSQL(SQL_CREATE_SETTINGS);
        db.execSQL(SQL_CREATE_TASKS);
        Log.i(TAG, "Database successfully created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Attachments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contexts.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Folders.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Perspectives.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Settings.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tasks.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Given important data, insert a new row into the database with
     * the given rowID. The values should have keys that correspond
     * to columns in the database, and appropriate values, but this
     * isn't validated because that would be wasteful. However, we
     * check to see if there are any values to be added first.
     */
    public void insert(String tableName, String rowID, ContentValues values) {

        SQLiteDatabase db = this.getWritableDatabase();

        if (values.size() != 0) {

            // null means insert
            values.put(Base.COLUMN_ID.name, rowID);

            try {
                db.insertOrThrow(tableName, null, values);

                Log.v(TAG, rowID + " added to " + tableName);
            } catch (SQLiteConstraintException e) {

                // If there's an error because we already have this key,
                // then just update it! Yay!

                Log.v(TAG, rowID + " already exists, updating");
                update(tableName, rowID, values);
            }
        }

        db.close();
    }

    /**
     * Update the database
     */
    public void update(String tableName, String rowID, ContentValues values) {

        SQLiteDatabase db = this.getWritableDatabase();

        if (values.size() != 0) {

            // The 'selection' parameter determines which rows to add the values
            // to. It creates a 'SELECT' statement, and adds a 'WHERE' before our
            // addition. Then it replaces all '?'s with your arguments in the
            // order that they appear.

            String selection = Base.COLUMN_ID.name + "=?";
            String[] selectionArgs = {rowID};
            db.update(tableName, values, selection, selectionArgs);

            Log.v(TAG, rowID + " updated in " + tableName);
        }

        db.close();
    }

    /**
     * Delete the entry with the given ID. No need for any ContentValues.
     */
    public void delete(String tableName, String rowID) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = Base.COLUMN_ID.name + "=?";
        String[] selectionArgs = { rowID };
        db.delete(tableName, selection, selectionArgs);

        Log.v(TAG, rowID + " deleted from " + tableName);

        db.close();
    }

    /**
     * Just a nice shorthand for getting strings from the database cursor,
     * but also with some bonus null checking.
     */
    public String getString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1 && cursor.getColumnCount() != 0) {
            return cursor.getString(index);
        }
        return null;
    }

    /**
     * Shorthands for querying without the stuff with the normal methods want.
     */
    public Cursor query(SQLiteDatabase db, String tableName, String[] columns, String selection) {
        return db.query(tableName, columns, selection, null, null, null, null);
    }

    public Cursor query(SQLiteDatabase db, String tableName, String[] columns, String selection,
                        String[] selectionArgs) {
        return db.query(tableName, columns, selection, selectionArgs, null, null, null);
    }

    /**
     * Build the tree into the database, and any other cell data that is
     * built after a sync instead of handed down with the rest of the data.
     *
     * Order of operations:
     *  1. Recursively move down the tree from each project, updating
     *     children with defer/due dates and flags in 'dateDueEffective'
     *     and the like.
     *     1.1 Run through each child, and calculate its due soon/overdue
     *         columns using the new data.
     *  2. Each parent gets the number of children for each count.
     *  3. Each child determines if it's blocked by checking its parent's
     *     'nextTask' column.
     *  4. Each child updates its parent's and context's 'available' column.
     */
    public void updateTree() {

        SQLiteDatabase db = this.getWritableDatabase();

        /**
         * Step 1: Recursively update children of projects
         */
        Cursor projects = query(
                db,
                Tasks.TABLE_NAME,
                new String[] {
                        Tasks.COLUMN_ID.name,
                        Tasks.COLUMN_DATE_DEFER.name,
                        Tasks.COLUMN_DATE_DUE.name,
                        Tasks.COLUMN_FLAGGED.name
                },
                Tasks.COLUMN_PROJECT.name + "='1'"
        );

        // The Cursor object is used to lazy-load SQLite databases, which
        // is important when we're dealing with lots and lots of data (not
        // so much applicable in this case, but you never know).
        //
        // Basically, it's been designed so that its positioning functions
        // (moveToNext, moveToFirst, etc) return booleans for whether they
        // were successful or not. Here, we call `moveToNext()`, and if it
        // returns `true`, then this task has a child we can iterate over.
        //
        // On this first call to `updateChildren()`, we set off the huge
        // recursive update. See the method itself for more details.

        while (projects.moveToNext()) {

            String id = getString(projects, Tasks.COLUMN_ID.name);
            String deferDate = getString(projects, Tasks.COLUMN_DATE_DEFER.name);
            String dueDate = getString(projects, Tasks.COLUMN_DATE_DUE.name);
            String flagged = getString(projects, Tasks.COLUMN_FLAGGED.name);

            updateChildren(db, id, deferDate, dueDate, flagged);
        }

        projects.close();
        Log.i(TAG, "Effective defer dates, due dates, flags, due soons and overdues updated");

        /**
         * Step 2: Update counts on parents
         */

        Cursor allParents = query(
                db,
                Tasks.TABLE_NAME,
                new String[] {
                        Tasks.COLUMN_ID.name
                },

                // This SQL statement will get all parents.
                // How? It finds all entries where their ID is found in a child's
                // PARENT_ID column. That's some goddamn SQL magic, son.

                Tasks.COLUMN_ID.name + " IN (SELECT " + Tasks.COLUMN_PARENT_ID.name + " FROM " +
                        Tasks.TABLE_NAME + ")"
        );

        while (allParents.moveToNext()) {
            String id = getString(allParents, Tasks.COLUMN_ID.name);

            // Gets all remaining items ordered by rank. This was going to
            // be a full list of children, but I realised I could re-use
            // this cursor for the 'next' parameter. So it's kinda cool.

            Cursor remaining = db.query(
                    Tasks.TABLE_NAME,
                    null,
                    Tasks.COLUMN_PARENT_ID.name + "=? AND " +
                    Tasks.COLUMN_DATE_COMPLETED + " IS NULL",
                    new String[]{id},
                    null,
                    null,
                    Tasks.COLUMN_RANK.name + " ASC"
            );

            // This bit gets the first child, when the list of children is
            // ordered by the rank. That child will be the 'next' task to

            String next = "";
            if (remaining.moveToFirst()) {
                next = getString(remaining, Tasks.COLUMN_ID.name);
            }

            // These three just get row counts straight from the query

            int countCompleted = query(db, Tasks.TABLE_NAME, null,
                    Tasks.COLUMN_PARENT_ID.name + "=? AND " +
                    Tasks.COLUMN_DATE_COMPLETED.name + " IS NOT NULL",
                    new String[]{id}).getCount();

            int countDueSoon = query(db, Tasks.TABLE_NAME, null,
                    Tasks.COLUMN_PARENT_ID.name + "=? AND " +
                    Tasks.COLUMN_DUE_SOON.name + "=1",
                    new String[]{id}).getCount();

            int countOverdue = query(db, Tasks.TABLE_NAME, null,
                    Tasks.COLUMN_PARENT_ID.name + "=? AND " +
                    Tasks.COLUMN_OVERDUE.name + "=1 AND " +

                    // Only get an overdue count for tasks that aren't complete

                    Tasks.COLUMN_DATE_COMPLETED.name + " IS NULL",
                    new String[]{id}).getCount();

            // The child count is always the number remaining (not completed)
            // plus the number of completed. So just build it here.

            int countRemaining = remaining.getCount();
            int countChildren = countRemaining + countCompleted;

            ContentValues values = new ContentValues();
            values.put(Tasks.COLUMN_COUNT_CHILDREN.name, countChildren);
            values.put(Tasks.COLUMN_COUNT_COMPLETED.name, countCompleted);
            values.put(Tasks.COLUMN_COUNT_DUE_SOON.name, countDueSoon);
            values.put(Tasks.COLUMN_COUNT_OVERDUE.name, countOverdue);
            values.put(Tasks.COLUMN_COUNT_REMAINING.name, countRemaining);
            values.put(Tasks.COLUMN_HAS_CHILDREN.name, 1);
            values.put(Tasks.COLUMN_NEXT.name, next);

            db.update(
                    Tasks.TABLE_NAME,
                    values,
                    Tasks.COLUMN_ID + "=?",
                    new String[] { id }
            );
        }
        allParents.close();

        Log.i(TAG, "All parent counts (except blocked) updated");

        /**
         * Step 3
         * TODO: This
         */

        db.close();
    }

    /**
     * Given a database and some other info, find the task's children (if any),
     * and update them with their parent's dateDue and dateDefer. We iterate
     * over any element which lists the task's ID as its parent, and fill in
     * the parent's data in their 'effective' fields.
     *
     * Once this is done, we check to see if the child has any of the same info
     * to pass down to its descendants. If there are any, we update the row.
     *
     * Then, finally, we recursively call the function again on each of these
     * children, until we've eventually descended the tree.
     */
    private void updateChildren(SQLiteDatabase db, String id, String dateDefer, String dateDue,
                                String flagged) {

        Cursor children = query(
                db,
                Tasks.TABLE_NAME,
                new String[]{
                        Tasks.COLUMN_ID.name,
                        Tasks.COLUMN_DATE_DEFER.name,
                        Tasks.COLUMN_DATE_DUE.name,
                        Tasks.COLUMN_FLAGGED.name
                },
                Tasks.COLUMN_PARENT_ID.name + "=?",
                new String[]{ id }
        );

        while (children.moveToNext()) {

            String childID = getString(children, Tasks.COLUMN_ID.name);
            String childDateDefer = getString(children, Tasks.COLUMN_DATE_DEFER.name);
            String childDateDue = getString(children, Tasks.COLUMN_DATE_DUE.name);
            String childFlagged = getString(children, Tasks.COLUMN_FLAGGED.name);

            ContentValues values = new ContentValues();

            values.put(Tasks.COLUMN_DATE_DEFER_EFFECTIVE.name, dateDefer);
            values.put(Tasks.COLUMN_FLAGGED_EFFECTIVE.name, flagged);
            values.put(Tasks.COLUMN_DATE_DUE_EFFECTIVE.name, dateDue);

            if (values.size() > 0) {
                db.update(
                        Tasks.TABLE_NAME,
                        values,
                        Tasks.COLUMN_ID.name + "=?",
                        new String[]{childID}
                );
            }

            if (childDateDefer != null) { dateDefer = childDateDefer; }
            if (childDateDue != null) { dateDue = childDateDue; }
            if (childFlagged.equals("1")) { flagged = childFlagged; }

            updateChildren(db, childID, dateDefer, dateDue, flagged);
        }

        children.close();

        /**
         * Step 1.5: Overdue/due soon
         */
        Cursor dueDates = query(
                db,
                Tasks.TABLE_NAME,
                new String[] {
                        Tasks.COLUMN_DATE_DUE.name,
                        Tasks.COLUMN_DATE_DUE_EFFECTIVE.name
                },
                Tasks.COLUMN_ID.name + "=?",
                new String[] { id }
        );

        dueDates.moveToNext();

        String normalDate = getString(dueDates, Tasks.COLUMN_DATE_DUE.name);
        String effectiveDate = getString(dueDates, Tasks.COLUMN_DATE_DUE_EFFECTIVE.name);

        if (normalDate != null || effectiveDate != null) {
            Date dueDate = new Date();
            Date today = new Date();
            ContentValues values = new ContentValues();

            // If we don't have a normal due date, then we default back on the
            // effective due date. That one becomes the one to compare with the
            // the current date to determine Due Soon and Overdue. Look up the
            // ternary operator if this is confusing.

            dueDate.setTime(Long.valueOf(normalDate == null ? effectiveDate : normalDate));

            if (today.after(dueDate)) {
                values.put(Tasks.COLUMN_OVERDUE.name, true);
            } else {

                // Add two days to the current time, and if the due date is now
                // before that, AND not overdue, then it's due soon, right?

                long ONE_DAY = 86400000;
                today.setTime(today.getTime() + (ONE_DAY * 7)); // TODO: Read in from settings class

                values.put(Tasks.COLUMN_DUE_SOON.name, today.after(dueDate));

            }

            db.update(
                    Tasks.TABLE_NAME,
                    values,
                    Tasks.COLUMN_ID.name + "=?",
                    new String[]{id}
            );
        }
    }
}
