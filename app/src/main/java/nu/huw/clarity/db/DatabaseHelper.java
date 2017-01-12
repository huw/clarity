package nu.huw.clarity.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Perspectives;
import nu.huw.clarity.db.DatabaseContract.Settings;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeParseException;

/**
 * A set of helper methods for dealing with the SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

  public static final int DATABASE_VERSION = 4;
  public static final String DATABASE_NAME = "OmniFocus";
  private static final String TAG = DatabaseHelper.class.getSimpleName();

  public DatabaseHelper(Context context) {

    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    db.execSQL(Attachments.CREATE);
    db.execSQL(Contexts.CREATE);
    db.execSQL(Folders.CREATE);
    db.execSQL(Perspectives.CREATE);
    db.execSQL(Settings.CREATE);
    db.execSQL(Tasks.CREATE);
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
   * Given important data, insert a new row into the database with the given rowID. The values
   * should have keys that correspond to columns in the database, and appropriate values, but this
   * isn't validated because that would be wasteful. However, we check to see if there are any
   * values to be added first.
   */
  public void insert(String tableName, String rowID, ContentValues values) {

    SQLiteDatabase db = this.getWritableDatabase();

    if (values.size() != 0) {

      // null means insert
      values.put(Base.COLUMN_ID, rowID);

      try {
        db.insertOrThrow(tableName, null, values);
      } catch (SQLiteConstraintException e) {

        // If there's an error because we already have this key,
        // then just update it! Yay!
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

      String selection = Base.COLUMN_ID + "=?";
      String[] selectionArgs = {rowID};
      db.update(tableName, values, selection, selectionArgs);
    }

    db.close();
  }

  /**
   * Delete the entry with the given ID. No need for any ContentValues.
   */
  public void delete(String tableName, String rowID) {

    SQLiteDatabase db = this.getWritableDatabase();

    String selection = Base.COLUMN_ID + "=?";
    String[] selectionArgs = {rowID};
    db.delete(tableName, selection, selectionArgs);

    db.close();
  }

  /**
   * Just a nice shorthand for getting strings from the database cursor, but also with some bonus
   * null checking.
   */
  public String getString(Cursor cursor, String columnName) {

    int index = cursor.getColumnIndex(columnName);
    if (index != -1 && cursor.getColumnCount() > 0) {
      String string = cursor.getString(index);
      if (string == null) {
        return null;
      } else if (!cursor.getString(index).isEmpty()) {
        return cursor.getString(index);
      }
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
  public LocalDateTime getDate(Cursor cursor, String columnName) {

    try {
      int index = cursor.getColumnIndex(columnName);
      if (index != -1 && cursor.getColumnCount() != 0) {
        String date = cursor.getString(index);

        if (date != null) {
          return LocalDateTime.ofInstant(Instant.parse(date), ZoneId.systemDefault());
        }
      } else if (index == -1) {
        throw new IndexOutOfBoundsException("No such column " + columnName);
      }
      return null;
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  /**
   * Get a duration from a string.
   */
  public Duration getDuration(Cursor cursor, String columnName) {

    try {
      int index = cursor.getColumnIndex(columnName);
      if (index != -1 && cursor.getColumnCount() != 0) {
        String duration = cursor.getString(index);

        if (duration != null) return Duration.parse(duration);
      } else if (index == -1) {
        throw new IndexOutOfBoundsException("No such column " + columnName);
      }
      return null;
    } catch (DateTimeParseException e) {
      return null;
    }
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
