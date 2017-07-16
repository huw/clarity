package nu.huw.clarity.db_old.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import nu.huw.clarity.db_old.DatabaseContract.Attachments;
import nu.huw.clarity.db_old.DatabaseHelper;
import nu.huw.clarity.model_old.Attachment;
import nu.huw.clarity.model_old.Task;

class AttachmentHelper {

  private static final String PARENT_ARG = Attachments.COLUMN_PARENT_ID + " = ?";
  private DatabaseHelper dbHelper;

  AttachmentHelper(DatabaseHelper dbHelper) {

    this.dbHelper = dbHelper;
  }

  /**
   * Gets a list of attachments belonging to the parent task
   *
   * @param parent Any task
   */
  List<Attachment> getAttachmentsFromParent(@NonNull Task parent) {
    return getAttachmentsFromSelection(PARENT_ARG, new String[]{parent.id});
  }

  /**
   * Gets all attachments matching a given SQL selection
   *
   * @param selection SQL selection matching a list of attachments
   * @param selectionArgs Arguments for the selection
   */
  private List<Attachment> getAttachmentsFromSelection(String selection, String[] selectionArgs) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor =
        dbHelper.query(db, Attachments.TABLE_NAME, Attachments.columns, selection, selectionArgs);

    List<Attachment> result = new ArrayList<>();
    while (cursor.moveToNext()) {
      result.add(getAttachmentFromCursor(cursor));
    }

    cursor.close();
    db.close();
    return result;
  }

  /**
   * Return an Attachment from the data model, given an ID
   *
   * @param id OmniFocus ID of Attachment (usually specified in Task)
   */
  Attachment getAttachment(String id) {

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String selection = Attachments.COLUMN_ID + " = ?";
    String[] selectionArgs = {id};

    Cursor cursor = dbHelper.query(db, Attachments.TABLE_NAME, Attachments.columns, selection,
        selectionArgs);
    cursor.moveToFirst();

    Attachment result = getAttachmentFromCursor(cursor);

    cursor.close();
    db.close();
    return result;
  }

  /**
   * Fetch an Attachment object from a given cursor
   *
   * @param cursor A database cursor containing an attachment object at the current pointer
   */
  private Attachment getAttachmentFromCursor(Cursor cursor) {

    Attachment attachment = new Attachment();

    attachment.id = dbHelper.getString(cursor, Attachments.COLUMN_ID);
    attachment.name = dbHelper.getString(cursor, Attachments.COLUMN_NAME);
    attachment.parentID = dbHelper.getString(cursor, Attachments.COLUMN_PARENT_ID);
    attachment.file = new File(dbHelper.getString(cursor, Attachments.COLUMN_PATH));
    // attachment.preview = dbHelper.getString(cursor, Attachments.COLUMN_PNG_PREVIEW);
    // TODO
    attachment.dateAdded = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_ADDED);
    attachment.dateModified = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_MODIFIED);

    return attachment;
  }
}
