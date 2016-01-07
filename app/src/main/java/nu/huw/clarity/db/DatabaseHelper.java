package nu.huw.clarity.db;

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
    public static final String DATABASE_NAME = "OmniFocus.db";

    public static String SQL_CREATE_TABLES;
    public static String SQL_DROP_TABLES;

    public DatabaseHelper() {
        super(MainActivity.context, DATABASE_NAME, null, DATABASE_VERSION);

        /**
         * For each table, generate a CREATE TABLE statement from the values
         * we have in the table.keys field. At the end, remove the final comma
         * with a call to substring, and move on.
         */
        String createAttachments = "CREATE TABLE " + Attachments.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Attachments.keys) {
            createAttachments += keyValue;
            if (keyValue.name.equals("id")) { createAttachments += " PRIMARY KEY"; }
            if (keyValue.val != null) { createAttachments += " DEFAULT " + keyValue.val; }
            createAttachments += ",";
        }
        createAttachments = createAttachments.substring(0, createAttachments.length() - 1) + ")";

        String createContexts = "CREATE TABLE " + Contexts.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Contexts.keys) {
            createContexts += keyValue;
            if (keyValue.name.equals("id")) { createContexts += " PRIMARY KEY"; }
            if (keyValue.val != null) { createContexts += " DEFAULT " + keyValue.val; }
            createContexts += ",";
        }
        createContexts = createContexts.substring(0, createContexts.length() - 1) + ")";

        String createFolders = "CREATE TABLE " + Folders.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Folders.keys) {
            createFolders += keyValue;
            if (keyValue.name.equals("id")) { createFolders += " PRIMARY KEY"; }
            if (keyValue.val != null) { createFolders += " DEFAULT " + keyValue.val; }
            createFolders += ",";
        }
        createFolders = createFolders.substring(0, createFolders.length() - 1) + ")";

        String createPerspectives = "CREATE TABLE " + Perspectives.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Perspectives.keys) {
            createPerspectives += keyValue;
            if (keyValue.name.equals("id")) { createPerspectives += " PRIMARY KEY"; }
            if (keyValue.val != null) { createPerspectives += " DEFAULT " + keyValue.val; }
            createPerspectives += ",";
        }
        createPerspectives = createPerspectives.substring(0, createPerspectives.length() - 1) + ")";

        String createSettings = "CREATE TABLE " + Settings.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Settings.keys) {
            createSettings += keyValue;
            if (keyValue.name.equals("id")) { createSettings += " PRIMARY KEY"; }
            if (keyValue.val != null) { createSettings += " DEFAULT " + keyValue.val; }
            createSettings += ",";
        }
        createSettings = createSettings.substring(0, createSettings.length() - 1) + ")";

        String createTasks = "CREATE TABLE " + Tasks.TABLE_NAME + "(";
        for (SQLKeyValue keyValue: Tasks.keys) {
            createTasks += keyValue;
            if (keyValue.name.equals("id")) { createTasks += " PRIMARY KEY"; }
            if (keyValue.val != null) { createTasks += " DEFAULT " + keyValue.val; }
            createTasks += ",";
        }
        createTasks = createTasks.substring(0, createTasks.length() - 1) + ")";

        SQL_CREATE_TABLES =
                createAttachments + ";" +
                createContexts + ";" +
                createFolders + ";" +
                createPerspectives + ";" +
                createSettings + ";" +
                createTasks;

        SQL_DROP_TABLES =
                "DROP TABLE IF EXISTS " + Attachments.TABLE_NAME + ";" +
                "DROP TABLE IF EXISTS " + Contexts.TABLE_NAME + ";" +
                "DROP TABLE IF EXISTS " + Folders.TABLE_NAME + ";" +
                "DROP TABLE IF EXISTS " + Perspectives.TABLE_NAME + ";" +
                "DROP TABLE IF EXISTS " + Settings.TABLE_NAME + ";" +
                "DROP TABLE IF EXISTS " + Tasks.TABLE_NAME;

        Log.d(TAG, SQL_CREATE_TABLES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLES);
        Log.i(TAG, "Database successfully created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLES);
    }
}
