package service;

import dataAccess.DataAccessException;
import dataAccess.MemoryUserDAO;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;

import javax.xml.crypto.Data;

public class UserService {

    private final MemoryUserDAO userDAO = new MemoryUserDAO();

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

    public void clear() {
        userDAO.deleteAllGames();
    }
}
