package service;

import dataAccess.*;
import request.LoginRequest;
import request.RegisterRequest;

public class DataService {
    private final mySQLAuth authDAO = new mySQLAuth();

    public String add(RegisterRequest user) throws DataAccessException {
        return authDAO.createAuth(user.username());
    }

    public void clear() throws DataAccessException {
        authDAO.deleteAllAuth();
    }

    public String login(LoginRequest login) throws DataAccessException {
        return authDAO.createAuth(login.username());
    }

    public void logout(String authToken) throws DataAccessException {
        if(authDAO.readAuth(authToken) != null) {
            authDAO.deleteAuth(authToken);
        }
        else {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public void verify(String authToken) throws DataAccessException {
        if(authDAO.readAuth(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public String getUsername(String authToken) throws DataAccessException {
        if (authDAO.readAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return authDAO.readAuth(authToken).username();
    }
}


