package service;

import model.User;

import java.util.List;

/**
 * Handles user authentication and registration.
 */
public class AuthService {

    private List<User> users;
    private User currentUser;

    public AuthService() {
        users = DataStore.loadUsers();
    }

    /**
     * @return authenticated User or null if credentials are wrong.
     */
    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.checkPassword(password)) {
                currentUser = u;
                return u;
            }
        }
        return null;
    }

    /**
     * @return true if registration succeeded, false if username already taken.
     */
    public boolean register(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return false;
        }
        users.add(new User(username, password, false));
        DataStore.saveUsers(users);
        return true;
    }

    public void logout() { currentUser = null; }

    public User getCurrentUser() { return currentUser; }
}
