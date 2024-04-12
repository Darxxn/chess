package dataAccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO {
    private final HashMap<String, UserData> userDataHashMap = new HashMap<>();

    public void createUser(UserData user) {
        userDataHashMap.put(user.username(), user);
    }

    public UserData readUser(String username) {
        return userDataHashMap.get(username);
    }
}
