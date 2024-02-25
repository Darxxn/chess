package dataAccess;

import java.util.ArrayList;

public class UserData {
    private final ArrayList<model.UserData> users = new ArrayList<>();

    public model.UserData getUser(String username) throws DataAccessException {
        for (model.UserData user: users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public void clearUsers() {
        users.clear();
    }

    public void createUsers(String username, String password, String email) throws DataAccessException {
        if (username == null || password == null || email == null) {
            throw new DataAccessException("Missing info");
        }
        model.UserData newUser = new model.UserData(username, password, email);
        users.add(newUser);
    }
}
