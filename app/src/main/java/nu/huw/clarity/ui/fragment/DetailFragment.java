package nu.huw.clarity.ui.fragment;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
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
import nu.huw.clarity.ui.activity.MainActivity;
import nu.huw.clarity.ui.adapter.AttachmentListAdapter.OnAttachmentListInteractionListener;
import nu.huw.clarity.ui.adapter.DetailPagerAdapter;
import nu.huw.clarity.ui.fragment.DetailInfoFragment.OnDetailInfoInteractionListener;
import nu.huw.clarity.ui.misc.CheckCircle;

public class DetailFragment extends BottomSheetDialogFragment implements
    OnDetailInfoInteractionListener,
    OnAttachmentListInteractionListener, OnShowListener {

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

  public DetailFragment() {
  }

  /**
   * Creates a new DetailFragment
   *
   * @param perspective Perspective to determine colours and format
   * @param entry Entry to display
   */
  public static DetailFragment newInstance(@NonNull Perspective perspective, @NonNull Entry entry) {
    DetailFragment fragment = new DetailFragment();
    Bundle args = new Bundle();
    args.putParcelable("PERSPECTIVE", perspective);
    args.putParcelable("ENTRY", entry);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {

    Bundle args = getArguments();
    if (args.containsKey("ENTRY")) {
      entry = args.getParcelable("ENTRY");
    } else {
      throw new IllegalArgumentException("Intent must contain 'ENTRY'");
    }

    if (args.containsKey("PERSPECTIVE")) {
      perspective = args.getParcelable("PERSPECTIVE");
    } else {
      throw new IllegalArgumentException("Intent must contain 'PERSPECTIVE'");
    }

    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_detail, container, false);
    ButterKnife.bind(this, view);

    // Set the bottom sheet behaviours once the dialog shows

    getDialog().setOnShowListener(this);

    // Dismiss the fragment on click

    toolbar_detail.setNavigationOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });

    // Status bar fix

    getDialog().getWindow().addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);

    // Set app bar colour

    @ColorInt int color = ContextCompat.getColor(getContext(), perspective.colorID);
    appbarlayout_detail.setBackgroundColor(color);
    relativelayout_detail_container.setBackgroundColor(color);

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
    // Remember to use the child fragment manager since we're nesting fragments

    DetailPagerAdapter viewPagerAdapter = new DetailPagerAdapter(getChildFragmentManager(),
        getContext(), perspective, entry);
    viewpager_detail.setAdapter(viewPagerAdapter);
    tablayout_detail.setupWithViewPager(viewpager_detail);

    if (viewPagerAdapter.getCount() <= 1) {

      // Remove tab layout if only one tab

      tablayout_detail.setVisibility(View.GONE);
    }

    return view;
  }

  /**
   * Runs when the dialog shows, which is when we're able to get the bottom sheet behaviour and set
   * the dialog to be expanded
   */
  @Override
  public void onShow(DialogInterface dialogInterface) {
    Dialog dialog = getDialog();
    View bottomSheet = dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
    final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

    // Set bottom sheet callbacks

    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {

          // Dismiss dialog when hidden

          dismiss();
        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {

          // Disable the collapsed state by rerouting it to the hidden state

          BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_HIDDEN);
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
      }
    });

    // Whenever the callback detects the sheet is in the peek state, it dismisses it
    // But with the default peek height, the drag down animation is janky
    // This cheap hack sets the peek height to be way at the bottom of the screen, smoothening
    // the transition really quickly

    bottomSheetBehavior.setPeekHeight(1);

    // Set the dialog to be expanded (fullscreen) by default

    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
  }

  @Override
  public void onContextClick(Entry entry, Perspective perspective) {

    DataModelHelper dataModelHelper = new DataModelHelper(getContext());

    Intent intent = new Intent(getContext(), MainActivity.class);
    intent.putExtra("ENTRY", entry);
    intent.putExtra("PERSPECTIVE", dataModelHelper.getContextsPerspective());
    intent.putExtra("BACK_BUTTON", true);
    startActivity(intent);
  }

  @Override
  public void onProjectClick(Entry entry, Perspective perspective) {

    DataModelHelper dataModelHelper = new DataModelHelper(getContext());

    Intent intent = new Intent(getContext(), MainActivity.class);
    intent.putExtra("ENTRY", entry);
    intent.putExtra("PERSPECTIVE", dataModelHelper.getProjectsPerspective());
    intent.putExtra("BACK_BUTTON", true);
    startActivity(intent);
  }

  @Override
  public void onAttachmentInteraction(@NonNull Attachment attachment) {

    // Open the file with the default mime type

    Uri fileURI = FileProvider
        .getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", attachment.file);
    Intent newIntent = new Intent(Intent.ACTION_VIEW);
    String mimeType = getContext().getContentResolver().getType(fileURI);
    newIntent.setDataAndType(fileURI, mimeType);
    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    try {
      startActivity(newIntent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(getContext(), "No handler for this type of file", Toast.LENGTH_LONG)
          .show(); // TODO: Change to a snackbar
    }
  }
}
