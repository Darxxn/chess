package service;

import dataAccess.MemoryAuthDAO;
import request.LoginRequest;
import request.RegisterRequest;

public class DataService {

    private final MemoryAuthDAO authDAO = new MemoryAuthDAO();

    public String add(RegisterRequest user) {
        return authDAO.createAuth(user.username());
    }
    public void clear() {
        authDAO.deleteAllAuthData();
    }

    public String login(LoginRequest login) {
        return authDAO.createAuth(login.username());
    }
}


