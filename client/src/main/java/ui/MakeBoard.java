package ui;

import java.util.*;
import java.util.stream.Collectors;

import chess.*;
//import dataAccess.DataAccessException;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;

public class MakeBoard implements GameHandler {
    private GameData gameData;
    private String url;
    private AuthData authData;
    private String color;
    private Boolean isObserver = false;
    private boolean isRunning = true;
    private ChessPosition highlightPosition = null;
    private Collection<ChessPosition> highlightMoves = null;
    private WebSocketFacade webSocketFacade;
    private Scanner scanner = new Scanner(System.in);

    public MakeBoard(GameData gameData, String givenColor, String url, AuthData authData) throws DataAccessException {
        this.gameData = gameData;
        this.url = url;
        this.authData = authData;
        if (givenColor != null) {
            this.color = givenColor;
            this.isObserver = false;
        } else {
            this.isObserver = true;
        }
        this.webSocketFacade = new WebSocketFacade(url, this);
    }

    public void startGame() {
        if (isObserver) {
            webSocketFacade.joinObserver(this.authData.authToken(), this.gameData.gameID(), this.authData.username());
        } else {
            webSocketFacade.joinPlayer(this.authData.authToken(), this.gameData.gameID(), this.authData.username(), this.convertTeamColor());
        }

        System.out.println(EscapeSequences.SET_TEXT_BOLD + "Welcome to chess\n" + EscapeSequences.RESET_TEXT_BOLD_FAINT + "(Type 'help' for a list of commands or 'quit' to exit the program.)");
        while (isRunning) {
            var color = isObserver ? "\n" + "Observer\n\n" : "\n" + this.color + "\n\n" ;
            System.out.print(EscapeSequences.SET_TEXT_BOLD + color + "[IN GAME] >>>> \n" + EscapeSequences.RESET_TEXT_BOLD_FAINT);
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
            case "resign" -> resign();
            case "highlight", "highlight legal moves" -> params.length != 1 ? "Invalid highlight command. Highlight by: highlight <position>." : highlightLegalMoves(params[0]);
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
                    "highlight - Highlight all legal moves for a piece. Highlight by: highlight <position>.\n";
        }
    }

    private String leaveGame()
    {
        webSocketFacade.leaveGame(this.authData.authToken(), this.gameData.gameID());
        isRunning = false;
        return "You have left the game.\n";
    }

    private String makeMove(String move){
        if (this.gameData.game().getTeamTurn() != convertTeamColor()) {
            return "It is not your turn.";
        }

        ChessPiece.PieceType promotion = null;
        var moveParts = move.split(" ");
        var from = moveParts[1];
        var to = moveParts[2];
        if (!from.matches("[a-h][1-8]") || !to.matches("[a-h][1-8]")) {
            return "Invalid move.\nPlease enter a move in the format 'move <from> <to>' where <from> and <to> are positions on the board in the format 'a1' to 'h8'.";
        }

        var fromMove = convertToLegalPosition(from);
        var toMove = convertToLegalPosition(to);
        var validMoves = gameData.game().validMoves(fromMove);
        if (validMoves == null || !validMoves.contains(new ChessMove(fromMove, toMove, null))) {

            return "Invalid move.\nPlease enter a move in the format 'move <from> <to>' where <from> and <to> are positions on the board in the format 'a1' to 'h8'.";
        }
        if (gameData.game().getBoard().getPiece(fromMove).getPieceType() == ChessPiece.PieceType.PAWN && (toMove.getRow() == 0 || toMove.getRow() == 7)) {
            System.out.println("What would you like to promote to? (queen, rook, bishop, knight)");
            try (Scanner scanner = new Scanner(System.in)) {
                var promotionInput = scanner.nextLine();
                switch (promotionInput) {
                    case "queen" -> promotion = ChessPiece.PieceType.QUEEN;
                    case "rook" -> promotion = ChessPiece.PieceType.ROOK;
                    case "bishop" -> promotion = ChessPiece.PieceType.BISHOP;
                    case "knight" -> promotion = ChessPiece.PieceType.KNIGHT;
                    default -> {
                        return "Invalid promotion. Please try again.";
                    }
                }
            }
        }
        var chessMove = new chess.ChessMove(fromMove, toMove, promotion);
        webSocketFacade.makeMove(this.authData.authToken(), this.gameData.gameID(), chessMove);
        return "You have made a move, " + from + " " + to + "." + "\n";
    }

    private String resign(){
        if(isObserver) return "Observers cannot resign. Please leave the game instead.";
        if (this.gameData.game().getTeamTurn() == ChessGame.TeamColor.FINISHED) {
            return "The game has already ended.";
        }
        if ((Objects.equals(this.color, "white") && this.gameData.blackUsername() == null ) || (Objects.equals(this.color, "black") && this.gameData.whiteUsername() == null)){
            return "The other player has already resigned or left the game.";
        }
        System.out.println("Are you sure you want to resign? (yes/no)");
        var input = scanner.nextLine();
        if (!input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("y")) {
            return "Resignation cancelled.";
        } else {
            resignGame();
        }
        return "You have resigned the game.";
    }

