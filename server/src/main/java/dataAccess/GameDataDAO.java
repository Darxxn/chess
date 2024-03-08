package dataAccess;

import model.GameData;
import java.util.ArrayList;


public interface GameDataDAO {
    void createGame(GameData game) throws DataAccessException;
    GameData readGame(Integer gameID) throws DataAccessException;
    void deleteGame(Integer gameID) throws DataAccessException;
    void deleteAllGameData() throws DataAccessException;
    ArrayList<GameData> allGames() throws DataAccessException;
}
