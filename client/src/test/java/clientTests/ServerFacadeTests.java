package clientTests;

import dataAccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.*;

public class ServerFacadeTests {
    private static ServerFacade serverFacade;
    private static Server server;


    @BeforeAll
    public static void init() {
        var port = 123;
        server = new Server();
        server.run(port);
        serverFacade = new ServerFacade("http://localhost:123");
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterEach
    public void resetData() {
        serverFacade.clearData();
    }

    @AfterAll
    public static void clearServer() {
        serverFacade.clearData();
        server.stop();
    }

    @Test
    public void positiveRegisterUser() {
        String username = "newUser";
        String password = "newPass123";
        String email = "newEmail@example.com";

        AuthData authResult = null;

        try {
            authResult = serverFacade.registerUser(username, password, email);
        } catch (exception.DataAccessException ex) {
            Assertions.fail("Registration failed unexpectedly: " + ex.getMessage());
        }
        Assertions.assertNotNull(authResult, "AuthData should not be null after successful registration.");
    }

    @Test
    public void negativeRegisterUser() {
        String username = null;
        String password = "newPass123";
        String email = "newEmail@example.com";

        try {
            serverFacade.registerUser(username, password, email);
            Assertions.fail("Expected exception");
        } catch (exception.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void negativeRegisteringExistingUser() {
        String username = "existingUser";
        String password = "eUser";
        String email = "eUser@example.com";

        try {
            serverFacade.registerUser(username, password, email);
            serverFacade.registerUser(username, password, email);
        } catch (exception.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void positiveLoginUser() {
        String username = "newUser";
        String password = "newPass123";
        String email = "newEmail@example.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            serverFacade.logout(authData.authToken());

            AuthData test = serverFacade.login(username, password);

            Assertions.assertNotNull(test);

        } catch (exception.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void negativeLoginUser() {
        String username = "newUser";
        String password = "newPass123";
        String email = "newEmail@example.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            serverFacade.logout(authData.authToken());

            serverFacade.login(username, null);

            Assertions.fail("Cannot login user");

        } catch (exception.DataAccessException e) {
            Assertions.assertEquals("Error\n", e.getMessage());
        }
    }

    @Test
    public void positiveLogoutUser() {
        String username = "newUser";
        String password = "newPass123";
        String email = "newEmail@example.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            serverFacade.logout(authData.authToken());
        } catch (exception.DataAccessException e) {
            Assertions.assertEquals("Error\n", e.getMessage());
        }
    }

    @Test
    public void negativeLogoutUser() {
        String username = "newUser";
        String password = "newPass123";
        String email = "newEmail@example.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            serverFacade.logout(authData.authToken());
            serverFacade.logout(authData.authToken());
        } catch (exception.DataAccessException e) {
            Assertions.assertEquals("Error\n", e.getMessage());
        }
    }

    @Test
    public void positiveListGames() {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";


        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            serverFacade.createGame(authData.authToken(), "testGame2");
            serverFacade.createGame(authData.authToken(), "testGame3");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
        } catch (exception.DataAccessException e) {
            Assertions.fail("Error" + e.getMessage());
        }
    }

    @Test
    public void negativeListGames() {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            serverFacade.createGame(authData.authToken(), "testGame2");
            serverFacade.createGame(authData.authToken(), "testGame3");
            serverFacade.listGames("wrongToken");
            Assertions.fail("Expected exception");
        }
        catch (exception.DataAccessException e) {
            Assertions.assertEquals("Error\n", e.getMessage());
        }
    }

    @Test
    public void positiveCreateGame() {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";

        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
        } catch (exception.DataAccessException e) {
            Assertions.fail("Error\n" + e.getMessage());
        }
    }

    @Test
    public void negativeCreateGame() {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";


        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame("Wrong Token", "testGame1");
            Assertions.fail("Expected exception");
        } catch (exception.DataAccessException e) {
            Assertions.assertEquals("Error\n", e.getMessage());
        }
    }

    @Test
    public void positiveJoinGame() {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";


        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
            var result = serverFacade.joinGame(authData.authToken(), games.games().get(0).gameID(), "white");
            Assertions.assertNotNull(result);
        } catch (exception.DataAccessException e) {
            Assertions.fail("Error\n" + e.getMessage());
        }
    }

    @Test
    public void negativeJoinGame() {
        String username = "testUser";
        String password = "testPassword";
        String email = "test@test.com";


        try {
            AuthData authData = serverFacade.registerUser(username, password, email);
            Assertions.assertNotNull(authData);
            serverFacade.createGame(authData.authToken(), "testGame1");
            var games = serverFacade.listGames(authData.authToken());
            Assertions.assertNotNull(games);
            serverFacade.joinGame(authData.authToken(), -1, "white");
            Assertions.fail("Expected exception");
        } catch (exception.DataAccessException e) {
            Assertions.assertEquals("Error\n", e.getMessage());
        }
    }
}