    private void resignGame(){
        isRunning = false;
        webSocketFacade.resignGame(this.authData.authToken(), this.gameData.gameID());
    }

    private String quit(){
        isRunning = false;
        if (this.gameData != null) {
            webSocketFacade.leaveGame(this.authData.authToken(), this.gameData.gameID());
        }
        webSocketFacade.onClose();
        return "Goodbye!\n";
    }

    public String displayGame(String color) {
        if (this.gameData == null) {
            return "No game data available.";
        }

        var gameInfo = this.gameData.game();
        var turn = gameInfo.getTeamTurn();

        var output = "\nGame:\n";

        output += displayBoard();
        return output + "\n" + "It is " + turn + "'s turn.\n";
    }

    private String displayBoard(){
        var gameInfo = this.gameData.game();
        var board = gameInfo.getBoard();
        var output = "";

        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet() + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        for (int i = 7; i >= 0; i--) {
            output += EscapeSequences.SET_TEXT_BOLD + (i + 1) + EscapeSequences.RESET_TEXT_BOLD_FAINT + " ";
            for (int j = 7; j >= 0; j--) {
                var piece = board.getPiece(new ChessPosition(i + 1, j + 1));
                if (highlightPosition != null && highlightPosition.getRow() == i && highlightPosition.getColumn() == j) {
                    output += EscapeSequences.SET_BG_COLOR_YELLOW + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY;
                } else if (highlightMoves != null && highlightMoves.contains(new ChessPosition(i + 1, j + 1))) {
                    output += EscapeSequences.SET_BG_COLOR_GREEN + returnPieceChar(piece) + EscapeSequences.SET_BG_COLOR_DARK_GREY;
                } else {
                    output += (i + j) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_DARK_GREY + returnPieceChar(piece) : EscapeSequences.SET_BG_COLOR_BLUE + returnPieceChar(piece);
                }
            }
            output += EscapeSequences.SET_BG_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BOLD + (i + 1) + EscapeSequences.RESET_TEXT_BOLD_FAINT + "\n";
        }
        output += EscapeSequences.SET_TEXT_BOLD + displayAlphabet() + EscapeSequences.RESET_TEXT_BOLD_FAINT + EscapeSequences.SET_BG_COLOR_DARK_GREY;
        return output;
    }

    private String returnPieceChar(ChessPiece piece){
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        if (Objects.equals(piece.getTeamColor().toString(), "WHITE")) {
            return switch (piece.getPieceType()) {
                case PAWN ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_PAWN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case ROOK ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KNIGHT ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case BISHOP ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case QUEEN ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KING ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;
                default ->
                        EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.EMPTY + EscapeSequences.SET_TEXT_COLOR_WHITE;
            };
        } else {
            return switch (piece.getPieceType()) {
                case PAWN ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_PAWN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case ROOK ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KNIGHT ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case BISHOP ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case QUEEN ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                case KING ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;
                default ->
                        EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.EMPTY + EscapeSequences.SET_TEXT_COLOR_WHITE;
            };
        }
    }

    private String displayAlphabet(){
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

    private ChessPosition convertToLegalPosition(String input) {
        char columnChar = input.charAt(0);
        int row = Character.getNumericValue(input.charAt(1));
        int column = 8 - (columnChar - 'a');
        return new ChessPosition(row, column);
    }

    private String highlightLegalMoves(String position){
        if (this.gameData.game().getTeamTurn() != convertTeamColor()) {
            return "It is not your turn.";
        }
        var pos = convertToLegalPosition(position);
        if (pos == null) {
            return "Invalid position. Please try again.";
        }
        var validMoves = gameData.game().validMoves(pos);
        if (validMoves == null) {
            return "Invalid position. Please try again.";
        }
        var output = "Legal moves for " + position + ": ";
        this.highlightPosition = pos;
        this.highlightMoves = validMoves.stream().map(move -> move.getEndPosition()).collect(Collectors.toList());
        output = displayBoard();
        this.highlightPosition = null;
        this.highlightMoves = null;
        return output;
    }

    @Override
    public void updateGame(ChessGame game, String whiteUsername, String blackUsername) {
        var playerColor = Boolean.TRUE.equals(isObserver) ? "Observer" : this.color;
        this.gameData = new GameData(this.gameData.gameID(), this.gameData.whiteUsername(), this.gameData.blackUsername(), this.gameData.gameName(), game);
        System.out.println("\n" + displayGame(this.color) + "\n");
        System.out.print(EscapeSequences.SET_TEXT_BOLD + playerColor + " >>>> " + EscapeSequences.RESET_TEXT_BOLD_FAINT);

        //Determine and display if the game is in Checkmate, Check, or Stalemate
        if (game.isInCheckmate(game.getTeamTurn())) {
            printMessage("King is in Checkmate! Game is over.");
        }

        else if (game.isInCheck(game.getTeamTurn())) {
            printMessage("King is in Check!");
        }

        else if (game.isInStalemate(game.getTeamTurn())) {
            printMessage("Game is in Stalemate!");
            printMessage("Game is over.");
        }
    }

    @Override
    public void printMessage(String message) {
        System.out.println("\n INCOMING MESSAGE >>>> " + message);
    }
}