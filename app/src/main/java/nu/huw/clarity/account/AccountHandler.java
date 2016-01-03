package nu.huw.clarity.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import nu.huw.clarity.R;
import nu.huw.clarity.activity.MainActivity;

/**
 * Used for easy access to account data.
 *
 * TODO: Multiple accounts
 */
public class AccountHandler {

    private static final String TAG = AccountHandler.class.getSimpleName();
    private static final Context context = MainActivity.context;

    public static final AccountManager accountManager = AccountManager.get(context);

    public static Account getAccount() {
        return accountManager.getAccountsByType(context.getString(R.string.account_type))[0];
    }

    public static String getUsername() {
        return getAccount().name;
    }

    public static String getPassword() {
        return accountManager.getPassword(getAccount());
    }

    public static String getServerDomain() {
        return accountManager.getUserData(getAccount(), "SERVER_DOMAIN");
    }

    public static int getServerPort() {
        return Integer.parseInt(accountManager.getUserData(getAccount(), "SERVER_PORT"));
    }

    public static String getURI() {
        return "https://" + getServerDomain() + "/" + getUsername() + "/OmniFocus.ofocus/";
    }
}
