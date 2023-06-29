package users;

import java.util.*;

public class UserManager {
    private final Set<String> usersSet;

    public UserManager() {
        users = new LinkedHashMap<>();
    }

    public synchronized void addUser(String username) {
        usersSet.add(username);
    }

    public synchronized void removeUser(String username) {
        usersSet.remove(username);
    }

    public synchronized Set<String> getUsers() {
        return Collections.unmodifiableSet(usersSet);
    }

    public boolean isUserExists(String username) {
        return usersSet.contains(username);
    }

    public synchronized User getUser(String name) {return users.get(name); }
}
