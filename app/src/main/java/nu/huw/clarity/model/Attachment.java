package nu.huw.clarity.model;

import java.util.Date;

public class Attachment extends Base {

    public String mName;
    public String mParentID;
    public String mPreviewImage;

    public Attachment(String id, Date dateAdded, Date dateModified,

            String name,
            String parentID,
            String previewImage
    ) {
        super(id, dateAdded, dateModified);

        mName = name;
        mParentID = parentID;
        mPreviewImage = previewImage;
    }
}
