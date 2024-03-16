package ui;

import java.util.Arrays;
import model.*;
import chess.ChessGame;
import dataAccess.*;
import org.xml.sax.ErrorHandler;

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

    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "quit" -> quit();
//                case "login" -> login();
//                case "register" -> register();
//                case "list" -> listGames();
//                case "create" -> createGame();
//                case "join" -> joinGame();
//                case "observe" -> obsGame();
                case "help" -> help();
                default -> help();
            };
        }
        catch (DataAccessException ex) {
         return ex.getMessage();
        }
    }

    public String help() {
        return "";
    }

    public String quit() {
        return "";
    }


}
