package nu.huw.clarity.model;

import android.util.Xml;

import java.util.Date;

public class Task extends Entry {

    public String mOrder;
    public String mRepeatMethod;
    public String mRepeatRule;
    public boolean mCompletedByChildren;
    public boolean mFlagged;
    public boolean mInInbox;
    public Date mDefer;
    public Date mDue;
    public Date mCompleted;
    public Date mEstimatedMinutes;
    public Context mContext;
    public Xml mNoteXml;

    public Task(String ID, Date dateAdded, Date dateModified, String name, String parentID,
                int rank,

            String order,
            String repeatMethod,
            String repeatRule,
            boolean completedByChildren,
            boolean flagged,
            boolean inInbox,
            Date defer,
            Date due,
            Date completed,
            Date estimatedMinutes,
            Context context,
            Xml noteXml
    ) {
        super(ID, dateAdded, dateModified, name, parentID, rank);

        mOrder = order;
        mRepeatMethod = repeatMethod;
        mRepeatRule = repeatRule;
        mCompletedByChildren = completedByChildren;
        mFlagged = flagged;
        mInInbox = inInbox;
        mDefer = defer;
        mDue = due;
        mCompleted = completed;
        mEstimatedMinutes = estimatedMinutes;
        mContext = context;
        mNoteXml = noteXml;
    }
}
