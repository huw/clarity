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

    public static final String SQL_CREATE_ATTACHMENTS =
            "CREATE TABLE " + Attachments.TABLE_NAME + "(" +
                Attachments.COLUMN_ID                           + " TEXT," +
                Attachments.COLUMN_CONTAINING_TRANSACTION       + " TEXT," +
                Attachments.COLUMN_DATE_ADDED                   + " TIMESTAMP," +
                Attachments.COLUMN_DATE_MODIFIED                + " TIMESTAMP," +
                Attachments.COLUMN_NAME                         + " TEXT," +
                Attachments.COLUMN_TASK                         + " TEXT" +
            ")";
    public static final String SQL_CREATE_CONTEXTS =
            "CREATE TABLE " + Contexts.TABLE_NAME + "(" +
                Contexts.COLUMN_ID                              + " TEXT," +
                Contexts.COLUMN_ACTIVE                          + " INTEGER," +
                Contexts.COLUMN_ALTITUDE                        + " REAL," +
                Contexts.COLUMN_DATE_ADDED                      + " TIMESTAMP," +
                Contexts.COLUMN_DATE_MODIFIED                   + " TIMESTAMP," +
                Contexts.COLUMN_DUE_SOON_COUNT                  + " INTEGER," +
                Contexts.COLUMN_HAS_CHILDREN                    + " INTEGER," +
                Contexts.COLUMN_LATITTUDE                       + " REAL," +
                Contexts.COLUMN_LOCATION_NAME                   + " TEXT," +
                Contexts.COLUMN_LONGITUDE                       + " REAL," +
                Contexts.COLUMN_NAME                            + " TEXT," +
                Contexts.COLUMN_ON_HOLD                         + " INTEGER," +
                Contexts.COLUMN_OVERDUE_COUNT                   + " INTEGER," +
                Contexts.COLUMN_PARENT                          + " TEXT," +
                Contexts.COLUMN_RADIUS                          + " REAL," +
                Contexts.COLUMN_RANK                            + " INTEGER," +
                Contexts.COLUMN_REMAINING_COUNT                 + " INTEGER" +
            " ) ";
    public static final String SQL_CREATE_FOLDERS =
            "CREATE TABLE " + Folders.TABLE_NAME + "(" +
                Folders.COLUMN_ID                               + " TEXT," +
                Folders.COLUMN_DATE_ADDED                       + " TIMESTAMP," +
                Folders.COLUMN_DATE_MODIFIED                    + " TIMESTAMP," +
                Folders.COLUMN_DUE_SOON_COUNT                   + " INTEGER," +
                Folders.COLUMN_HAS_CHILDREN + " INTEGER," +
                Folders.COLUMN_NAME                             + " TEXT," +
                Folders.COLUMN_OVERDUE_COUNT                    + " INTEGER," +
                Folders.COLUMN_PARENT                           + " TEXT," +
                Folders.COLUMN_RANK                             + " INTEGER," +
                Folders.COLUMN_REMAINING_COUNT                  + " INTEGER" +
            ")";
    public static final String SQL_CREATE_PERSPECTIVES =
            "CREATE TABLE " + Perspectives.TABLE_NAME + "(" +
                Perspectives.COLUMN_ID                          + " TEXT," +
                Perspectives.COLUMN_DATE_ADDED                  + " TIMESTAMP," +
                Perspectives.COLUMN_DATE_MODIFIED               + " TIMESTAMP," +
                Perspectives.COLUMN_VALUE                       + " BLOB" +
            ")";
    public static final String SQL_CREATE_PROJECTS =
            "CREATE TABLE " + Projects.TABLE_NAME + "(" +
                Projects.COLUMN_ID                              + " TEXT," +
                Projects.COLUMN_DEFER                           + " TIMESTAMP," +
                Projects.COLUMN_DUE                             + " TIMESTAMP," +
                Projects.COLUMN_DUE_SOON_COUNT                  + " INTEGER," +
                Projects.COLUMN_FOLDER                          + " TEXT," +
                Projects.COLUMN_LAST_REVIEW                     + " TIMESTAMP," +
                Projects.COLUMN_OVERDUE_COUNT                   + " INTEGER," +
                Projects.COLUMN_REMAINING_COUNT                 + " INTEGER," +
                Projects.COLUMN_REPEAT_REVIEW                   + " TEXT," +
                Projects.COLUMN_SINGLE_ACTION                   + " INTEGER," +
                Projects.COLUMN_STATUS                          + " TEXT," +
                Projects.COLUMN_TASK                            + " TEXT," +
                Projects.COLUMN_TASK_BLOCKED_BY_DEFER           + " INTEGER" +
            ")";
    public static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE " + Settings.TABLE_NAME + "(" +
                Settings.COLUMN_ID                              + " TEXT," +
                Settings.COLUMN_DATE_ADDED                      + " TIMESTAMP," +
                Settings.COLUMN_DATE_MODIFIED                   + " TIMESTAMP," +
                Settings.COLUMN_VALUE                           + " BLOB" +
            ")";
    public static final String SQL_CREATE_TASKS =
            "CREATE TABLE " + Tasks.TABLE_NAME + "(" +
                Tasks.COLUMN_ID                                 + " TEXT," +
                Tasks.COLUMN_BLOCKED_BY_DEFER                   + " INTEGER," +
                Tasks.COLUMN_COMPLETE_WITH_CHILDREN             + " INTEGER," +
                Tasks.COLUMN_CONTAINING_PROJECT                 + " TEXT," +
                Tasks.COLUMN_CONTEXT                            + " TEXT," +
                Tasks.COLUMN_DATE_ADDED                         + " TIMESTAMP," +
                Tasks.COLUMN_DATE_COMPLETED                     + " TIMESTAMP," +
                Tasks.COLUMN_DATE_DUE                           + " TIMESTAMP," +
                Tasks.COLUMN_DATE_MODIFIED                      + " TIMESTAMP," +
                Tasks.COLUMN_DEFER                              + " TIMESTAMP," +
                Tasks.COLUMN_ESTIMATED_TIME                     + " INTEGER," +
                Tasks.COLUMN_FLAGGED                            + " INTEGER," +
                Tasks.COLUMN_HAS_CHILDREN                       + " INTEGER," +
                Tasks.COLUMN_INBOX                              + " INTEGER," +
                Tasks.COLUMN_NAME                               + " TEXT," +
                Tasks.COLUMN_NOTE_PLAINTEXT                     + " TEXT," +
                Tasks.COLUMN_NOTE_XML                           + " BLOB," +
                Tasks.COLUMN_PARENT                             + " TEXT," +
                Tasks.COLUMN_PROJECT                            + " TEXT," +
                Tasks.COLUMN_PROJECT_SINGLE_ACTION              + " INTEGER," +
                Tasks.COLUMN_RANK                               + " INTEGER," +
                Tasks.COLUMN_REPETITION_METHOD                  + " TEXT," +
                Tasks.COLUMN_REPETITION_RULE                    + " TEXT," +
                Tasks.COLUMN_SEQUENTIAL                         + " INTEGER" +
            ")";

    public static final String SQL_CREATE_ENTRIES =
         // SQL_CREATE_ATTACHMENTS + ";" + (Not yet)
            SQL_CREATE_CONTEXTS + ";" +
            SQL_CREATE_FOLDERS + ";" +
         // SQL_CREATE_PERSPECTIVES + ";" +
            SQL_CREATE_PROJECTS + ";" +
         // SQL_CREATE_SETTINGS + ";" +
            SQL_CREATE_TASKS;

    public static final String SQL_DROP_TABLES =
            " DROP TABLE IF EXISTS" + Attachments.TABLE_NAME +
            " DROP TABLE IF EXISTS" + Contexts.TABLE_NAME +
            " DROP TABLE IF EXISTS" + Folders.TABLE_NAME +
            " DROP TABLE IF EXISTS" + Perspectives.TABLE_NAME +
            " DROP TABLE IF EXISTS" + Projects.TABLE_NAME +
            " DROP TABLE IF EXISTS" + Settings.TABLE_NAME +
            " DROP TABLE IF EXISTS" + Tasks.TABLE_NAME;

    public DatabaseHelper() {
        super(MainActivity.context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.i(TAG, "Database successfully created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLES);
    }
}
