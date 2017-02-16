package nu.huw.clarity.model;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import com.dd.plist.NSDate;
import com.dd.plist.NSDictionary;
import java.text.ParseException;
import java.util.UUID;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

public class Client {

  private static final String TAG = Client.class.getSimpleName();
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern("yyyyMMddHHmmss");
  public final String filename;
  private final int CurrentFrameworkVersion;
  private final int HardwareCPUCount;
  private final int[] HardwareCPUType;
  private final String HardwareCPUTypeDescription;
  private final String HardwareCPUTypeName;
  private final String HardwareModel;
  private final double OFMSyncClientModelVersion;
  private final String[] OFMSyncClientSupportedCapabilities;
  private final String OSVersion;
  private final String OSVersionNumber;
  private final String bundleIdentifier;
  private final String bundleVersion;
  private final String clientIdentifier;
  private final String hostID;
  private final Instant lastSyncDate;
  private final String name;
  private final Instant registrationDate;
  private final String[] tailIdentifiers;

  public Client(android.content.Context androidContext) {

    SharedPreferences sharedPreferences = androidContext.getSharedPreferences("PREFS",
        android.content.Context.MODE_PRIVATE);

    this.CurrentFrameworkVersion = 2;
    this.HardwareCPUCount = Runtime.getRuntime().availableProcessors();
    this.HardwareCPUType = new int[]{0, 0};
    this.HardwareCPUTypeDescription = System.getProperty("os.arch");
    this.HardwareCPUTypeName = this.HardwareCPUTypeDescription;
    this.HardwareModel = Build.MODEL;
    this.OFMSyncClientModelVersion = 4.5;
    this.OFMSyncClientSupportedCapabilities = new String[]{"delta_transactions"};
    this.OSVersion = VERSION.INCREMENTAL;
    this.OSVersionNumber = VERSION.RELEASE;
    this.bundleIdentifier = androidContext.getPackageName();

    String bundleVersion;
    try {

      // If we can't get the version number for the package, then return a generic version number

      PackageInfo packageInfo = androidContext.getPackageManager()
          .getPackageInfo(this.bundleIdentifier, 0);
      bundleVersion = packageInfo.versionName;
    } catch (NameNotFoundException e) {
      bundleVersion = "1.0";
    }
    this.bundleVersion = bundleVersion;

    String clientIdentifier;
    if (sharedPreferences.contains("CLIENT_IDENTIFIER")) {

      // If we can't get the client identifier, generate a new one and save it

      clientIdentifier = sharedPreferences.getString("CLIENT_IDENTIFIER", null);
    } else {
      clientIdentifier = ID.generate();
      sharedPreferences.edit().putString("CLIENT_IDENTIFIER", clientIdentifier).apply();
    }
    this.clientIdentifier = clientIdentifier;

    String hostID;
    if (sharedPreferences.contains("HOST_ID")) {

      // The hostID field is just another UUID. So if we can't find it, generate and save a new one.

      hostID = sharedPreferences.getString("HOST_ID", null);
    } else {
      hostID = UUID.randomUUID().toString().toUpperCase();
      sharedPreferences.edit().putString("HOST_ID", hostID).apply();
    }
    this.hostID = hostID;

    Instant lastSyncDate;
    if (sharedPreferences.contains("LAST_SYNC_DATE")) {

      // If we don't have a last sync date (we should), then save right now

      lastSyncDate = Instant.parse(sharedPreferences.getString("LAST_SYNC_DATE", null));
    } else {
      lastSyncDate = Instant.now();
      sharedPreferences.edit().putString("LAST_SYNC_DATE", lastSyncDate.toString()).apply();
    }
    this.lastSyncDate = lastSyncDate;

    this.name = BluetoothAdapter.getDefaultAdapter().getName();

    Instant registrationDate;
    if (sharedPreferences.contains("REGISTRATION_DATE")) {
      registrationDate = Instant.parse(sharedPreferences.getString("REGISTRATION_DATE", null));
    } else {
      registrationDate = Instant.now();
      sharedPreferences.edit().putString("REGISTRATION_DATE", registrationDate.toString()).apply();
    }
    this.registrationDate = registrationDate;

    this.tailIdentifiers = new String[]{sharedPreferences.getString("TAIL_IDENTIFIER", null)};

    // Generate filename

    LocalDateTime date = LocalDateTime.ofInstant(this.lastSyncDate, ZoneOffset.UTC);
    this.filename = date.format(dateTimeFormatter) + "=" + this.clientIdentifier + ".client";
  }

  /**
   * Seralises the object to an XML property list
   */
  public String toPlistString() {

    NSDictionary dict = new NSDictionary();

    dict.put("CurrentFrameworkVersion", String.valueOf(CurrentFrameworkVersion));
    dict.put("HardwareCPUCount", String.valueOf(HardwareCPUCount));
    dict.put("HardwareCPUType", HardwareCPUType[0] + "," + HardwareCPUType[1]);
    dict.put("HardwareCPUTypeDescription", HardwareCPUTypeDescription);
    dict.put("HardwareCPUTypeName", HardwareCPUTypeName);
    dict.put("HardwareModel", HardwareModel);
    dict.put("OFMSyncClientModelVersion", String.valueOf(OFMSyncClientModelVersion));
    dict.put("OFMSyncClientSupportedCapabilities", OFMSyncClientSupportedCapabilities);
    dict.put("OSVersion", OSVersion);
    dict.put("OSVersionNumber", OSVersionNumber);
    dict.put("bundleIdentifier", bundleIdentifier);
    dict.put("bundleVersion", bundleVersion);
    dict.put("clientIdentifier", clientIdentifier);
    dict.put("hostID", hostID);

    try {

      // Parse our date into an NSDate so it shows up correctly on the plist
      // To do this, we have to truncate to seconds so it fits the expected ISO8601 format

      dict.put("lastSyncDate", new NSDate(lastSyncDate.truncatedTo(ChronoUnit.SECONDS).toString()));
    } catch (ParseException e) {
      Log.e(TAG, "Error parsing the last sync date " + lastSyncDate.toString(), e);
    }

    dict.put("name", name);

    try {
      dict.put("registrationDate",
          new NSDate(registrationDate.truncatedTo(ChronoUnit.SECONDS).toString()));
    } catch (ParseException e) {
      Log.e(TAG, "Error parsing the registration sync date " + registrationDate.toString());
    }

    dict.put("tailIdentifiers", tailIdentifiers);

    return dict.toXMLPropertyList();
  }

  @Override
  public String toString() {
    return toPlistString();
  }
}
