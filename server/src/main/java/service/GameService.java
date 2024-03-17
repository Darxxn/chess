package service;

import dataAccess.*;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResponse;
import dataAccess.DataAccessException;
import java.util.Random;
import chess.ChessGame;
import model.GameData;
import result.ListGamesResponse;

public class GameService {
    private final mySQLGame gameDAO = new mySQLGame();

    public void clear() throws DataAccessException {
        gameDAO.deleteAllGameData();
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        if(request.gameName() == null){
            throw new DataAccessException("Error: bad request");
        }
        Random random = new Random();
        int gameID = Math.abs(random.nextInt());
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(gameID,null,null, request.gameName(),game);
        gameDAO.createGame(gameData);
        return new CreateGameResponse(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
    }

    public ListGamesResponse listGames() throws DataAccessException{
        return new ListGamesResponse(gameDAO.allGames());
    }

    public void joinGame(JoinGameRequest joinRequest, String username) throws DataAccessException {
        if (gameDAO.readGame(joinRequest.gameID()) == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (joinRequest.playerColor() != null) {
            int gameID = joinRequest.gameID();
            GameData previousGame = gameDAO.readGame(gameID);

            if (joinRequest.playerColor().equals("WHITE") && previousGame.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }

            if (joinRequest.playerColor().equals("BLACK") && previousGame.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }

            String playerColor = joinRequest.playerColor();
            String whiteUser = playerColor.equals("WHITE") ? username : previousGame.whiteUsername();
            String blackUser = playerColor.equals("BLACK") ? username : previousGame.blackUsername();
            String gameName = previousGame.gameName();
            ChessGame game = previousGame.game();
            GameData newGame = new GameData(gameID, whiteUser, blackUser, gameName, game);
            gameDAO.deleteGame(gameID);
            gameDAO.createGame(newGame);
        }
    }
}
