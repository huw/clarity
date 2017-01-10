package nu.huw.clarity.db.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Entries;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Folder;

class FolderHelper {

    private static final String NO_PARENT  = Folders.COLUMN_PARENT_ID + " IS NULL";
    private static final String PARENT_ARG = Folders.COLUMN_PARENT_ID + " = ?";
    private static final String ID_ARG     = Folders.COLUMN_ID + " = ?";
    private DatabaseHelper dbHelper;

    FolderHelper(DatabaseHelper dbHelper) {

        this.dbHelper = dbHelper;
    }

    /**
     * Gets a list of child folders matching the given parent folder. Note that folders can only
     * have another folder as a parent, or no parent.
     *
     * @param parent Any Folder or null, where null will return all top-level folders
     */
    List<Folder> getFoldersFromParent(Folder parent) {

        if (parent == null) {
            return getFoldersFromSelection(NO_PARENT, null);
        } else {
            return getFoldersFromSelection(PARENT_ARG, new String[]{parent.id});
        }
    }

    /**
     * Get the folder with the specified ID
     *
     * @param id ID of a folder
     */
    Folder getFolderFromID(String id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Folders.TABLE_NAME, Folders.columns, ID_ARG, new String[]{id});

        cursor.moveToFirst();
        Folder result = getFolderFromCursor(cursor);

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Gets all folders matching a given SQL selection
     *
     * @param selection     SQL selection matching a list of folders
     * @param selectionArgs Arguments for the selection
     */
    private List<Folder> getFoldersFromSelection(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Folders.TABLE_NAME, Folders.columns, selection, selectionArgs);

        List<Folder> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getFolderFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Fetch a Folder object from a given cursor
     *
     * @param cursor A database cursor containing a folder object at the current pointer
     */
    private Folder getFolderFromCursor(Cursor cursor) {

        Folder folder = new Folder();

        // Base methods
        folder.id = dbHelper.getString(cursor, Base.COLUMN_ID);
        folder.dateAdded = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED);
        folder.dateModified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED);

        // Entries methods
        folder.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE);
        folder.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN);
        folder.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED);
        folder.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON);
        folder.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE);
        folder.name = dbHelper.getString(cursor, Entries.COLUMN_NAME);
        folder.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID);
        folder.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK);

        return folder;
    }
}
