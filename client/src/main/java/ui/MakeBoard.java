package ui;

import java.util.Arrays;
import java.util.Scanner;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class MakeBoard implements GameHandler {
    private GameData gameData;
    private String url;
    private AuthData authData;
    private String color;
    private Boolean isObserver = false;
    private boolean isRunning = true;
    private ChessBoard board;

    public MakeBoard(GameData gameData, String givenColor) throws DataAccessException {
        this.board = new ChessBoard();
        board.resetBoard();
        if (givenColor != null) {
            this.color = givenColor;
            this.isObserver = false;
        } else {
            this.color = "white";
            this.isObserver = true;
        }
    }

    public void startGame() {
        Scanner scanner = new Scanner(System.in);

        System.out.println(EscapeSequences.SET_TEXT_BOLD + "Welcome to the chess\n" + EscapeSequences.RESET_TEXT_BOLD_FAINT + "(Type 'help' for a list of commands or 'quit' to exit the program.)");
        System.out.print(displayGame(this.color));
        while (isRunning) {
            var color = isObserver ? "\n" + "Observer\n\n" : "\n" + this.color + "\n\n" ;
            System.out.print(EscapeSequences.SET_TEXT_BOLD + color + "[IN GAME] >>>> " + EscapeSequences.RESET_TEXT_BOLD_FAINT);
            String input = scanner.nextLine();
            String output = inputParser(input);
            System.out.println(output);
        }
    }

    public String inputParser(String input){
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> quit();
            case "help" -> help();
            case "redraw", "redrawing chess board \n" -> displayGame(this.color);
            case "leave" -> leaveGame();
            case "move" -> params.length != 2 ? "Invalid move command. Usage: move <from> <to>." : makeMove(input);
            case "resign" -> "You have resigned the game.";
            case "highlight", "highlight legal moves" -> "You have highlighted the legal moves.";
            default -> help();
        };
    }

    private String help(){
        if (isObserver) {
            return "Available commands:\n" +
                    "help - Display this help message.\n" +
                    "quit - Exit the program.\n" +
                    "Redraw Chess Board or Redraw - Redraw the chess board.\n" +
                    "Leave - Leave the game.\n";
        } else {
            return "Available commands:\n" +
                    "help - Display this help message.\n" +
                    "quit - Exit the program.\n" +
                    "redraw - Redraw the chess board.\n" +
                    "leave - Leave the game.\n" +
                    "move - Make a move. Usage: move <from> <to>.\n" +
                    "resign - Resign the game.\n" +
                    "highlight - Highlight all legal moves for a piece. Usage: highlight legal moves <position>.\n";
        }
    }

    private String leaveGame()
    {
        isRunning = false;
        return "You have left the game.\n";
    }

    private String makeMove(String move){
        return "You have made a move.\n";
    }

    private String quit(){
        isRunning = false;
        return "Goodbye!\n";
    }



    public String displayGame(String color) {
        var output = "\nGame:\n";

        if (color.equalsIgnoreCase("white")) {
            output += displayBoard();
            output += "\n--------------------------------\n";
            output += displayBoardInverted();
        } else {
            output += displayBoardInverted();
            output += "\n--------------------------------\n";
            output += displayBoard();
        }
        return output + "\n";
    }

    private String displayBoard(){
        var output = "";

        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(false) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        for (int i = 0; i < 8; i++) {
            output += EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + " ";
            for (int j = 0; j < 8; j++) {
                var piece = board.getPiece(new ChessPosition(i + 1, j + 1));
                output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_DARK_GREY + returnPieceChar(piece) : EscapeSequences.SET_BG_COLOR_BLUE + returnPieceChar(piece);
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        }
        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(false) + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_BG_COLOR_DARK_GREY;
        return output;
    }

    private String displayBoardInverted(){
        var output = "";

        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(true) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        for (int i = 7; i >= 0; i--) {
            output += EscapeSequences.SET_TEXT_BOLD + (8 - i) + EscapeSequences.RESET_TEXT_BOLD_FAINT + " ";
            for (int j = 7; j >= 0; j--) {
                var piece = board.getPiece(new ChessPosition(i + 1, j + 1));
                output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_DARK_GREY + returnPieceChar(piece) : EscapeSequences.SET_BG_COLOR_LIGHT_GREY + returnPieceChar(piece);
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (i + 1) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        }
        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet(true) + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_BG_COLOR_DARK_GREY;
        return output;
    }

    private String returnPieceChar(ChessPiece piece){
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        if (piece.getTeamColor().toString() == "WHITE") {
            switch (piece.getPieceType()) {
                case PAWN:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_PAWN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case ROOK:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KNIGHT:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case BISHOP:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case QUEEN:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KING:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;
                default:
                    return EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.EMPTY + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }
        } else {
            switch (piece.getPieceType()) {
                case PAWN:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_PAWN  + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case ROOK:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KNIGHT:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case BISHOP:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case QUEEN:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KING:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;
                default:
                    return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.EMPTY + EscapeSequences.SET_TEXT_COLOR_WHITE;
            }
        }
    }

    private String displayAlphabet(Boolean inverted){
        if (inverted) {
            return "  \u2003h\u2003 g\u2003 f\u2003 e\u2003 d\u2003 c\u2003 b\u2003 a";
        }
        return "  \u2003a\u2003 b\u2003 c\u2003 d\u2003 e\u2003 f\u2003 g\u2003 h";
    }

    private ChessGame.TeamColor convertTeamColor(){
        if (this.color == null) {
            return null;
        }

        if (this.color.equalsIgnoreCase("white")) {
            return ChessGame.TeamColor.WHITE;
        } else {
            return ChessGame.TeamColor.BLACK;
        }
    }

    @Override
    public void updateGame(ChessGame game, String whiteUsername, String blackUsername) {
        var playerColor = Boolean.TRUE.equals(isObserver) ? "Observer" : this.color;
        //redraw the game with the new game data
        this.gameData = new GameData(this.gameData.gameID(), this.gameData.whiteUsername(), this.gameData.blackUsername(), this.gameData.gameName(), game);
        System.out.println("\n" + displayGame(this.color) + "\n");
        System.out.print(EscapeSequences.SET_TEXT_BOLD + playerColor + " >>>> " + EscapeSequences.RESET_TEXT_BOLD_FAINT);
    }

    @Override
    public void printMessage(String message) {
        System.out.println("\n INCOMING MESSAGE >>>> " + message);
    }
}