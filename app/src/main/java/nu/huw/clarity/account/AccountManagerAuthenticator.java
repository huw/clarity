package nu.huw.clarity.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import nu.huw.clarity.ui.activity.LoginActivity;

/**
 * AccountManagerAuthenticator is a class to authenticate and create accounts using the
 * AccountManager service. It doesn't implement many methods because it doesn't really need to.
 */
public class AccountManagerAuthenticator extends AbstractAccountAuthenticator {

  private final Context mContext;

  public AccountManagerAuthenticator(Context context) {

    super(context);

    mContext = context;
  }

  @Override
  public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
      String authTokenType, String[] requiredFeatures,
      Bundle options) throws NetworkErrorException {

    final Intent intent = new Intent(mContext, LoginActivity.class);
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

    final Bundle bundle = new Bundle();
    bundle.putParcelable(AccountManager.KEY_INTENT, intent);
    return bundle;
  }

  @Override
  public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options)
      throws NetworkErrorException {

    return null;
  }

  @Override
  public String getAuthTokenLabel(String authTokenType) {

    return null;
  }

  @Override
  public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {

    return null;
  }

  @Override
  public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
      String[] features) throws NetworkErrorException {

    return null;
  }

  @Override
  public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
      Bundle options) throws NetworkErrorException {

    return null;
  }

  @Override
  public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options)
      throws NetworkErrorException {

    return null;
  }
}