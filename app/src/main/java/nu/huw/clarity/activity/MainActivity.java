package nu.huw.clarity.activity;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import nu.huw.clarity.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AccountManager mAccountManager;

    /**
     * Okay, this is really bad practice but from what I've found it's
     * basically the only way forward.
     *
     * You can't use contexts in a base class, which is problematic
     * because I need to access an AccountManager to create HttpClients.
     * Instead, I've set a static variable here for the application's
     * context, so I can access AccountManager stuff. This is called
     * from any class which needs to get a basic context for the app.
     */
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        mAccountManager = AccountManager.get(getBaseContext());
        if (mAccountManager.getAccountsByType(this.getPackageName()).length == 0) {

            // If there are no syncing accounts, sign in
            startActivity(new Intent(this, LoginActivity.class));

        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        finish();
    }
}
