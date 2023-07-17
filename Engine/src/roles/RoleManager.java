package roles;

import java.util.HashMap;
import java.util.Map;

public class RoleManager {
    public Map<String,Role> roles;

    public RoleManager() {
        this.roles = new HashMap<>();
    }

    public synchronized void addRole(Role role) {
        roles.put(role.getName(), role);
    }


    public synchronized boolean removeRole(String name) {
        boolean res=false;
        if(!roles.get(name).isUsersAssigned()) {
            roles.remove(name);
            res=true;
        }
        return res;
    }

    public synchronized Map<String, Role> getRoles() {
        return roles;
    }

    public synchronized Role getRole(String name) {return roles.get(name); }

    public boolean isRoleExist(String name) {
        return roles.containsKey(name);
    }
}
