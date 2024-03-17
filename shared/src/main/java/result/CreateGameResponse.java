package result;

import chess.ChessGame;

public record CreateGameResponse(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
