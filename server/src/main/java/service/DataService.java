package service;

import dataAccess.DataAccessException;
import dataAccess.MemoryAuthDAO;
import request.LoginRequest;
import request.RegisterRequest;

import javax.xml.crypto.Data;

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
            throw new DataAccessException("error: unauthorized");
        }
    }

    public
}


