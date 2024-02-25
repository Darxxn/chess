package dataAccess;

import com.google.gson.Gson;
public interface UserDAO {
    String createUser(String username, String password, String email);
}