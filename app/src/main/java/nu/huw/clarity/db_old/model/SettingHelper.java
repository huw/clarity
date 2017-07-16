package nu.huw.clarity.db_old.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import nu.huw.clarity.db_old.DatabaseContract.Settings;
import nu.huw.clarity.db_old.DatabaseHelper;

/**
 * Contains helper methods for obtaining settings from the database
 */
public class SettingHelper {

  private static final String ID_ARG = Settings.COLUMN_ID + " = ?";
  private DatabaseHelper dbHelper;

  SettingHelper(DatabaseHelper dbHelper) {

    this.dbHelper = dbHelper;
  }

  /**
   * Get the setting value for the specified setting ID
   *
   * @param id ID of a context
   */
  @Nullable
  String getSettingFromID(String id) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String[] columns = new String[]{Settings.COLUMN_VALUE};
    Cursor cursor =
        dbHelper.query(db, Settings.TABLE_NAME, columns, ID_ARG, new String[]{id});

    String result = null;
    if (cursor.moveToFirst()) {
      result = dbHelper.getString(cursor, Settings.COLUMN_VALUE);
    }

    cursor.close();
    db.close();
    return result;
  }

}
