package ui;

import java.util.Arrays;
import java.util.Scanner;
import model.*;
import chess.ChessGame;
import dataAccess.*;

public class ChessClient {

    private ChessState state = ChessState.LOGGEDOUT;
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
        System.out.println("Type Help to get started");

        while (serverLive) {
            System.out.print(EscapeSequences.SET_TEXT_BOLD);
            System.out.println("[" + this.state + "]" + " >>>");
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
        return "";
    }

    public String quit() {
        if (this.state == ChessState.LOGGEDIN) {
            this.logout();
        }
        this.state = ChessState.LOGGEDOUT;
        this.serverLive = false;
        return "Chess client terminated.";
    }

    public String logout() {
        return "";
    }

    private void assertLoggedIn() throws DataAccessException {
        if (state == ChessState.LOGGEDOUT) {
            throw new DataAccessException("You must sign in");
        }
    }
}
