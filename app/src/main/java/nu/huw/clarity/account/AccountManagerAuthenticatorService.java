package nu.huw.clarity.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * AccountManagerAuthenticatorService is a class to implement a service for AccountManager to deal
 * with the Omni Sync Server. It works because AccountManagerAuthenticatorService inherits
 * `.getIBinder()` from AbstractAuthenticatorService.
 */
public class AccountManagerAuthenticatorService extends Service {

  @Override
  public IBinder onBind(Intent intent) {

    AccountManagerAuthenticator authenticator = new AccountManagerAuthenticator(this);
    return authenticator.getIBinder();
  }
}