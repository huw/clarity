package nu.huw.clarity.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import nu.huw.clarity.R;

/**
 * Convenience methods for the Account Manager
 *
 * TODO: Multiple accounts
 */
public class AccountManagerHelper {

    private static final String         TAG            = AccountManagerHelper.class.getSimpleName();
    private Context        context;
    private AccountManager accountManager;

    public AccountManagerHelper(Context context) {

        this.context = context;
        this.accountManager = AccountManager.get(context);
    }

    public boolean doesAccountExist() {

        return accountManager.getAccountsByType(context.getString(R.string.account_type)).length !=
               0;
    }

    public Account getAccount() {

        return accountManager.getAccountsByType(context.getString(R.string.account_type))[0];
    }

    public String getUsername() {

        return getAccount().name;
    }

    public String getPassword() {

        return accountManager.getPassword(getAccount());
    }

    public String getServerDomain() {

        return accountManager.getUserData(getAccount(), "SERVER_DOMAIN");
    }

    public int getServerPort() {

        return Integer.parseInt(accountManager.getUserData(getAccount(), "SERVER_PORT"));
    }

    public String getBaseURI() {

        return "https://" + getServerDomain();
    }

    /**
     * The Ofocus URI is the location of the OmniFocus.ofocus folder which contains all of the
     * useful syncing data. It is found at: `https://sync<your number>.omnigroup
     * .com/<username>/OmniFocus.ofocus/`
     */
    public String getOfocusURI() {

        return getBaseURI() + "/" + getUsername() + "/OmniFocus.ofocus/";
    }

    public void setUserData(String key, String value) {

        accountManager.setUserData(getAccount(), key, value);
    }
}
