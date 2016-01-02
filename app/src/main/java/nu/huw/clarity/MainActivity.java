package nu.huw.clarity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountManager = AccountManager.get(getBaseContext());
        if (mAccountManager.getAccountsByType(this.getPackageName()).length == 0) {

            // If there are no syncing accounts, sign in
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

}
