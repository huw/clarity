package nu.huw.clarity.model;

import java.util.Date;

public class Folder extends Entry {
    public Folder(String ID, Date dateAdded, Date dateModified, String name, String parentID,
                  int rank
    ) {
        super(ID, dateAdded, dateModified, name, parentID, rank);
    }
}
