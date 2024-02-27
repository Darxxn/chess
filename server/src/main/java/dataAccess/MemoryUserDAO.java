package dataAccess;

import service.UserService;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO {
    private final HashMap<String, UserData> userDataHashMap = new HashMap<String, UserData>();

    public void createUser(UserData user) {
        userDataHashMap.put(user.username(), user);
    }

    public UserData readUser(String username) {
        return userDataHashMap.get(username);
    }

    public void deleteAllGames() {
        userDataHashMap.clear();
    }
}
