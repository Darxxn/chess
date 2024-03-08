package dataAccess;

import model.AuthData;

public interface AuthDataDAO {
    String createAuth(String username) throws DataAccessException;
    AuthData readAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void deleteAllAuth() throws DataAccessException;
}
