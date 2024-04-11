package dataAccess;

import chess.ChessMove;
import model.GameData;

public interface WebSocketDAO {
    String getUsernameFromAuthToken(String authToken);
    String leaveGame(int gameID, String authToken) throws DataAccessException;
    String resignGame(int gameID, String authToken) throws DataAccessException;
    GameData makeMove(int gameID, String authToken, ChessMove move) throws DataAccessException;
    GameData getGameData(int gameID, String authToken);
}
