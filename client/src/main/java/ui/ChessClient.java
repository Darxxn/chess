package ui;

import java.util.*;

import model.*;
import dataAccess.*;


public class ChessClient {

    public AuthData authData;
    public ChessState state = ChessState.LOGGED_OUT;
    public ServerFacade serverFacade;
    public String url;
    public GameData gameData;
    public boolean serverLive = true;

    public ChessClient() {
        serverFacade = new ServerFacade("http://localhost:8080");
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
        System.out.println("    Welcome to Chess!");
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
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> quit();
            case "logout" -> logout();
            case "login" -> params.length < 2 ? (EscapeSequences.SET_TEXT_COLOR_YELLOW + "Missing login information.\n") + EscapeSequences.SET_TEXT_COLOR_WHITE : login(params[0], params[1]);
            case "register" -> params.length < 3 ? (EscapeSequences.SET_TEXT_COLOR_YELLOW + "Provide information to register.\n") + EscapeSequences.SET_TEXT_COLOR_WHITE : register(params[0], params[1], params[2]);
            case "list" -> listGames();
            case "create" -> params.length< 1 ? (EscapeSequences.SET_TEXT_COLOR_YELLOW + "Provide a game name\n") + EscapeSequences.SET_TEXT_COLOR_WHITE : createGame(params[0]);
            case "join" -> params.length < 1 ? "Please provide a gameID\n" : params.length == 2 ? joinGame(params[0], params[1]) : joinGame(params[0], "");
            case "observe" -> obsGame(params[0]);
            case "help" -> help();
            default -> "Provide a correct command\n";
        };
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

    public String joinGame(String gameIndex, String color) {
        int gameID = 0;
        int index = 0;

        if (this.state == ChessState.LOGGED_OUT) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must login to join a game\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        if (!color.equalsIgnoreCase("white") && !color.equalsIgnoreCase("black") && !color.equalsIgnoreCase("")) {
            return "Choose a team: 'white', 'black', or leave blank :)\n";
        }

        try {
            index = Integer.parseInt(gameIndex);
        } catch (NumberFormatException exception) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game ID.\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        try {
            var games = serverFacade.listGames(authData.authToken());
            List<GameData> listOfGames = new ArrayList<>(games.games());

            if (index <= 0 || index > listOfGames.size()) {
                return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game ID.\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }

            // find the gameID based on the gameIndex
            gameID = listOfGames.get(index - 1).gameID();
            this.gameData = serverFacade.joinGame(authData.authToken(), gameID, color);
            new MakeBoard(this.gameData, color).startGame();
            return "";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }

    public String obsGame(String gameIndex) {
        int gameID = 0;
        int index = 0;

        if(this.state == ChessState.LOGGED_OUT){
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to observe a game.\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try{
            index = Integer.parseInt(gameIndex);
        } catch (NumberFormatException ex) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game ID.\n"+ EscapeSequences.SET_TEXT_COLOR_WHITE;
        }
        try {
            var listGames = serverFacade.listGames(authData.authToken());
            List<GameData> listOfGames = new ArrayList<>(listGames.games());

            if (index <= 0 || index > listOfGames.size()) {
                return EscapeSequences.SET_TEXT_COLOR_RED + "Invalid game ID.\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }

            gameID = listOfGames.get(index - 1).gameID();
            this.gameData = serverFacade.joinGame(authData.authToken(), gameID, null);
            new MakeBoard(this.gameData, null).startGame();
            return "";
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
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must logout first\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        try {
            AuthData user = serverFacade.login(username, password);
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
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to logout\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        try {
            serverFacade.logout(authData.authToken());
            this.authData = null;
            this.state = ChessState.LOGGED_OUT;
            return "Logged out successfully\n";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }

    private String register(String username, String password, String email) {
        if (this.state == ChessState.LOGGED_IN) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "Log out to create a register user\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        try {
            AuthData user = serverFacade.registerUser(username, password, email);
            this.authData = user;
            this.state = ChessState.LOGGED_IN;
            return user.username() + " was registered!\n";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }

    public String createGame(String gameName) {
        if (this.state == ChessState.LOGGED_OUT) {
            return EscapeSequences.SET_TEXT_COLOR_RED +  "You must login to create a game\n" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        try {
            var game = serverFacade.createGame(authData.authToken(), gameName);
            return game.gameName() + " was created!\n";
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }

    public String listGames() {
        if (this.state == ChessState.LOGGED_OUT) {
            return EscapeSequences.SET_TEXT_COLOR_RED + "You must be logged in to view games" + EscapeSequences.SET_TEXT_COLOR_WHITE;
        }

        try {
            var games = serverFacade.listGames(authData.authToken());
            List<GameData> listOfGames = new ArrayList<>(games.games());
            listOfGames.sort(Comparator.comparingInt(GameData::gameID));
            StringBuilder output = new StringBuilder("List of Games:\n");

            if (listOfGames.isEmpty()) {
                output.append("No games right now :(\n");
            }

            for (int i = 0; i < listOfGames.size(); i++) {
                GameData game = listOfGames.get(i);
                output.append(i + 1);
                output.append(EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_WHITE + ". gameName: ");
                output.append(EscapeSequences.SET_TEXT_FAINT + EscapeSequences.SET_TEXT_COLOR_MAGENTA + game.gameName() + ", ");
                output.append(EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_WHITE + " whiteUsername: ");
                output.append(EscapeSequences.SET_TEXT_FAINT + EscapeSequences.SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + ", ");
                output.append(EscapeSequences.SET_TEXT_BOLD + EscapeSequences.SET_TEXT_COLOR_WHITE + " blackUsername: ");
                output.append(EscapeSequences.SET_TEXT_FAINT + EscapeSequences.SET_TEXT_COLOR_MAGENTA + game.blackUsername());
                output.append(EscapeSequences.SET_TEXT_COLOR_WHITE + "\n");
            }

            return output.toString();
        } catch (DataAccessException exception) {
            return exception.getMessage();
        }
    }
}
