package nu.huw.clarity.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import nu.huw.clarity.activity.MainActivity;
import nu.huw.clarity.db.DatabaseContract.*;

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

        MainActivity.context.deleteDatabase(DATABASE_NAME);
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
}
