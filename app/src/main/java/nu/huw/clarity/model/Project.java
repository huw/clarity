package nu.huw.clarity.model;

import android.util.Xml;

import java.util.Date;

public class Project extends Task {

    public Date mLastReview;
    public Date mNextReview;
    public Date mReviewInterval;
    public String mStatus;
    public String mFolderID;
    public boolean mSingleAction;

    public Project(String ID, Date dateAdded, Date dateModified, String name, String parentID,
                   int rank, String order, String repeatMethod, String repeatRule,
                   boolean completedByChildren, boolean flagged, boolean inInbox, Date defer,
                   Date due, Date completed, Date estimatedMinutes, Context context, Xml noteXml,

            Date lastReview,
            Date nextReview,
            Date reviewInterval,
            String status,
            String folderID,
            boolean singleAction
    ) {
        super(ID, dateAdded, dateModified, name, parentID, rank, order, repeatMethod, repeatRule,
                completedByChildren, flagged, inInbox, defer, due, completed, estimatedMinutes,
                context, noteXml);

        mLastReview = lastReview;
        mNextReview = nextReview;
        mReviewInterval = reviewInterval;
        mStatus = status;
        mFolderID = folderID;
        mSingleAction = singleAction;
    }
}
