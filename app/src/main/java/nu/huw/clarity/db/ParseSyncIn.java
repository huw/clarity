package nu.huw.clarity.db;

import android.content.ContentValues;
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

import nu.huw.clarity.db.DatabaseContract.*;

/**
 * Converts a contents.xml into a usable object
 */
public class ParseSyncIn {

    private static final String TAG = ParseSyncIn.class.getSimpleName();
    private static final String namespace = "http://www.omnigroup.com/namespace/OmniFocus/v1";

    private static final DatabaseHelper mDBHelper = new DatabaseHelper();

    public static void parse(InputStream input) {

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

        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        // Create a map of values to add in the new line
        ContentValues values = new ContentValues();

        String id = parser.getAttributeValue(null, "id");

        if (id != null) {
            values.put(Base.COLUMN_ID.name, id);

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

                        parseTag(parser, values);

                    } else if (next == XmlPullParser.END_TAG) {
                        depth--;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing XML", e);
            }
        }

        if (values.size() != 0) {
            db.insert(tableName, null, values);
        }

        Log.v(TAG, values.getAsString("id") + " parsed to " + tableName);
    }

    /**
     * Given a parser object, parses the current XML tag appropriately,
     * and adds the result to the passed ContentValues.
     */
    private static void parseTag(XmlPullParser parser, ContentValues values)
            throws IOException, XmlPullParserException {

        String name = "";
        String value = "";

        switch (parser.getName()) {

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
                value = parser.getText();
                break;

            /**
             * Perspectives/Settings
             * TODO: Actually parse
             */
            case "plist":
                break;

            /**
             * Tasks/Projects
             */
            case "order":
                parser.next();
                name = Tasks.COLUMN_TYPE.name;
                value = parser.getText();
                break;

            case "completed-by-children":
                parser.next();
                name = Tasks.COLUMN_COMPLETE_WITH_CHILDREN.name;
                value = String.valueOf(5 - parser.getText().length()); // I'm so sorry
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
                name = Tasks.COLUMN_REPETITION_RULE.name;
                value = parser.getText();
                break;

            case "repetition-method":
                name = Tasks.COLUMN_REPETITION_METHOD.name;
                value = parser.getText();
                break;
        }

        if (!name.isEmpty()) {
            values.put(name, value);
        }
    }

    /**
     * Given a string representing an ISO 8601 full datetime,
     * convert it to a string in milliseconds that represents
     * the time since the UNIX epoch.
     */
    public static String convertToMilliseconds(String dateString) {
        try {

            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            return String.valueOf(format.parse(dateString).getTime());

        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }
    }
}
