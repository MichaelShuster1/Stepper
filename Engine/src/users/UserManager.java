package users;


import java.util.*;

public class UserManager {
    private final Map<String,User> users;

    public UserManager() {
        users = new LinkedHashMap<>();
    }


    public synchronized void addUser(String username) {
        users.put(username, new User(username));
    }

    public synchronized void removeUser(String username) {
        users.get(username).removeUserNameInRoles();
        users.remove(username);
    }

    public synchronized Set<String> getUsers() {
        return Collections.unmodifiableSet(users.keySet());
    }

    public synchronized Map<String,User> getUsersMap() {return Collections.unmodifiableMap(users);}

    public boolean isUserExists(String username) {
        return users.containsKey(username);
    }

    public synchronized User getUser(String name) {return users.get(name); }
}
