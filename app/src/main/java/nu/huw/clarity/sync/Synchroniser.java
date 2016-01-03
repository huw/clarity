package nu.huw.clarity.sync;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import nu.huw.clarity.account.AccountHandler;

/**
 * This class handles all of the synchronisation methods.
 * A.K.A. Anything that needs an Apache library.
 */
public class Synchroniser {

    private static final String TAG = Synchroniser.class.getSimpleName();

    /**
     * This function will create a new HttpClient and automatically add the
     * account's credentials in an appropriate form.
     *
     * It's really useful, because we can just call `getHttpClient()` and
     * run WebDAV/HTTP methods on it straightaway, without having to
     * configure it.
     *
     * This code will be the appropriate setup code 100% of the time, AFTER
     * AN ACCOUNT HAS BEEN MADE.
     */
    public static HttpClient getHttpClient() {

        HttpClient client = new HttpClient();

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                AccountHandler.getUsername(),
                AccountHandler.getPassword()
        );
        AuthScope newHostScope = new AuthScope(
                AccountHandler.getServerDomain(),
                AccountHandler.getServerPort(),
                AuthScope.ANY_REALM
        );

        client.getState().setCredentials(newHostScope, credentials);
        client.getParams().setAuthenticationPreemptive(true);
        return client;
    }
}