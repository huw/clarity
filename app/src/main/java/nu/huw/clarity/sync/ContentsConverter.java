package nu.huw.clarity.sync;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Converts a contents.xml into a usable object
 */
public class ContentsConverter {

    private static final String TAG = ContentsConverter.class.getSimpleName();
    private static final String namespace = "http://www.omnigroup.com/namespace/OmniFocus/v1";

    public static void parse(InputStream input) {

        try {

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);

            // Require that the first tag is an XML start (i.e. "<start>" and
            // not "</finish>" or "<self-closing />", that we belong to the
            // OmniFocus namespace (see the variable above), and that the tag
            // name is 'omnifocus'. A good start to reading these things.

            parser.require(XmlPullParser.START_TAG, namespace, "omnifocus");

            while (parser.next() != XmlPullParser.END_TAG) {

                if (parser.getEventType() != XmlPullParser.START_TAG) {

                    // We only want to deal with parent tags for now, and
                    // some self-closing child tags can accidentally match
                    // out parameters.

                    continue;
                }

                String tagName = parser.getName();

                Log.d(TAG, "Found a " + tagName);

                switch (tagName) {
                    case "attachment":
                        parseAttachment(parser);
                        break;
                    case "setting":
                        parseSetting(parser);
                        break;
                    case "context":
                        parseContext(parser);
                        break;
                    case "folder":
                        parseFolder(parser);
                        break;
                    case "task":
                        parseTask(parser);
                        break;
                    case "perspective":
                        parsePerspective(parser);
                        break;
                    default:
                        skipTag(parser);
                }

            }

            input.close();

        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error parsing XML", e);
        } catch (IOException e) {
            Log.e(TAG, "Streams unexpectedly crossed", e);
        }
    }

    /**
     * TODO: Parse attachments (complex!)
     */
    private static void parseAttachment(XmlPullParser parser) {
        Log.i(TAG, "Attachment element skipped, not being parsed yet");
    }

    /**
     * TODO: Parse settings
     */
    private static void parseSetting(XmlPullParser parser) {
        Log.i(TAG, "Settings element skipped, not being parsed yet");
    }

    private static void parseContext(XmlPullParser parser) {
        Log.i(TAG, "Context element parsed");
    }

    private static void parseFolder(XmlPullParser parser) {
        Log.i(TAG, "Folder element parsed");
    }

    private static void parseTask(XmlPullParser parser) {
        Log.i(TAG, "Task element parsed");
    }

    /**
     * TODO: Parse perspectives
     */
    private static void parsePerspective(XmlPullParser parser) {
        Log.i(TAG, "Perspective element skipped, not being parsed yet");
    }

    /**
     * Really cool subroutine for skipping a tag in the tree,
     * courtesy of Google. Basically, it skips tags until it
     * finds the next END_TAG, but it keeps track of the tag
     * depth to ensure that it completely skips the tag that
     * we don't want.
     *
     * See http://developer.android.com/training/basics/network-ops/xml.html#skip
     */
    private static void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {

        if (parser.getEventType() != XmlPullParser.START_TAG) {

            // Remind me one day to write an UnexpectedSpanishInquisitionException
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Given a string representing an ISO 8601 full datetime,
     * convert it to a usable Java object.
     */
    public Date convertToJavaDate(String dateString) {
        try {

            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            return format.parse(dateString);

        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }
    }
}
