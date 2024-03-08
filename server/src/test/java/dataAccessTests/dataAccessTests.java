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

public class dataAccessTests {
    private final mySQLUser userService = new mySQLUser();
    private final mySQLAuth authService = new mySQLAuth();
    private final mySQLGame gameService = new mySQLGame();

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    UserData user = new UserData("nappy", "pass1", "email@gmail.com");
    UserData hashedPassword = new UserData("nappy", encoder.encode("pass1"), "email@gmail.com");

    GameData firstGame = new GameData(1, "white", "black", "chess game", new ChessGame());
    GameData secondGame = new GameData(2, "white", "black", "chess game", new ChessGame());
    GameData thirdGame = new GameData(3, "white", "black", "chess game", new ChessGame());



}
