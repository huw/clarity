package nu.huw.clarity.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Entry;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Perspectives;
import nu.huw.clarity.db.DatabaseContract.Settings;
import nu.huw.clarity.db.DatabaseContract.Tasks;

/**
 * Converts a contents.xml into a usable object
 */
public class SyncDownParser {

    private static final String         TAG       = SyncDownParser.class.getSimpleName();
    private static final String         namespace =
            "http://www.omnigroup.com/namespace/OmniFocus/v1";
    private static final DatabaseHelper mDBHelper = new DatabaseHelper();

    public static void parse(InputStream input) {

        Log.v(TAG, "New file encountered");

        try {

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);
            parser.next(); // Bypass the header tag

            // Require that the first tag is an XML start (i.e. "<start>" and
            // not "</finish>" or "<self-closing />", that we belong to the
            // OmniFocus namespace (see the variable above), and that the tag
            // name is 'omnifocus'. A good start to reading these things.

            parser.require(XmlPullParser.START_TAG, namespace, "omnifocus");

            while (parser.next() != XmlPullParser.END_DOCUMENT) {

                if (parser.getEventType() != XmlPullParser.START_TAG) {

                    // We only want to deal with parent tags for now, and
                    // some self-closing child tags can accidentally match
                    // out parameters.

                    continue;
                }

                String tableName;
                String tagName = parser.getName();
                switch (tagName) {
                    case "attachment":
                        tableName = Attachments.TABLE_NAME;
                        break;
                    case "setting":
                        tableName = Settings.TABLE_NAME;
                        break;
                    case "context":
                        tableName = Contexts.TABLE_NAME;
                        break;
                    case "folder":
                        tableName = Folders.TABLE_NAME;
                        break;
                    case "task":
                        tableName = Tasks.TABLE_NAME;
                        break;
                    case "perspective":
                        tableName = Perspectives.TABLE_NAME;
                        break;
                    default:
                        tableName = "";
                }

                parseEntry(tableName, parser);
            }

            input.close();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error parsing XML", e);
        } catch (IOException e) {
            Log.e(TAG, "Streams unexpectedly crossed", e);
        }
    }

    private static void parseEntry(String tableName, XmlPullParser parser) {

        // Create a map of values to add in the new line
        ContentValues values = new ContentValues();

        String id        = parser.getAttributeValue(null, "id");
        String operation = "";

        if (parser.getAttributeValue(null, "op") != null) {
            operation = parser.getAttributeValue(null, "op");
        }

        if (id != null) {

            try {

                /**
                 * Loop through the tags in this part of the tree. This loop will
                 * catch every tag under the current one, which is nice.
                 */
                int depth = 1;
                while (depth != 0) {
                    int next = parser.next();
                    if (next == XmlPullParser.START_TAG) {
                        depth++;

                        parseTag(parser, values, tableName);
                    } else if (next == XmlPullParser.END_TAG) {
                        depth--;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing XML", e);
            }
        }

        switch (operation) {
            case "reference":

                SQLiteDatabase db = mDBHelper.getReadableDatabase();

                String[] columns = {Base.COLUMN_ID.name};
                String selection = Base.COLUMN_ID.name + "=?";
                String[] selectionArgs = {id};

                Cursor cursor =
                        db.query(tableName, columns, selection, selectionArgs, null, null, null);

                // If there are rows where the 'id' attribute is equal to the
                // id being referenced, then we haven't accidentally lost them,
                // so we can just continue.
                //
                // Otherwise, we've lost these rows for some reason, and we
                // need to get them back. So just fall through to the 'insert'
                // statement.

                if (cursor.getCount() > 0) {
                    cursor.close();
                    db.close();
                    break;
                }

                cursor.close();
                db.close();

            case "":
                mDBHelper.insert(tableName, id, values);
                break;
            case "update":
                mDBHelper.update(tableName, id, values);
                break;
            case "delete":
                mDBHelper.delete(tableName, id);
                break;
        }
    }

    /**
     * Given a parser object, parses the current XML tag appropriately, and adds the result to the
     * passed ContentValues.
     */
    private static void parseTag(XmlPullParser parser, ContentValues values, String table)
            throws IOException, XmlPullParserException {

        String name  = "";
        String value = "";

        switch (parser.getName()) {

            /**
             * Parents
             */
            case "context":
                if (table.equals(Tasks.TABLE_NAME)) {
                    name = Tasks.COLUMN_CONTEXT.name;
                    value = parser.getAttributeValue(null, "idref");
                    break;
                }
                // else, fall through to adding a parent ID

            case "folder":
            case "task":
                name = Entry.COLUMN_PARENT_ID.name;
                value = parser.getAttributeValue(null, "idref");
                break;

            /**
             * Base
             */
            case "added":
                parser.next();
                name = Base.COLUMN_DATE_ADDED.name;
                value = convertToMilliseconds(parser.getText());
                break;

            case "modified":
                parser.next();
                name = Base.COLUMN_DATE_MODIFIED.name;
                value = convertToMilliseconds(parser.getText());
                break;

            /**
             * Entry
             */
            case "name":
                parser.next();
                name = Entry.COLUMN_NAME.name;
                value = parser.getText();
                break;

            case "rank":
                parser.next();
                name = Entry.COLUMN_RANK.name;
                value = parser.getText();
                break;

            case "hidden":
                parser.next();
                name = Entry.COLUMN_ACTIVE.name;
                value = parser.getText().equals("true") ? "0" : "1"; // Invert and convert
                break;

            /**
             * Attachment
             */
            case "preview-image":
                parser.next();
                name = Attachments.COLUMN_PNG_PREVIEW.name;
                value = parser.getText();
                break;

            /**
             * Context
             */
            case "location":
                // If it's a null value, then we just add a null value to the database.
                String locationName = parser.getAttributeValue(null, "name");
                String altitude = parser.getAttributeValue(null, "altitude");
                String latitude = parser.getAttributeValue(null, "latitude");
                String longitude = parser.getAttributeValue(null, "longitude");
                String radius = parser.getAttributeValue(null, "radius");

                values.put(Contexts.COLUMN_LOCATION_NAME.name, locationName);
                values.put(Contexts.COLUMN_ALTITUDE.name, altitude);
                values.put(Contexts.COLUMN_LATITUDE.name, latitude);
                values.put(Contexts.COLUMN_LONGITUDE.name, longitude);
                values.put(Contexts.COLUMN_RADIUS.name, radius);
                break;

            case "prohibits-next-action":
                parser.next();
                name = Contexts.COLUMN_ON_HOLD.name;
                value = String.valueOf(5 - parser.getText().length()); // I'm so sorry
                break;

            /**
             * Perspectives/Settings
             * TODO: Actually parse
             */
            case "plist":
                break;

            /**
             * Tasks
             */
            case "order":

                // The task type is a tricky one, because it can be either 'sequential',
                // 'parallel' or 'single action', but 'single action' is only applicable
                // to projects and comes from a different tag (see "singleton" below).
                // As the "singleton" tag will always come first, we can test to see if
                // we've already added the 'type' column, and only proceed if we haven't.

                if (!values.containsKey(Tasks.COLUMN_TYPE.name)) {
                    parser.next();
                    name = Tasks.COLUMN_TYPE.name;
                    value = parser.getText();
                }
                break;

            case "completed-by-children":
                parser.next();
                name = Tasks.COLUMN_COMPLETE_WITH_CHILDREN.name;
                value = parser.getText().equals("true") ? "1" : "0";
                break;

            case "start":
                parser.next();
                name = Tasks.COLUMN_DATE_DEFER.name;
                value = convertToMilliseconds(parser.getText());
                break;

            case "due":
                parser.next();
                name = Tasks.COLUMN_DATE_DUE.name;
                value = convertToMilliseconds(parser.getText());
                break;

            case "completed":
                parser.next();
                name = Tasks.COLUMN_DATE_COMPLETED.name;
                value = convertToMilliseconds(parser.getText());
                break;

            case "estimated-minutes":
                parser.next();
                name = Tasks.COLUMN_ESTIMATED_TIME.name;
                value = parser.getText();
                break;

            case "flagged":
                parser.next();
                name = Tasks.COLUMN_FLAGGED.name;
                value = parser.getText().equals("true") ? "1" : "0";
                break;

            case "inbox":
                name = Tasks.COLUMN_INBOX.name;
                value = "1";
                break;

            case "note":
                //TODO: Parse notes
                break;

            case "repetition-rule":
                parser.next();
                name = Tasks.COLUMN_REPETITION_RULE.name;
                value = parser.getText();
                break;

            case "repetition-method":
                parser.next();
                name = Tasks.COLUMN_REPETITION_METHOD.name;
                value = parser.getText();
                break;

            /**
             * Projects
             */

            case "project":
                name = Tasks.COLUMN_PROJECT.name;
                value = "1";
                break;

            case "singleton":
                parser.next();
                if (parser.getText().equals("true")) {
                    name = Tasks.COLUMN_TYPE.name;
                    value = "single action";
                }
                break;

            case "last-review":
                parser.next();
                name = Tasks.COLUMN_PROJECT_LAST_REVIEW.name;
                value = convertToMilliseconds(parser.getText());
                break;

            case "next-review":
                parser.next();
                name = Tasks.COLUMN_PROJECT_NEXT_REVIEW.name;
                value = convertToMilliseconds(parser.getText());
                break;

            case "review-interval":
                parser.next();
                name = Tasks.COLUMN_PROJECT_REPEAT_REVIEW.name;
                value = parser.getText();
                break;

            case "status":
                parser.next();
                name = Tasks.COLUMN_PROJECT_STATUS.name;
                value = parser.getText();
                break;
        }

        if (!name.isEmpty()) {
            values.put(name, value);
        }
    }

    /**
     * Given a string representing an ISO 8601 full datetime, convert it to a string in milliseconds
     * that represents the time since the UNIX epoch.
     */
    public static String convertToMilliseconds(String dateString) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            return String.valueOf(format.parse(dateString).getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }
    }
}
