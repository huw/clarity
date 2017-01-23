package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.lang.reflect.Field;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;

public class DetailNoteFragment extends Fragment {

  private static final String TAG = DetailInfoFragment.class.getSimpleName();
  @BindView(R.id.edittext_detail_note)
  AppCompatEditText edittext_detail_note;
  private Task task;
  private Perspective perspective;

  public DetailNoteFragment() {
  }

  public static DetailNoteFragment newInstance(@NonNull Task task,
      @Nullable Perspective perspective) {

    DetailNoteFragment fragment = new DetailNoteFragment();
    Bundle args = new Bundle();
    args.putParcelable("TASK", task);
    args.putParcelable("PERSPECTIVE", perspective);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      task = args.getParcelable("TASK");
      perspective = args.getParcelable("PERSPECTIVE");
    } else {
      throw new IllegalArgumentException("DetailFragment requires argument bundle");
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_note, container, false);
    ButterKnife.bind(this, view);

    // Set text

    if (task.noteXML != null) {
      edittext_detail_note.setText(task.noteXML);
    }

    // Set cursor colour

    try {
      Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
      f.setAccessible(true);
      f.set(edittext_detail_note, perspective.cursorDrawable);
    } catch (NoSuchFieldException | IllegalAccessException ignored) {}

    return view;
  }
}
