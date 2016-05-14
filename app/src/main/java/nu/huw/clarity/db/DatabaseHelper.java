package nu.huw.clarity.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Perspectives;
import nu.huw.clarity.db.DatabaseContract.SQLKeyValue;
import nu.huw.clarity.db.DatabaseContract.Settings;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.ui.MainActivity;

/**
 * A set of helper methods for dealing with the SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final  int    DATABASE_VERSION = 1;
    public static final  String DATABASE_NAME    = "OmniFocus";
    private static final String TAG              = DatabaseHelper.class.getSimpleName();
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
         * TODO: Replace all this with hardcoded strings (in final version)
         */
        SQL_CREATE_ATTACHMENTS = "CREATE TABLE " + Attachments.TABLE_NAME + "(";
        for (SQLKeyValue keyValue : Attachments.keys) {
            SQL_CREATE_ATTACHMENTS += keyValue;
            if (keyValue.name.equals("id")) {
                SQL_CREATE_ATTACHMENTS += " PRIMARY KEY";
            }
            if (keyValue.type != null) {
                SQL_CREATE_ATTACHMENTS += " " + keyValue.type;
            }
            if (keyValue.val != null) {
                SQL_CREATE_ATTACHMENTS += " DEFAULT " + keyValue.val;
            }
            SQL_CREATE_ATTACHMENTS += ",";
        }
        SQL_CREATE_ATTACHMENTS =
                SQL_CREATE_ATTACHMENTS.substring(0, SQL_CREATE_ATTACHMENTS.length() - 1) + ")";

        SQL_CREATE_CONTEXTS = "CREATE TABLE " + Contexts.TABLE_NAME + "(";
        for (SQLKeyValue keyValue : Contexts.keys) {
            SQL_CREATE_CONTEXTS += keyValue;
            if (keyValue.name.equals("id")) {
                SQL_CREATE_CONTEXTS += " PRIMARY KEY";
            }
            if (keyValue.type != null) {
                SQL_CREATE_CONTEXTS += " " + keyValue.type;
            }
            if (keyValue.val != null) {
                SQL_CREATE_CONTEXTS += " DEFAULT " + keyValue.val;
            }
            SQL_CREATE_CONTEXTS += ",";
        }
        SQL_CREATE_CONTEXTS =
                SQL_CREATE_CONTEXTS.substring(0, SQL_CREATE_CONTEXTS.length() - 1) + ")";

        SQL_CREATE_FOLDERS = "CREATE TABLE " + Folders.TABLE_NAME + "(";
        for (SQLKeyValue keyValue : Folders.keys) {
            SQL_CREATE_FOLDERS += keyValue;
            if (keyValue.name.equals("id")) {
                SQL_CREATE_FOLDERS += " PRIMARY KEY";
            }
            if (keyValue.type != null) {
                SQL_CREATE_FOLDERS += " " + keyValue.type;
            }
            if (keyValue.val != null) {
                SQL_CREATE_FOLDERS += " DEFAULT " + keyValue.val;
            }
            SQL_CREATE_FOLDERS += ",";
        }
        SQL_CREATE_FOLDERS = SQL_CREATE_FOLDERS.substring(0, SQL_CREATE_FOLDERS.length() - 1) + ")";

        SQL_CREATE_PERSPECTIVES = "CREATE TABLE " + Perspectives.TABLE_NAME + "(";
        for (SQLKeyValue keyValue : Perspectives.keys) {
            SQL_CREATE_PERSPECTIVES += keyValue;
            if (keyValue.name.equals("id")) {
                SQL_CREATE_PERSPECTIVES += " PRIMARY KEY";
            }
            if (keyValue.type != null) {
                SQL_CREATE_PERSPECTIVES += " " + keyValue.type;
            }
            if (keyValue.val != null) {
                SQL_CREATE_PERSPECTIVES += " DEFAULT " + keyValue.val;
            }
            SQL_CREATE_PERSPECTIVES += ",";
        }
        SQL_CREATE_PERSPECTIVES =
                SQL_CREATE_PERSPECTIVES.substring(0, SQL_CREATE_PERSPECTIVES.length() - 1) + ")";

        SQL_CREATE_SETTINGS = "CREATE TABLE " + Settings.TABLE_NAME + "(";
        for (SQLKeyValue keyValue : Settings.keys) {
            SQL_CREATE_SETTINGS += keyValue;
            if (keyValue.name.equals("id")) {
                SQL_CREATE_SETTINGS += " PRIMARY KEY";
            }
            if (keyValue.type != null) {
                SQL_CREATE_SETTINGS += " " + keyValue.type;
            }
            if (keyValue.val != null) {
                SQL_CREATE_SETTINGS += " DEFAULT " + keyValue.val;
            }
            SQL_CREATE_SETTINGS += ",";
        }
        SQL_CREATE_SETTINGS =
                SQL_CREATE_SETTINGS.substring(0, SQL_CREATE_SETTINGS.length() - 1) + ")";

        SQL_CREATE_TASKS = "CREATE TABLE " + Tasks.TABLE_NAME + "(";
        for (SQLKeyValue keyValue : Tasks.keys) {
            SQL_CREATE_TASKS += keyValue;
            if (keyValue.name.equals("id")) {
                SQL_CREATE_TASKS += " PRIMARY KEY";
            }
            if (keyValue.type != null) {
                SQL_CREATE_TASKS += " " + keyValue.type;
            }
            if (keyValue.val != null) {
                SQL_CREATE_TASKS += " DEFAULT " + keyValue.val;
            }
            SQL_CREATE_TASKS += ",";
        }
        SQL_CREATE_TASKS = SQL_CREATE_TASKS.substring(0, SQL_CREATE_TASKS.length() - 1) + ")";
    }

    @Override public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ATTACHMENTS);
        db.execSQL(SQL_CREATE_CONTEXTS);
        db.execSQL(SQL_CREATE_FOLDERS);
        db.execSQL(SQL_CREATE_PERSPECTIVES);
        db.execSQL(SQL_CREATE_SETTINGS);
        db.execSQL(SQL_CREATE_TASKS);
        Log.i(TAG, "Database successfully created");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Attachments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contexts.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Folders.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Perspectives.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Settings.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tasks.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Given important data, insert a new row into the database with the given rowID. The values
     * should have keys that correspond to columns in the database, and appropriate values, but this
     * isn't validated because that would be wasteful. However, we check to see if there are any
     * values to be added first.
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

            String   selection     = Base.COLUMN_ID.name + "=?";
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

        String   selection     = Base.COLUMN_ID.name + "=?";
        String[] selectionArgs = {rowID};
        db.delete(tableName, selection, selectionArgs);

        Log.v(TAG, rowID + " deleted from " + tableName);

        db.close();
    }

    /**
     * Just a nice shorthand for getting strings from the database cursor, but also with some bonus
     * null checking.
     */
    public String getString(Cursor cursor, String columnName) {

        int index = cursor.getColumnIndex(columnName);
        if (index != -1 && cursor.getColumnCount() != 0) {
            return cursor.getString(index);
        } else if (index == -1) {
            throw new IndexOutOfBoundsException("No such column " + columnName);
        }
        return null;
    }

    /**
     * Shorthand for getting ints. Null checking included.
     */
    public int getInt(Cursor cursor, String columnName) {

        int index = cursor.getColumnIndex(columnName);
        if (index != -1 && cursor.getColumnCount() != 0) {
            return cursor.getInt(index);
        } else if (index == -1) {
            throw new IndexOutOfBoundsException("No such column " + columnName);
        }
        return 0;
    }

    public long getLong(Cursor cursor, String columnName) {

        int index = cursor.getColumnIndex(columnName);
        if (index != -1 && cursor.getColumnCount() != 0) {
            return cursor.getLong(index);
        } else if (index == -1) {
            throw new IndexOutOfBoundsException("No such column " + columnName);
        }
        return 0;
    }

    /**
     * Shorthand for getting a boolean. Null = false.
     */
    public boolean getBoolean(Cursor cursor, String columnName) {

        String string = getString(cursor, columnName);
        return string != null && string.equals("1");
    }

    /**
     * Get a date from a string.
     */
    public Date getDate(Cursor cursor, String columnName) {

        int index = cursor.getColumnIndex(columnName);
        if (index != -1 && cursor.getColumnCount() != 0) {
            long milliseconds = cursor.getLong(index);

            if (milliseconds != 0) return new Date(milliseconds);
        } else if (index == -1) {
            throw new IndexOutOfBoundsException("No such column " + columnName);
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
}
