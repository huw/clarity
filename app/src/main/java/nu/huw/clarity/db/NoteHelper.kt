package nu.huw.clarity.db

import android.util.Log
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

object NoteHelper {

    private val TAG = NoteHelper::class.java.simpleName

    fun noteXMLtoString(noteXML: String): String {

        var string = ""

        try {

            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            val document = db.parse(InputSource(StringReader(noteXML)))

            document.documentElement.normalize()
            val nodes = document.firstChild.childNodes

            (0..nodes.length - 1)
                    .asSequence()
                    .map { nodes.item(it) }
                    .filter { it.nodeType == Node.ELEMENT_NODE && it.nodeName == "text" }
                    .forEach {
                        // Nodes at this level should always be <p> paragraph nodes
                        string = parseTextChildren(it, string)
                    }

        } catch (e: NullPointerException) {
            Log.e(TAG, "Error with the XML format", e)
        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "Error parsing XML", e)
        } catch (e: SAXException) {
            Log.e(TAG, "Error parsing XML", e)
        } catch (e: IOException) {
            Log.e(TAG, "Error parsing XML", e)
        }

        return string
    }

    private fun parseTextChildren(node: Node, _string: String): String {
        var string = _string
        val nodes = node.childNodes

        (0..nodes.length - 1)
                .asSequence()
                .map { nodes.item(it) }
                .filter { it.nodeType == Node.ELEMENT_NODE && it.nodeName == "p" }
                .forEach {
                    // Should also always be <p> nodes
                    if (!string.isEmpty()) {
                        string += "\n" // add a newline
                    }
                    string = parseParagraphChildren(it, string)
                }

        return string
    }

    private fun parseParagraphChildren(node: Node, _string: String): String {
        var string = _string
        val nodes = node.childNodes
        (0..nodes.length - 1)
                .asSequence()
                .map { nodes.item(it) }
                .filter { it.nodeType == Node.ELEMENT_NODE && it.nodeName == "run" }
                .forEach {
                    // Should also always be <run> nodes as children of a <p>

                    string = parseRunChildren(it, string)
                }
        return string
    }

    private fun parseRunChildren(node: Node, _string: String): String {
        var string = _string
        val nodes = node.childNodes
        (0..nodes.length - 1)
                .asSequence()
                .map { nodes.item(it) }
                .filter { it.nodeType == Node.ELEMENT_NODE && it.nodeName == "lit" }
                .forEach {
                    // Ignore <style> nodes, only <lit> nodes wanted

                    string = parseLitChildren(it, string)
                }
        return string
    }

    /**
     * Most lit function in the entire app
     */
    private fun parseLitChildren(node: Node, _string: String): String {
        var string = _string
        val nodes = node.childNodes
        (0..nodes.length - 1)
                .map { nodes.item(it) }
                .filter { it.nodeType.toInt() == Node.TEXT_NODE.toInt() }
                .forEach { string += it.nodeValue }
        return string
    }

}