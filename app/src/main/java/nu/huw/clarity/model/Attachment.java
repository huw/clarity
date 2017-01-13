package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Attachments.
 */
public class Attachment extends Base {

  public static final Parcelable.Creator<Attachment> CREATOR =
      new Parcelable.Creator<Attachment>() {
        public Attachment createFromParcel(Parcel in) {

          return new Attachment(in);
        }

        public Attachment[] newArray(int size) {

          return new Attachment[size];
        }
      };
  public String name;
  public String parentID;
  public String path;

  public Attachment() {
  }

  protected Attachment(Parcel in) {

    super(in);
    name = in.readString();
    parentID = in.readString();
    path = in.readString();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {

    super.writeToParcel(out, flags);
    out.writeString(name);
    out.writeString(parentID);
    out.writeString(path);
  }
}
