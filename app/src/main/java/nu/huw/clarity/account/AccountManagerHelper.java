package nu.huw.clarity.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import nu.huw.clarity.R;
import nu.huw.clarity.ui.MainActivity;

/**
 * Convenience methods for the Account Manager
 *
 * TODO: Multiple accounts
 */
public class AccountManagerHelper {

    private static final String         TAG            = AccountManagerHelper.class.getSimpleName();
    private static final Context        context        = MainActivity.context;
    public static final  AccountManager accountManager = AccountManager.get(context);

    public static boolean doesAccountExist() {

        return accountManager.getAccountsByType(context.getString(R.string.account_type)).length !=
               0;
    }

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

    public static String getBaseURI() {

        return "https://" + getServerDomain();
    }

    /**
     * The Ofocus URI is the location of the OmniFocus.ofocus folder which contains all of the
     * useful syncing data. It is found at: `https://sync<your number>.omnigroup
     * .com/<username>/OmniFocus.ofocus/`
     */
    public static String getOfocusURI() {

        return getBaseURI() + "/" + getUsername() + "/OmniFocus.ofocus/";
    }

    public static void setUserData(String key, String value) {

        accountManager.setUserData(getAccount(), key, value);
    }
}
