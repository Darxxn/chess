package service;

import dataAccess.DataAccessException;
import dataAccess.MemoryUserDAO;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;

public class UserService {

    private final MemoryUserDAO userDAO = new MemoryUserDAO();
    public void clear() {
        userDAO.deleteAllGames();
    }
}
