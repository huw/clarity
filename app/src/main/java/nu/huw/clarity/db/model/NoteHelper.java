package nu.huw.clarity.db.model;

import android.util.Log;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NoteHelper {

  private static final String TAG = NoteHelper.class.getSimpleName();

  public static String noteXMLtoString(String noteXML) {

    String string = "";

    try {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse(new InputSource(new StringReader(noteXML)));

      document.getDocumentElement().normalize();
      NodeList nodes = document.getFirstChild().getChildNodes();

      for (int i = 0; i < nodes.getLength(); i++) {

        Node node = nodes.item(i);

        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("text")) {

          // Nodes at this level should always be <p> paragraph nodes

          string = parseTextChildren(node, string);
        }
      }

    } catch (NullPointerException e) {
      Log.e(TAG, "Error with the XML format", e);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      Log.e(TAG, "Error parsing XML", e);
    }

    return string;
  }

  private static String parseTextChildren(Node node, String string) {
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);

      if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("p")) {

        // Should also always be <p> nodes

        if (!string.isEmpty()) {
          string += "\n"; // add a newline
        }

        string = parseParagraphChildren(childNode, string);
      }
    }
    return string;
  }

  private static String parseParagraphChildren(Node node, String string) {
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);

      if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("run")) {

        // Should also always be <run> nodes as children of a <p>

        string = parseRunChildren(childNode, string);

      }
    }
    return string;
  }

  private static String parseRunChildren(Node node, String string) {
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("lit")) {

        // Ignore <style> nodes, only <lit> nodes wanted

        string = parseLitChildren(childNode, string);

      }
    }
    return string;
  }

  /**
   * Most lit function in the entire app
   */
  private static String parseLitChildren(Node node, String string) {
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      int nodeType = childNode.getNodeType();

      if (nodeType == Node.TEXT_NODE) {
        string += childNode.getNodeValue();
      }
    }
    return string;
  }

}
