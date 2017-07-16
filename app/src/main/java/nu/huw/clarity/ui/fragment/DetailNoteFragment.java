package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.db_old.model.NoteHelper;
import nu.huw.clarity.model_old.Perspective;
import nu.huw.clarity.model_old.Task;

public class DetailNoteFragment extends Fragment {

  private static final String TAG = DetailInfoFragment.class.getSimpleName();
  @BindView(R.id.edittext_detail_note)
  EditText edittext_detail_note;
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
    View view = inflater.inflate(R.layout.fragment_detail_note, container, false);
    ButterKnife.bind(this, view);

    // Set text

    if (task.noteXML != null) {
      String string = NoteHelper.noteXMLtoString(task.noteXML);
      edittext_detail_note.setText(string);
      edittext_detail_note.setMovementMethod(LinkMovementMethod.getInstance()); // enable links
    }

    return view;
  }
}
