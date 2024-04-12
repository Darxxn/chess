package dataAccess;

import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO {
    private final HashMap<String, AuthData> authDataHashMap = new HashMap<>();

    public String createAuth(String username) {
        UUID uuid = UUID.randomUUID();
        AuthData auth = new AuthData(username, uuid.toString());
        authDataHashMap.put(uuid.toString(), auth);
        return uuid.toString();
    }

    public AuthData readAuth(String authToken) {
        return authDataHashMap.get(authToken);
    }

    public void deleteAuth(String authToken) {
        authDataHashMap.remove(authToken);
    }
}
