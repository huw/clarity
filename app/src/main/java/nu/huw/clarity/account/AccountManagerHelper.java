package nu.huw.clarity.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import nu.huw.clarity.R;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * Convenience methods for the Account Manager
 */
public class AccountManagerHelper {

  public static final String TYPE_OMNISYNC = "OMNISYNC";
  public static final String TYPE_OTHERSYNC = "OTHERSYNC";

  private static final String TAG = AccountManagerHelper.class.getSimpleName();
  private Context androidContext;
  private AccountManager accountManager;

  public AccountManagerHelper(@NonNull Context androidContext) {
    this.androidContext = androidContext;
    this.accountManager = AccountManager.get(androidContext);
  }

  public AccountManager getAccountManager() {
    return accountManager;
  }

  public boolean accountExists() {
    return accountManager.getAccountsByType(androidContext.getString(R.string.account_type)).length
        != 0;
  }

  @NonNull
  public Account getAccount() {
    Account[] accounts = accountManager
        .getAccountsByType(androidContext.getString(R.string.account_type));
    if (accounts.length > 0) {
      return accounts[0];
    }
    throw new NullPointerException("No account found");
  }

  @NonNull
  public String getUsername() {
    return getAccount().name;
  }

  @NonNull
  public String getPassword() {
    return accountManager.getPassword(getAccount());
  }

  @NonNull
  public String getType() {
    String type = accountManager.getUserData(getAccount(), "TYPE");
    if (type == null || !(type.equals(TYPE_OMNISYNC) || type.equals(TYPE_OTHERSYNC))) {
      type = TYPE_OMNISYNC;
    }
    return type;
  }

  @NonNull
  public String getPassphrase() {
    return accountManager.getUserData(getAccount(), "PASSPHRASE");
  }

  @Nullable
  public Uri getServerUri() {
    String uriString = accountManager.getUserData(getAccount(), "URI");
    if (uriString != null) {
      return Uri.parse(uriString);
    } else {
      return null;
    }
  }

  @NonNull
  public Uri getOfocusUri() {
    Uri uri = Uri.parse(accountManager.getUserData(getAccount(), "URI"));
    return uri.buildUpon().appendPath("OmniFocus.ofocus").build();
  }

  public void setUserData(@NonNull String key, @Nullable String value) {
    accountManager.setUserData(getAccount(), key, value);
  }

  /**
   * Gets a valid instance of an HttpClient, already configured with the user's data
   */
  @NonNull
  public HttpClient getHttpClient() {

    HttpClient client = new HttpClient();
    Uri serverUri = getServerUri();

    if (serverUri != null) {
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(),
          getPassword());
      AuthScope newHostScope = new AuthScope(serverUri.getHost(), serverUri.getPort(),
          AuthScope.ANY_REALM);

      client.getState().setCredentials(newHostScope, credentials);
      client.getParams().setAuthenticationPreemptive(true);
    }

    return client;
  }
}
