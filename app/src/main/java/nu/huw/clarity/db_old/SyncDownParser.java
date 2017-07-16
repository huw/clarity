package nu.huw.clarity.db_old;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nu.huw.clarity.db_old.DatabaseContract.Attachments;
import nu.huw.clarity.db_old.DatabaseContract.Base;
import nu.huw.clarity.db_old.DatabaseContract.Contexts;
import nu.huw.clarity.db_old.DatabaseContract.Folders;
import nu.huw.clarity.db_old.DatabaseContract.Perspectives;
import nu.huw.clarity.db_old.DatabaseContract.Settings;
import nu.huw.clarity.db_old.DatabaseContract.Tasks;
import org.threeten.bp.Duration;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Converts a contents.xml into a usable object
 */
public class SyncDownParser {

  private static final String TAG = SyncDownParser.class.getSimpleName();
  private final DatabaseHelper mDBHelper;
  private Context androidContext;
  private Transformer transformer = null;

  public SyncDownParser(Context context) {

    androidContext = context;
    mDBHelper = new DatabaseHelper(context);

    try {
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private String stringOf(Node node) {

    if (node == null) {
      throw new IllegalArgumentException("Node is null");
    }

    try {
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(node), new StreamResult(writer));
      return writer.toString();
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  public void parse(InputStream input) {

    try {

      // Load XML into DOM

      Document document;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();

      document = db.parse(input);

      document.getDocumentElement().normalize();
      NodeList nodes = document.getFirstChild().getChildNodes();

      // For each element in DOM

      for (int i = 0; i < nodes.getLength(); i++) {

        Node node = nodes.item(i);

        if (node.getNodeType() == Node.ELEMENT_NODE) {

          String tableName;
          String tagName = node.getNodeName();
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

          parseEntry(tableName, node);
        }
      }

      input.close();
    } catch (ParserConfigurationException | SAXException e) {
      Log.e(TAG, "Error parsing XML", e);
    } catch (IOException e) {
      Log.e(TAG, "Streams unexpectedly crossed", e);
    }
  }

  private void parseEntry(String tableName, Node node) {

    // Create a map of values to add in the new line
    ContentValues values = new ContentValues();

    NamedNodeMap attributes = node.getAttributes();

    Node idNode = attributes.getNamedItem("id");
    Node opNode = attributes.getNamedItem("op");
    String id, operation = "";

    if (idNode == null) {
      return;
    }
    if (opNode != null) {
      operation = attributes.getNamedItem("op").getNodeValue();
    }

    id = attributes.getNamedItem("id").getNodeValue();
    NodeList children = node.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {

      Node child = children.item(i);

      parseTag(child, values, tableName, id);
    }

    switch (operation) {
      case "reference":

        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        String[] columns = {Base.COLUMN_ID};
        String selection = Base.COLUMN_ID + "=?";
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
  private void parseTag(Node node, ContentValues values, String table, String id) {

    String name = "";
    String value = node.getTextContent();

    if (value == null) {
      value = "";
    }

    NamedNodeMap attributes = node.getAttributes();

    switch (node.getNodeName()) {

      /*
       * Parents
       */
      case "context":
        if (table.equals(Tasks.TABLE_NAME)) {
          name = Tasks.COLUMN_CONTEXT;
          Node attr = attributes.getNamedItem("idref");
          if (attr != null) {
            value = attr.getTextContent();
          }
          break;
        }
        // else, fall through to adding a parent ID

      case "folder":
      case "task":
        name = DatabaseContract.Entries.COLUMN_PARENT_ID;
        Node attr = attributes.getNamedItem("idref");
        if (attr != null) {
          value = attr.getTextContent();
        }
        break;

      /*
       * Base
       */
      case "added":
        name = Base.COLUMN_DATE_ADDED;
        break;

      case "modified":
        name = Base.COLUMN_DATE_MODIFIED;
        break;

      /*
       * Entry
       */
      case "name":
        name = DatabaseContract.Entries.COLUMN_NAME;

        if (table.equals(Attachments.TABLE_NAME)) {

          // Save file path as well

          File file = new File(androidContext.getFilesDir(), "data/" + id + "/" + value);
          if (file.exists()) {
            values.put(Attachments.COLUMN_PATH, file.getPath());
          }
        }

        break;

      case "rank":
        name = DatabaseContract.Entries.COLUMN_RANK;
        break;

      /*
       * Attachment
       */
      case "preview-image":
        name = Attachments.COLUMN_PNG_PREVIEW;
        break;

      /*
       * Context
       */
      case "location":

        Node locationNameAttr = attributes.getNamedItem("name");
        Node altitudeAttr = attributes.getNamedItem("altitude");
        Node latitudeAttr = attributes.getNamedItem("latitude");
        Node longitudeAttr = attributes.getNamedItem("longitude");
        Node radiusAttr = attributes.getNamedItem("radius");

        String locationName = "", altitude = "", latitude = "", longitude = "", radius = "";

        if (locationNameAttr != null) {
          locationName = locationNameAttr.getTextContent();
        }

        if (altitudeAttr != null) {
          altitude = altitudeAttr.getTextContent();
        }

        if (latitudeAttr != null) {
          latitude = latitudeAttr.getTextContent();
        }

        if (longitudeAttr != null) {
          longitude = longitudeAttr.getTextContent();
        }

        if (radiusAttr != null) {
          radius = radiusAttr.getTextContent();
        }

        values.put(Contexts.COLUMN_LOCATION_NAME, locationName);
        values.put(Contexts.COLUMN_ALTITUDE, altitude);
        values.put(Contexts.COLUMN_LATITUDE, latitude);
        values.put(Contexts.COLUMN_LONGITUDE, longitude);
        values.put(Contexts.COLUMN_RADIUS, radius);
        break;

      case "prohibits-next-action":
        name = Contexts.COLUMN_ON_HOLD;
        value = value.equals("true") ? "1" : "0";
        break;

      /*
       * Perspectives/Settings
       */
      case "plist":
        if (table.equals(Perspectives.TABLE_NAME)) {

          // Parse as perspective

          name = Perspectives.COLUMN_VALUE;

          try {
            byte[] tagBytes = stringOf(node).getBytes();

            NSDictionary plist =
                (NSDictionary) PropertyListParser.parse(tagBytes);
            NSDictionary viewState = (NSDictionary) plist.get("viewState");
            NSDictionary viewModeState = (NSDictionary) viewState.get("viewModeState");
            String viewMode =
                ((NSString) viewState.get("viewMode")).getContent();
            NSDictionary data = (NSDictionary) viewModeState.get(viewMode);

            String perspectiveName = ((NSString) plist.get("name")).getContent();
            String icon =
                ((NSString) plist.get("iconNameInBundle")).getContent();
            String group = ((NSString) data.get("collation")).getContent();
            String sort = ((NSString) data.get("sort")).getContent();

            // These three fields could be null, so check for that

            NSString filterDurationData = ((NSString) data.get("actionDurationFilter"));
            NSString filterFlaggedData = ((NSString) data.get("actionFlaggedFilter"));
            NSString filterStatusData =
                ((NSString) data.get("actionCompletionFilter"));

            if (filterDurationData != null) {
              String filterDuration = filterDurationData.getContent();
              values.put(Perspectives.COLUMN_FILTER_DURATION, filterDuration);
            }

            if (filterFlaggedData != null) {
              String filterFlagged = filterFlaggedData.getContent();
              values.put(Perspectives.COLUMN_FILTER_FLAGGED, filterFlagged);
            }

            if (filterStatusData != null) {
              String filterStatus = filterStatusData.getContent();
              values.put(Perspectives.COLUMN_FILTER_STATUS, filterStatus);
            }

            values.put(Perspectives.COLUMN_NAME, perspectiveName);
            values.put(Perspectives.COLUMN_ICON, icon);
            values.put(Perspectives.COLUMN_VIEW_MODE, viewMode);
            values.put(Perspectives.COLUMN_GROUP, group);
            values.put(Perspectives.COLUMN_SORT, sort);
          } catch (IOException | ParseException | SAXException | ParserConfigurationException e) {
            Log.e(TAG, "Error parsing perspective", e);
          } catch (PropertyListFormatException e) {
            Log.e(TAG, "Plist is malformed", e);
          }
        } else if (table.equals(Settings.TABLE_NAME)) {

          // Parse as setting
          // Note that all the other settings pretty much read in fine as strings

          name = Settings.COLUMN_VALUE;

          try {
            byte[] tagBytes = stringOf(node).getBytes();

            switch (id) {
              case "cPIrzdPU37-": // No idea
              case "PerspectiveOrder_v2": // Perspective order

                NSObject[] array = ((NSArray) PropertyListParser.parse(tagBytes)).getArray();
                value = "";

                for (int i = 0; i < array.length; i++) {

                  // Get the value of the string from the NSObject
                  value += ((NSString) array[i]).getContent();

                  // Add a comma delimeter, unless this is the last in the array
                  if (i < array.length - 1) {
                    value += ",";
                  }
                }

                break;
              case "DueSoonInterval":

                // Convert the number of seconds into a Duration object
                // which we can use to get a proper ISO duration

                value = Duration.ofSeconds(Long.valueOf(value)).toString();
            }
          } catch (IOException | ParseException | SAXException | ParserConfigurationException e) {
            Log.e(TAG, "Error parsing setting", e);
          } catch (PropertyListFormatException e) {
            Log.e(TAG, "Plist is malformed", e);
          }
        }

        break;

      /*
       * Tasks
       */
      case "order":

        // The task type is a tricky one, because it can be either 'sequential',
        // 'parallel' or 'single action', but 'single action' is only applicable
        // to projects and comes from a different tag (see "singleton" below).
        // As the "singleton" tag will always come first, we can test to see if
        // we've already added the 'type' column, and only proceed if we haven't.

        if (!values.containsKey(Tasks.COLUMN_TYPE)) {
          name = Tasks.COLUMN_TYPE;
        }
        break;

      case "completed-by-children":
        name = Tasks.COLUMN_COMPLETE_WITH_CHILDREN;
        value = value.equals("true") ? "1" : "0";
        break;

      case "start":
        name = Tasks.COLUMN_DATE_DEFER;
        break;

      case "due":
        name = Tasks.COLUMN_DATE_DUE;
        break;

      case "completed":
        name = Tasks.COLUMN_DATE_COMPLETED;
        break;

      case "estimated-minutes":
        name = Tasks.COLUMN_ESTIMATED_TIME;
        if (!value.isEmpty()) {
          value = Duration.ofMinutes(Long.valueOf(value)).toString();
        }
        break;

      case "flagged":
        name = Tasks.COLUMN_FLAGGED;
        value = value.equals("true") ? "1" : "0";
        break;

      case "inbox":
        name = Tasks.COLUMN_INBOX;
        value = value.equals("false") ? "0" : "1";
        break;

      case "note":
        if (table.equals(Tasks.TABLE_NAME) && node.hasChildNodes()) {
          name = Tasks.COLUMN_NOTE_XML;
          value = stringOf(node);
        }
        break;

      case "repetition-rule":
        name = Tasks.COLUMN_REPETITION_RULE;
        break;

      case "repetition-method":
        name = Tasks.COLUMN_REPETITION_METHOD;
        break;

      /*
       * Projects
       */
      case "project":
        if (node.hasChildNodes()) {
          name = Tasks.COLUMN_PROJECT;
          value = "1";
        }

        // GO DEEPER

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

          Node child = children.item(i);

          parseTag(child, values, table, id);
        }

        break;

      case "singleton":
        if (value.equals("true")) {
          name = Tasks.COLUMN_TYPE;
          value = "single action";
        }
        break;

      case "last-review":
        name = Tasks.COLUMN_PROJECT_LAST_REVIEW;
        break;

      case "next-review":
        name = Tasks.COLUMN_PROJECT_NEXT_REVIEW;
        break;

      case "review-interval":
        name = Tasks.COLUMN_PROJECT_REPEAT_REVIEW;
        break;

      case "status":
        name = Tasks.COLUMN_PROJECT_STATUS;
        break;
    }

    if (!name.isEmpty() && !value.isEmpty()) {
      values.put(name, value);
    }
  }
}
