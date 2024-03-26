package java.clientTests;

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
        var port = 1234;
        server = new Server();
        server.run(port);
        serverFacade = new ServerFacade("http://localhost:1234");
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
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void positiveRegister() {
        String username = "newUser";
        String password = "newPass123";
        String email = "newEmail@example.com";

        AuthData authResult = null;

        try {
            authResult = serverFacade.registerUser(username, password, email);
        } catch (DataAccessException ex) {
            Assertions.fail("Registration failed unexpectedly: " + ex.getMessage());
        }
        Assertions.assertNotNull(authResult, "AuthInfo should not be null after successful registration.");
    }
}