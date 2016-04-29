package nu.huw.clarity.model;

import java.util.Date;

/**
 * A basic class for any OmniFocus element. Everything will implement the fields in this class.
 */
public class Base {

    public String id;
    public Date   added;
    public Date   modified;

    public Base() {}
}
