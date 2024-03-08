package serviceTests;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import dataAccess.DataAccessException;
import org.junit.jupiter.api.Test;
import service.*;
import request.*;
import result.*;

public class ServiceTests {
    private final UserService userService = new UserService();
    private final DataService authService = new DataService();
    private final GameService gameService = new GameService();

    RegisterRequest user = new RegisterRequest("nappy", "pass123", "email@gmail.com");
    CreateGameRequest newGame = new CreateGameRequest("new game of chess");
    LoginRequest login = new LoginRequest("nappy", "pass123");

    @Test
    public void clearAllTests() throws DataAccessException {
        userService.clear();
        userService.add(user);
        authService.add(user);
        gameService.createGame(newGame);
        userService.clear();
        authService.clear();
        gameService.clear();
    }

    @Test
    public void negativeRegisterUserTest() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            RegisterRequest falseUser = new RegisterRequest(null, null, null);
            userService.add(falseUser);
            authService.add(falseUser);
        });
    }

    @Test
    public void positiveRegisterUserTest() throws DataAccessException {
        userService.clear();
        userService.add(user);
        authService.add(user);
    }

    @Test
    public void negativeLoginUser() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
           LoginRequest wrongLogin = new LoginRequest("nap", "py");
           String username = userService.add(user);
           String authToken = authService.add(user);
           userService.login(wrongLogin);
        });
    }

    @Test
    public void positiveLoginUser() throws DataAccessException {
        userService.clear();
        String username = userService.add(user);
        String authToken = authService.add(user);
        userService.login(login);
        authService.login(login);
    }

    @Test
    public void negativeLogout() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            String wrongToken = UUID.randomUUID().toString();
            authService.logout(wrongToken);
        });
    }

    @Test
    public void positiveLogout() throws DataAccessException {
        String authToken = authService.add(user);
        authService.logout(authToken);
    }

    @Test
    public void negativeCreateGame() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
           CreateGameRequest wrongGame = new CreateGameRequest(null);
           gameService.createGame(wrongGame);
        });
    }

    @Test
    public void positiveCreateGame() throws DataAccessException {
        gameService.createGame(newGame);
    }

    @Test
    public void negativeListGames() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            String authToken = authService.add(user);
            String wrongToken = UUID.randomUUID().toString();
            authService.verify(wrongToken);
        });
    }

    @Test
    public void positiveListGames() throws DataAccessException {
        String authToken = authService.add(user);
        gameService.createGame(newGame);
        authService.verify(authToken);
        gameService.listGames();
    }

    @Test
    public void negativeJoinGame() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
           String authToken = authService.add(user);
           authService.verify(authToken);
           gameService.createGame(newGame);
           JoinGameRequest wrongJoin = new JoinGameRequest(null, 43);
           gameService.joinGame(wrongJoin, null);
        });
    }

    @Test
    public void positiveJoinGame() throws DataAccessException {
        String username = userService.add(user);
        String authToken = authService.add(user);
        authService.verify(authToken);
        CreateGameResponse game = gameService.createGame(newGame);
        JoinGameRequest correctJoin = new JoinGameRequest(username, game.gameID());
    }
}
