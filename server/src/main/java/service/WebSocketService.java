package service;

import chess.ChessMove;
import dataAccess.*;
import model.*;

public class WebSocketService {
    private final WebSocketAccess dataAccess = new WebSocketAccess();

    public GameData makeMove(int gameID, String authToken, ChessMove move) throws DataAccessException {
        if (gameID <= 0 || authToken == null || authToken.isEmpty() || move == null){
            throw new DataAccessException("Error: Bad Request");
        }
        return dataAccess.makeMove(gameID, authToken, move);
    }

    public String leaveGame(int gameID, String authToken) throws DataAccessException {
        if (gameID <= 0 || authToken == null || authToken.isEmpty()){
            throw new DataAccessException("Error: Bad Request");
        }
        return dataAccess.leaveGame(gameID, authToken);
    }

    public String resignGame(int gameID, String authToken) throws DataAccessException {
        if (gameID <= 0 || authToken == null || authToken.isEmpty()){
            throw new DataAccessException("Error: Bad Request");
        }
        return dataAccess.resignGame(gameID, authToken);
    }

    public GameData getGameData(int gameID, String authToken) throws DataAccessException {
        if (gameID <= 0 || authToken == null || authToken.isEmpty()){
            throw new DataAccessException("Error: Bad Request");
        }
        return dataAccess.getGameData(gameID, authToken);
    }

    public String getUsernameFromAuthToken(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()){
            throw new DataAccessException("Error: Bad Request");
        }
        return dataAccess.getUsernameFromAuthToken(authToken);
    }
}
