package dataAccess;

import model.AuthData;

public interface AuthDataDAO {
    String createAuth(String username);
    AuthData readAuth(String authToken);
    void deleteAuth(String authToken);
    void deleteAllAuth();
}
