package hms.ui;

/** Implemented by any screen that should reload its data when shown. */
public interface Refreshable {
    void refresh();
}
