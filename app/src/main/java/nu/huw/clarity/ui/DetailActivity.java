package nu.huw.clarity.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.BuildConfig;
import nu.huw.clarity.R;
import nu.huw.clarity.db.model.DataModelHelper;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapter.AttachmentListAdapter.OnAttachmentListInteractionListener;
import nu.huw.clarity.ui.adapter.DetailPagerAdapter;
import nu.huw.clarity.ui.fragment.DetailInfoFragment.OnDetailInfoInteractionListener;
import nu.huw.clarity.ui.misc.CheckCircle;

public class DetailActivity extends AppCompatActivity
    implements OnDetailInfoInteractionListener, OnAttachmentListInteractionListener {

  @BindView(R.id.relativelayout_detail_container)
  RelativeLayout relativelayout_detail_container;
  @BindView(R.id.appbarlayout_detail)
  AppBarLayout appbarlayout_detail;
  @BindView(R.id.toolbar_detail)
  Toolbar toolbar_detail;
  @BindView(R.id.checkcircle_detail)
  CheckCircle checkcircle_detail;
  @BindView(R.id.textinputlayout_detail_name)
  TextInputLayout textinputlayout_detail_name;
  @BindView(R.id.textinputedittext_detail_name)
  TextInputEditText textinputedittext_detail_name;
  @BindView(R.id.tablayout_detail)
  TabLayout tablayout_detail;
  @BindView(R.id.viewpager_detail)
  ViewPager viewpager_detail;
  private Entry entry;
  private Perspective perspective;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    Intent intent = getIntent();
    if (intent.hasExtra("ENTRY")) {
      entry = intent.getParcelableExtra("ENTRY");
    } else {
      throw new IllegalArgumentException("Intent must contain 'ENTRY'");
    }

    if (intent.hasExtra("PERSPECTIVE")) {
      perspective = intent.getParcelableExtra("PERSPECTIVE");
    } else {
      throw new IllegalArgumentException("Intent must contain 'PERSPECTIVE'");
    }

    // Set the theme to the theme we just had back in MainActivity

    if (perspective != null) {
      setTheme(perspective.themeID);
    }

    // Set content view __AFTER__ theme

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    ButterKnife.bind(this);

    setSupportActionBar(toolbar_detail);
    final ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setTitle("");
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Set background colours depending on the view we just came from

    TypedValue typedValue = new TypedValue();
    getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
    relativelayout_detail_container.setBackgroundColor(typedValue.data);

    // Set the default text in the text field to the thing's name, and set the hint text to
    // the thing's type

    textinputedittext_detail_name.setText(entry.name);
    int hintID = R.string.detail_name;

    if (entry instanceof Task) {
      if (((Task) entry).isProject) {
        hintID = R.string.detail_project;
      } else {
        hintID = R.string.detail_task;
      }
    } else if (entry instanceof Context) {
      hintID = R.string.detail_context;
    } else if (entry instanceof Folder) {
      hintID = R.string.detail_folder;
    }

    textinputedittext_detail_name.setHint(hintID);
    textinputlayout_detail_name.setHint(getResources().getString(hintID));

    // Check circle

    if (entry instanceof Task) {
      Task task = (Task) entry;

      // Available tasks can have a flag, but they can't have colorised overdue/due soon
      // circles because the user doesn't want to start them yet.

      checkcircle_detail.setChecked(task.dateCompleted != null);
      checkcircle_detail.setFlagged(task.flagged && perspective.themeID != R.style.AppTheme_Orange);

      // Also, if the colour is going to clash with the background, then don't set the
      // attribute. This applies to the red clashing with forecast, and orange clashing
      // with flagged (in both cases the intended colour should be pretty obvious)

      if (task.isRemaining()) {
        checkcircle_detail.setOverdue(task.overdue && perspective.themeID != R.style.AppTheme_Red);
        checkcircle_detail.setDueSoon(task.dueSoon);
      } else {
        checkcircle_detail.setOverdue(false);
        checkcircle_detail.setDueSoon(false);
      }
    } else {
      // Remove the check circle
      checkcircle_detail.setVisibility(View.GONE);
    }

    // Setup view pager and tablayout automatically

    DetailPagerAdapter viewPagerAdapter = new DetailPagerAdapter(getSupportFragmentManager(),
        getApplicationContext(), perspective,
        entry);
    viewpager_detail.setAdapter(
        new DetailPagerAdapter(getSupportFragmentManager(), getApplicationContext(), perspective,
            entry));
    tablayout_detail.setupWithViewPager(viewpager_detail);

    if (viewPagerAdapter.getCount() <= 1) {

      // Remove tab layout if only one tab

      tablayout_detail.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onContextClick(Entry entry, Perspective perspective) {

    DataModelHelper dataModelHelper = new DataModelHelper(this);

    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("ENTRY", entry);
    intent.putExtra("PERSPECTIVE", dataModelHelper.getContextsPerspective());
    intent.putExtra("BACK_BUTTON", true);
    startActivity(intent);
  }

  @Override
  public void onProjectClick(Entry entry, Perspective perspective) {

    DataModelHelper dataModelHelper = new DataModelHelper(this);

    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("ENTRY", entry);
    intent.putExtra("PERSPECTIVE", dataModelHelper.getProjectsPerspective());
    intent.putExtra("BACK_BUTTON", true);
    startActivity(intent);
  }

  @Override
  public void onAttachmentInteraction(@NonNull Attachment attachment) {

    // Open the file with the default mime type

    Uri fileURI = FileProvider
        .getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
            attachment.file);
    Intent newIntent = new Intent(Intent.ACTION_VIEW);
    String mimeType = getApplicationContext().getContentResolver().getType(fileURI);
    newIntent.setDataAndType(fileURI, mimeType);
    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    try {
      startActivity(newIntent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(getApplicationContext(), "No handler for this type of file", Toast.LENGTH_LONG)
          .show(); // TODO: Change to a snackbar
    }

  }
}
