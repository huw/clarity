package nu.huw.clarity.model;

/**
 * A context, in OmniFocus, holds a number of tasks but is external from the tree. Every task
 * lists a context to which it belongs.
 */
public class Context extends Entry {

    public double  altitude;
    public double  latitude;
    public String  locationName;
    public double  longitude;
    public boolean onHold;
    public double  radius;

    public Context() {}
}
