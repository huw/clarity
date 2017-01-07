package nu.huw.clarity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragment.DetailFragment;
import nu.huw.clarity.ui.misc.CheckCircle;

public class DetailActivity extends AppCompatActivity
        implements DetailFragment.OnDetailInteractionListener {

    private int themeID;

    private void setupToolbar(Toolbar toolbar, int themeID) {

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set the theme to the theme we just had back in MainActivity
        this.themeID = themeID;
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
        Entry  entry;

        if (intent.hasExtra("ENTRY")) {

            // If we've passed an entry already, then just pick it up

            entry = intent.getParcelableExtra("ENTRY");
        } else {
            throw new IllegalArgumentException("Intent must contain 'ENTRY'");
        }

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
                if (((Task) entry).isProject) {
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

            Fragment detailFragment = DetailFragment.newInstance(entry);

            if (detailFragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.detail_fragment_container, detailFragment);
                ft.commit();
            }
        }

        CheckCircle checkCircleView = (CheckCircle) findViewById(R.id.checkcircle);
        if (entry instanceof Task) {
            Task task = (Task) entry;

            // Check circle
            // Available tasks can have a flag, but they can't have colorised overdue/due soon
            // circles because the user doesn't want to start them yet.

            checkCircleView.setChecked(task.dateCompleted != null);
            checkCircleView.setFlagged(task.flagged && themeID != R.style.AppTheme_Orange);

            // Also, if the colour is going to clash with the background, then don't set the
            // attribute. This applies to the red clashing with forecast, and orange clashing
            // with flagged (in both cases the intended colour should be pretty obvious)

            if (task.isAvailable()) {
                checkCircleView.setOverdue(task.overdue && themeID != R.style.AppTheme_Red);
                checkCircleView.setDueSoon(task.dueSoon);
            } else {
                checkCircleView.setOverdue(false);
                checkCircleView.setDueSoon(false);
            }
        } else {
            // Remove the check circle
            checkCircleView.setVisibility(View.GONE);
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

    @Override public void onContextClick(String id) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ENTRY_ID", id);
        intent.putExtra("TABLE_NAME", DatabaseContract.Contexts.TABLE_NAME);
        intent.putExtra("THEME_ID", themeID);
        startActivity(intent);

        /*Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ENTRY_ID", id);
        intent.putExtra("TABLE_NAME", DatabaseContract.Contexts.TABLE_NAME);
        intent.putExtra("THEME_ID", themeID);
        startActivity(intent);*/
    }

    @Override public void onProjectClick(String id) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ENTRY_ID", id);
        intent.putExtra("TABLE_NAME", DatabaseContract.Tasks.TABLE_NAME);
        intent.putExtra("THEME_ID", themeID);
        startActivity(intent);

        /*Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ENTRY_ID", id);
        intent.putExtra("TABLE_NAME", DatabaseContract.Tasks.TABLE_NAME);
        intent.putExtra("THEME_ID", themeID);
        startActivity(intent);*/
    }
}
