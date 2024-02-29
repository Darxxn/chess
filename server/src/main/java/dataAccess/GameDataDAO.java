package dataAccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDataDAO {
    void createGame(GameData game);
    GameData readGame(Integer gameID);
    void deleteGame(Integer gameID);
    void deleteAllGames();
}
