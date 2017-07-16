package nu.huw.clarity.model

import android.net.Uri
import java.security.SecureRandom
import java.util.*
import java.util.regex.Pattern

object IDHelper {

    private val LENGTH = 11
    private val idPattern = Pattern.compile("[a-zA-Z0-9_-]{$LENGTH}")
    private val strictIDPattern = Pattern.compile("^[a-zA-Z0-9_-]{$LENGTH}$")
    private val historyFilePattern = Pattern.compile("^[0-9]{14}=[a-zA-Z0-9_-]{$LENGTH}\\+[a-zA-Z0-9_-]{$LENGTH}(\\+[a-zA-Z0-9_-]{$LENGTH})?\\.zip$")
    private val transactionFilePattern = Pattern.compile("^[0-9]{14}=[a-zA-Z0-9_-]{$LENGTH}\\+[a-zA-Z0-9_-]{$LENGTH}\\.zip$")
    private val mergeFilePattern = Pattern.compile("^[0-9]{14}=[a-zA-Z0-9_-]{$LENGTH}\\+[a-zA-Z0-9_-]{$LENGTH}\\+[a-zA-Z0-9_-]{$LENGTH}\\.zip$")
    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
    private val random by lazy { SecureRandom() }

    fun generate(): String {
        val sb = StringBuilder(LENGTH)
        for (i in 1..LENGTH) {
            sb.append(alphabet[random.nextInt(64)])
        }
        return sb.toString()
    }

    fun validate(string: String, strict: Boolean = true): Boolean {
        if (strict) {
            return strictIDPattern.matcher(string).matches()
        } else {
            return idPattern.matcher(string).matches()
        }
    }

    private fun getMatches(uri: Uri): List<ID> {
        val fileName = uri.lastPathSegment
        val matcher = idPattern.matcher(fileName)
        val matches = ArrayList<ID>()

        while (matcher.find()) {
            matches.add(ID(matcher.group()))
        }
        return matches
    }

    fun getDestination(uri: Uri): ID {
        val matches = getMatches(uri)
        return matches[matches.size - 1]
    }

    fun getOrigin(uri: Uri): ID {
        val matches = getMatches(uri)
        return matches[matches.size - 2] // Origin is always 2nd last, not first
    }

    fun getFirst(uri: Uri): ID? {
        val matches = getMatches(uri)
        if (isValidMergeFile(uri)) {
            return matches[matches.size - 3] // note: first match will be in the date string
        } else {
            return null
        }
    }

    fun getDate(uri: Uri): Int {
        return Integer.valueOf(uri.lastPathSegment.substring(0, 13))!!
    }

    fun isValidHistoryFile(uri: Uri): Boolean {
        return historyFilePattern.matcher(uri.lastPathSegment).matches()
    }

    fun isValidTransactionFile(uri: Uri): Boolean {
        return transactionFilePattern.matcher(uri.lastPathSegment).matches()
    }

    fun isValidMergeFile(uri: Uri): Boolean {
        return mergeFilePattern.matcher(uri.lastPathSegment).matches()
    }

    fun isValidClientFile(uri: Uri, clientID: String): Boolean {
        val clientFilePattern = Pattern.compile("^[0-9]{14}=$clientID\\.client$")
        return clientFilePattern.matcher(uri.lastPathSegment).matches()
    }

}