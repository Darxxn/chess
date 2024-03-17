package ui;

import java.awt.color.ICC_ColorSpace;
import java.util.Arrays;
import java.util.Scanner;

import chess.ChessPiece;
import model.*;
import chess.ChessGame;
import dataAccess.*;


public class ChessClient {

    private AuthData authData;
    private ChessState state = ChessState.LOGGED_OUT;
    private ChessServer server;
    private String url;
    private GameData gameData;
    private boolean serverLive = true;

    public ChessClient() {
        server = new ChessServer("http://localhost:8080");
        this.url = "http://localhost:8080";
    }

    public static void main(String[] args) {
        var newClient = new ChessClient();
        newClient.run();
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(EscapeSequences.ERASE_SCREEN);
        System.out.print(EscapeSequences.SET_TEXT_BOLD);
        System.out.println("Welcome to Chess!");
        System.out.print(EscapeSequences.SET_TEXT_FAINT);
        System.out.println("Type " + EscapeSequences.SET_TEXT_ITALIC + "Help " + EscapeSequences.RESET_TEXT_ITALIC + "to get started");

        while (serverLive) {
            System.out.print("[" + this.state + "]" + " >>> ");
            var input = scanner.nextLine();
            var output = eval(input);
            System.out.println(output);
        }
    }

    public String eval(String input) {
//        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "quit" -> quit();
                case "logout" -> logout();
                case "login" -> params.length < 2 ? (EscapeSequences.SET_TEXT_COLOR_YELLOW + "Missing login information.\n") + EscapeSequences.SET_TEXT_COLOR_WHITE : login(params[0], params[1]);
                case "register" -> params.length < 3 ? (EscapeSequences.SET_TEXT_COLOR_YELLOW + "Provide information to register.\n") + EscapeSequences.SET_TEXT_COLOR_WHITE : register(params[0], params[1], params[2]);
//                case "list" -> listGames();
                case "create" -> params.length< 1 ? (EscapeSequences.SET_TEXT_COLOR_YELLOW + "Provide a game name\n") + EscapeSequences.SET_TEXT_COLOR_WHITE : createGame(params[0]);
//                case "join" -> joinGame();
//                case "observe" -> obsGame();
                case "help" -> help();
                default -> "Provide a correct command\n";
            };
//        }
//        catch (DataAccessException ex) {
//         return ex.getMessage();
//        }
    }

    public String help() {
        if (state == ChessState.LOGGED_IN) {
            return (
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   create <NAME>" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- a game" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   list" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- games" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   join <ID> [WHITE][BLACK][<empty>]" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- game" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   observe <ID>" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- a game" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   logout" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- when you are done" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   quit" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- playing chess" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_MAGENTA + "   help" + EscapeSequences.SET_TEXT_COLOR_WHITE + "- with possible commands" + "\n"
                    );
        } else {
            return (
                    EscapeSequences.SET_TEXT_COLOR_BLUE + "   register <username> <password> <email> " + EscapeSequences.SET_TEXT_COLOR_WHITE + "- register a new account" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_BLUE + "   login <username> <password> " + EscapeSequences.SET_TEXT_COLOR_WHITE + "- log into an existing account" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_BLUE + "   quit " + EscapeSequences.SET_TEXT_COLOR_WHITE + "- playing chess" + "\n" +
                    EscapeSequences.SET_TEXT_COLOR_BLUE + "   help " + EscapeSequences.SET_TEXT_COLOR_WHITE + "- with possible commands" + "\n"
                    );
        }
    }

//    public String listGames() {
//        if (this.state == ChessState.LOGGED_OUT) {
//            return "You must login first";
//        }
//    }

    public String createGame(String gameName) {
        if (this.state == ChessState.LOGGED_OUT) {
            return "You must login to create a game\n";
        }

        try {
            var game = server.createGame(authData.authToken(), gameName);
            return game.gameName() + " was created!\n";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }

    public String quit() {
        if (this.state == ChessState.LOGGED_IN) {
            this.logout();
        }
        this.state = ChessState.LOGGED_OUT;
        this.serverLive = false;
        return "Chess client terminated.\n";
    }

    public String login(String username, String password) {
        if (this.state == ChessState.LOGGED_IN) {
            return "You must logout first\n";
        }

        try {
            AuthData user = server.login(username, password);
            this.authData = user;
            this.state = ChessState.LOGGED_IN;
            return user.username() + " was logged in!\n";
        } catch (DataAccessException exception) {
            if (exception.getMessage().contains("401")) {
                return "Invalid username or password\n";
            }
            return exception.getMessage();
        }
    }

    public String logout() {
        if (this.state == ChessState.LOGGED_OUT) {
            return "You must be logged in to logout\n";
        }

        try {
            server.logout(authData.authToken());
            this.authData = null;
            this.state = ChessState.LOGGED_OUT;
            return "Logged out successfully\n";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }

    private String register(String username, String password, String email) {
        if (this.state == ChessState.LOGGED_IN) {
            return "Log out to create a register user\n";
        }

        try {
            AuthData user = server.registerUser(username, password, email);
            this.authData = user;
            this.state = ChessState.LOGGED_IN;
            return user.username() + " was registered!\n";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }
}
