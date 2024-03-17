package ui;

import java.util.Arrays;
import java.util.Scanner;
import model.*;
import chess.ChessGame;
import dataAccess.*;

import javax.sound.midi.SysexMessage;

public class ChessClient {

    private ChessState state = ChessState.LOGGED_OUT;
    private ChessServer server;
    private String url;
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
/*
                case "login" -> login();
                case "register" -> register();
                case "list" -> listGames();
                case "create" -> createGame();
                case "join" -> joinGame();
                case "observe" -> obsGame();
*/
                case "help" -> help();
                default -> help();
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

    public String quit() {
        if (this.state == ChessState.LOGGED_IN) {
            this.logout();
        }
        this.state = ChessState.LOGGED_OUT;
        this.serverLive = false;
        return "Chess client terminated.";
    }

    public String logout() {
        return "";
    }

    private void assertLoggedIn() throws DataAccessException {
        if (state == ChessState.LOGGED_OUT) {
            throw new DataAccessException("You must sign in");
        }
    }
}
