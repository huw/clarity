package nu.huw.clarity.model;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ID {

  private static final String TAG = ID.class.getSimpleName();
  private static final Pattern idPattern = Pattern.compile("[a-zA-Z0-9_-]{11}");
  private static final Pattern strictIDPattern = Pattern.compile("^[a-zA-Z0-9_-]{11}$");
  private static final Pattern filePattern = Pattern
      .compile("^[0-9]{14}=[a-zA-Z0-9_-]{11}\\+[a-zA-Z0-9_-]{11}(\\+[a-zA-Z0-9_-]{11})?\\.zip$");
  private static final Pattern transactionFilePattern = Pattern
      .compile("^[0-9]{14}=[a-zA-Z0-9_-]{11}\\+[a-zA-Z0-9_-]{11}\\.zip$");
  private static final Pattern mergeFilePattern = Pattern
      .compile("^[0-9]{14}=[a-zA-Z0-9_-]{11}\\+[a-zA-Z0-9_-]{11}\\+[a-zA-Z0-9_-]{11}\\.zip$");

  public static boolean validate(String string) {
    return validate(string, true);
  }

  public static boolean validate(String string, boolean strict) {
    if (strict) {
      return strictIDPattern.matcher(string).matches();
    } else {
      return idPattern.matcher(string).matches();
    }
  }

  private static List<String> getMatches(Uri uri) {
    String fileName = uri.getLastPathSegment();
    Matcher matcher = idPattern.matcher(fileName);
    List<String> matches = new ArrayList<>();

    while (matcher.find()) {
      matches.add(matcher.group());
    }
    return matches;
  }

  public static String getDestination(Uri uri) {
    List<String> matches = getMatches(uri);
    return matches.get(matches.size() - 1);
  }

  public static String getOrigin(Uri uri) {
    List<String> matches = getMatches(uri);
    return matches.get(matches.size() - 2); // Origin is always 2nd last, not first
  }

  public static String getFirst(Uri uri) {
    List<String> matches = getMatches(uri);
    if (isMergeFile(uri)) {
      return matches.get(matches.size() - 3); // note: first match will be in the date string
    } else {
      return null;
    }
  }

  public static int getDate(Uri uri) {
    return Integer.valueOf(uri.getLastPathSegment().substring(0, 13));
  }

  public static boolean isValidFile(Uri uri) {
    return filePattern.matcher(uri.getLastPathSegment()).matches();
  }

  public static boolean isTransactionFile(Uri uri) {
    return transactionFilePattern.matcher(uri.getLastPathSegment()).matches();
  }

  public static boolean isMergeFile(Uri uri) {
    return mergeFilePattern.matcher(uri.getLastPathSegment()).matches();
  }

}
