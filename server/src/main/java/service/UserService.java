package service;

import dataAccess.*;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import request.LoginRequest;
import request.RegisterRequest;

public class UserService {
    private final MySQLUser userDAO = new MySQLUser();

    public String add(RegisterRequest user) throws DataAccessException {
        if (userDAO.readUser(user.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Error: bad request");
        }
        UserData userData = new UserData(user.username(), user.password(), user.email());
        userDAO.createUser(userData);
        return user.username();
    }

    public void clear() throws DataAccessException {
        userDAO.deleteAllUsers();
    }

    public String login(LoginRequest login) throws DataAccessException {
        UserData user = userDAO.readUser(login.username());

        if (user == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = userDAO.readUser(login.username()).password();

        if (!encoder.matches(login.password(), hashedPassword)) {
            throw new DataAccessException("Error: unauthorized");
        }
        return user.username();
    }
}
