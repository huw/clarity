package nu.huw.clarity.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.model_old.Attachment;

public class AttachmentListAdapter extends ArrayAdapter<Attachment> {

  private OnAttachmentListInteractionListener listener;

  public AttachmentListAdapter(Context androidContext, List<Attachment> attachments,
      OnAttachmentListInteractionListener listener) {
    super(androidContext, 0, attachments);
    this.listener = listener;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {

    // Get and inflate view

    final Attachment attachment = getItem(position);
    if (view == null) {
      view = LayoutInflater.from(getContext()).inflate(R.layout.item_attachment, parent, false);
    }

    // Set attachment name

    TextView textview_attachmentitem_name = (TextView) view
        .findViewById(R.id.textview_attachmentitem_name);
    textview_attachmentitem_name.setText(attachment.name);

    // Set click listener

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onAttachmentInteraction(attachment);
      }
    });

    return view;
  }

  /**
   * This interface must be implemented by the containing activity so that it can receive events
   * like clicks from the attachments and appropriately respond.
   */
  public interface OnAttachmentListInteractionListener {

    void onAttachmentInteraction(@NonNull Attachment attachment);
  }
}
