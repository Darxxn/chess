package dataAccess;

import model.UserData;

public interface UserDataDAO {
    void createUser(UserData user) throws DataAccessException;
    UserData readUser(String username) throws DataAccessException;
    void deleteAllUsers() throws DataAccessException;
}