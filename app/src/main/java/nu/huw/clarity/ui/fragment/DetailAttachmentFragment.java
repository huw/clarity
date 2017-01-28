package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.db.model.DataModelHelper;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapter.AttachmentListAdapter;
import nu.huw.clarity.ui.adapter.AttachmentListAdapter.OnAttachmentListInteractionListener;

public class DetailAttachmentFragment extends Fragment {

  private static final String TAG = DetailAttachmentFragment.class.getSimpleName();
  @BindView(R.id.listview_detail_attachments)
  ListViewCompat listview_detail_attachments;
  private OnAttachmentListInteractionListener listener;
  private Task task;
  private Perspective perspective;

  public DetailAttachmentFragment() {
  }

  public static DetailAttachmentFragment newInstance(@NonNull Task task,
      @Nullable Perspective perspective) {

    DetailAttachmentFragment fragment = new DetailAttachmentFragment();
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
    View view = inflater.inflate(R.layout.fragment_attachment, container, false);
    ButterKnife.bind(this, view);

    // Get list of attachments and set adapter

    DataModelHelper dataModelHelper = new DataModelHelper(getContext());
    List<Attachment> attachments = dataModelHelper.getAttachmentsFromTask(task);

    Log.i(TAG, attachments.size() + "");

    if (!attachments.isEmpty()) {
      AttachmentListAdapter adapter = new AttachmentListAdapter(getContext(), attachments,
          listener);
      listview_detail_attachments.setAdapter(adapter);
    }

    return view;
  }

  @Override
  public void onAttach(android.content.Context context) {
    super.onAttach(context);
    if (context instanceof OnAttachmentListInteractionListener) {
      listener = (OnAttachmentListInteractionListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnAttachmentListInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }
}
