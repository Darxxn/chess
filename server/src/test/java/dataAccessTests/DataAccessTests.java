package dataAccessTests;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import dataAccess.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {
    private final MySQLUser userDAO = new MySQLUser();
    private final MySQLAuth authDAO = new MySQLAuth();
    private final MySQLGame gameDAO = new MySQLGame();

    UserData user = new UserData("nappy", "pass1", "email@gmail.com");

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

    @Test
    public void positiveCreateGame() throws DataAccessException {
        gameDAO.createGame(firstGame);
    }

    @Test
    public void negativeCreateGame() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(firstGame);
            gameDAO.createGame(firstGame);
        });
    }

    @Test
    public void positiveDeleteAllGameData() throws DataAccessException {
        gameDAO.deleteAllGameData();
    }

    @Test
    public void negativeDeleteAllGameData() throws DataAccessException {
        gameDAO.deleteAllGameData();
        GameData gameData = gameDAO.readGame(45);
        assertNull(gameData);
    }

    @Test
    public void positiveDeleteGame() throws DataAccessException {
        gameDAO.createGame(firstGame);
        gameDAO.deleteGame(firstGame.gameID());
    }

    @Test
    public void negativeDeleteGame() throws DataAccessException {
        GameData gameData = gameDAO.readGame(43);
        assertNull(gameData);
    }

    @Test
    public void positiveReadGame() throws DataAccessException {
        gameDAO.createGame(firstGame);
        GameData gameData = gameDAO.readGame(firstGame.gameID());
        assertEquals(firstGame, gameData);
    }

    @Test
    public void positiveAllGames() throws DataAccessException {
        gameDAO.createGame(firstGame);
        gameDAO.createGame(secondGame);
        gameDAO.createGame(thirdGame);
        ArrayList<GameData> createdGames = gameDAO.allGames();

        ArrayList<GameData> newGameList = new ArrayList<>();
        newGameList.add(firstGame);
        newGameList.add(secondGame);
        newGameList.add(thirdGame);

        assertEquals(createdGames, newGameList);
    }

    @Test
    public void negativeAllGames() throws DataAccessException {
        gameDAO.createGame(firstGame);
        ArrayList<GameData> createdGames = gameDAO.allGames();
        ArrayList<GameData> newGameList = new ArrayList<>();
        assertNotEquals(createdGames, newGameList);
    }
}
