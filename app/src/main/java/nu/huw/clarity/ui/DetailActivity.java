package nu.huw.clarity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;

public class DetailActivity extends AppCompatActivity {

    private Entry entry;

    private void setupToolbar(Toolbar toolbar, int themeID) {

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set the theme to the theme we just had back in MainActivity
        setTheme(themeID);

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

        setupToolbar((Toolbar) findViewById(R.id.toolbar_detail),
                     intent.getIntExtra("THEME_ID", R.style.AppTheme));

        // Set the default text in the text field to the thing's name, and set the hint text to
        // the thing's type
        TextInputEditText nameView = (TextInputEditText) findViewById(R.id.detail_name);
        TextInputLayout   tiLayout = (TextInputLayout) findViewById(R.id.detail_name_layout);

        if (nameView != null && tiLayout != null) {
            nameView.setText(entry.name);

            int hintID = R.string.prompt_name;

            if (entry instanceof Task) {
                if (((Task) entry).project) {
                    hintID = R.string.project;
                } else {
                    hintID = R.string.task;
                }
            } else if (entry instanceof Context) {
                hintID = R.string.context;
            } else if (entry instanceof Folder) {
                hintID = R.string.folder;
            }

            nameView.setHint(hintID);
            tiLayout.setHint(getResources().getString(hintID));
        }
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
