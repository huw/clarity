package nu.huw.clarity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * OmniSyncAuthenticatorService is a class to implement a service
 * for AccountManager to deal with the Omni Sync Server. It works
 * because OmniSyncAuthenticatorService inherits `.getIBinder()`
 * from AbstractAuthenticatorService.
 */
public class OmniSyncAuthenticatorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        OmniSyncAuthenticator authenticator = new OmniSyncAuthenticator(this);
        return authenticator.getIBinder();
    }

}