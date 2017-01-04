package nu.huw.clarity.db.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Attachment;

class AttachmentHelper {

    private DatabaseHelper dbHelper;

    AttachmentHelper(DatabaseHelper dbHelper) {

        this.dbHelper = dbHelper;
    }

    /**
     * Return an Attachment from the data model, given an ID
     *
     * @param id OmniFocus ID of Attachment (usually specified in Task)
     */
    Attachment getAttachment(String id) {

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
     * Fetch an Attachment object from a given cursor
     *
     * @param cursor A database cursor containing an attachment object at the current pointer
     */
    private Attachment getAttachmentFromCursor(Cursor cursor) {

        Attachment attachment = new Attachment();

        attachment.id = dbHelper.getString(cursor, Attachments.COLUMN_ID.name);
        attachment.name = dbHelper.getString(cursor, Attachments.COLUMN_NAME.name);
        attachment.parentID = dbHelper.getString(cursor, Attachments.COLUMN_PARENT_ID.name);
        attachment.path = dbHelper.getString(cursor, Attachments.COLUMN_PATH.name);
        // attachment.preview = dbHelper.getString(cursor, Attachments.COLUMN_PNG_PREVIEW.name);
        // TODO
        attachment.dateAdded = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_ADDED.name);
        attachment.dateModified = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_MODIFIED.name);

        return attachment;
    }
}
