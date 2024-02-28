package dataAccess;

import model.UserData;

public interface UserDataDAO {
    void createUser(UserData user);
    UserData readUser(String username);
    void deleteAllGames();

}