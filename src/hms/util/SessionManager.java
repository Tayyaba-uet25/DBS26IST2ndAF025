package hms.util;

import hms.model.User;

/**
 * Holds the currently logged-in user for the running session.
 * SOFTWARE CLASS #5
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() { }

    public static void login(User user) {
        currentUser = user;
        AppLogger.info("User logged in: " + user.getUsername());
    }

    public static void logout() {
        if (currentUser != null) {
            AppLogger.info("User logged out: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String currentUserName() {
        return currentUser == null ? "guest" : currentUser.getUsername();
    }
}
