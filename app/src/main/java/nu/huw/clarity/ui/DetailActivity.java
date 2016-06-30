package nu.huw.clarity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Entry;

public class DetailActivity extends AppCompatActivity {

    private Entry entry;

    private void setupToolbar(Toolbar toolbar, int menuID) {

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switch (menuID) {
            case R.id.nav_forecast:
                setTheme(R.style.AppTheme_Red);
                break;
            case R.id.nav_inbox:
                setTheme(R.style.AppTheme_BlueGrey);
                break;
            case R.id.nav_projects:
                setTheme(R.style.AppTheme_Blue);
                break;
            case R.id.nav_contexts:
                setTheme(R.style.AppTheme);
                break;
            case R.id.nav_flagged:
                setTheme(R.style.AppTheme_Orange);
                break;
            case R.id.nav_nearby:
                setTheme(R.style.AppTheme_Green);
                break;
        }

        AppBarLayout   abLayout   = (AppBarLayout) findViewById(R.id.barlayout_detail);
        RelativeLayout detailView = (RelativeLayout) findViewById(R.id.detail_view);

        // Set background colours depending on the view we just came from
        if (abLayout != null && detailView != null) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            abLayout.setBackgroundColor(typedValue.data);
            detailView.setBackgroundColor(typedValue.data);
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        entry = intent.getParcelableExtra("ENTRY");

        setupToolbar((Toolbar) findViewById(R.id.toolbar_detail), intent.getIntExtra("MENU_ID", 0));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
