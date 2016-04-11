package nu.huw.clarity.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountHelper;
import nu.huw.clarity.ui.fragments.EntryListFragment;

public class MainActivity extends AppCompatActivity implements EntryListFragment.ListInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

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

        if (!AccountHelper.doesAccountExist()) {

            // If there are no syncing accounts, sign in
            startActivity(new Intent(this, LoginActivity.class));

        } else {

            if (savedInstanceState != null) {
                return;
            }

            EntryListFragment listFragment = new EntryListFragment();

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, listFragment)
                    .commit();
        }
    }

    @Override
    public void onListInteraction() {}
}
