package dataAccessTests;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import result.*;
import request.*;
import java.util.UUID;
import service.*;
import org.junit.jupiter.api.Test;
import dataAccess.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;


public class dataAccessTests {
    private final mySQLUser userDAO = new mySQLUser();
    private final mySQLAuth authDAO = new mySQLAuth();
    private final mySQLGame gameDAO = new mySQLGame();

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    UserData user = new UserData("nappy", "pass1", "email@gmail.com");
    UserData hashedPassword = new UserData("nappy", encoder.encode("pass1"), "email@gmail.com");

    GameData firstGame = new GameData(1, "white", "black", "chess game", new ChessGame());
    GameData secondGame = new GameData(2, "white", "black", "chess game", new ChessGame());
    GameData thirdGame = new GameData(3, "white", "black", "chess game", new ChessGame());

    @BeforeEach
    public void clearing() throws DataAccessException {
        userDAO.deleteAllUsers();
        authDAO.deleteAllAuth();
        gameDAO.deleteAllGameData();
    }

    @Test
    public void positiveReadUser() throws DataAccessException {
        userDAO.createUser(user);
        UserData readUser = userDAO.readUser(user.username());
    }

    @Test
    public void negativeReadUser() throws DataAccessException {
        UserData readUser = userDAO.readUser("nonexistent user");
        assertNull(readUser);
    }

    @Test
    public void positiveCreateUser() throws DataAccessException {
        userDAO.createUser(user);
    }

    @Test
    public void negativeCreateUser() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user);
            userDAO.createUser(user);
        });
    }

    @Test
    public void positiveDeleteAllUsers() throws DataAccessException {
        userDAO.createUser(user);
        userDAO.deleteAllUsers();
    }

    @Test
    public void negativeDeleteAllUsers() throws DataAccessException{
        userDAO.deleteAllUsers();
        UserData newUser = userDAO.readUser(user.username()) ;
        assertNull(newUser);
    }

    @Test
    public void positiveCreateAuth() throws DataAccessException {
        authDAO.createAuth(user.username());
    }

    @Test
    public void negativeCreateAuth() throws DataAccessException {
        authDAO.createAuth(user.username());
        authDAO.createAuth(user.username());
    }

    @Test
    public void positiveReadAuth() throws DataAccessException {
        String authToken = authDAO.createAuth(user.username());
        AuthData authData = authDAO.readAuth(authToken);
        assertEquals(user.username(), authData.username());
    }

    @Test
    public void negativeReadAuth() throws DataAccessException {
        AuthData authData = authDAO.readAuth("authToken");
        assertNull(authData);
    }

    @Test
    public void positiveDeleteAuth() throws DataAccessException {
        String authToken = authDAO.createAuth(user.username());
        authDAO.deleteAuth(authToken);
    }

    @Test
    public void negativeDeleteAuth() throws DataAccessException {
        String authToken = authDAO.createAuth(user.username());
        authDAO.deleteAuth(authToken);
        AuthData authData = authDAO.readAuth("wrong token");
        assertNull(authData);
    }

    @Test
    public void positiveDeleteAllAuth() throws DataAccessException {
        authDAO.deleteAllAuth();
    }

    @Test
    public void negativeDeleteAllAuth() throws DataAccessException {
        authDAO.deleteAllAuth();
        AuthData authData = authDAO.readAuth("wrong token");
        assertNull(authData);
    }
}
