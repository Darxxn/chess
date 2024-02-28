package service;

import dataAccess.MemoryGameDAO;
import request.CreateGameRequest;
import result.CreateGameResponse;
import dataAccess.DataAccessException;
import java.util.Random;
import chess.ChessGame;
import model.GameData;
import result.ListGamesResponse;

public class GameService {

    private final MemoryGameDAO gameDAO = new MemoryGameDAO();

    public void clear() {
        gameDAO.deleteAllGameData();
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        if(request.gameName() == null){
            throw new DataAccessException("bad request");
        }
        Random random = new Random();
        int gameID = Math.abs(random.nextInt());
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(gameID,null,null, request.gameName(),game);
        gameDAO.createGame(gameData);
        return  new CreateGameResponse(gameID);
    }

    public ListGamesResponse listGames() {
        return new ListGamesResponse(gameDAO.allGames());
    }

}
