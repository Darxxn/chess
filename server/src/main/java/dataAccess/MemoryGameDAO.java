package dataAccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryGameDAO {
    private final HashMap<Integer, GameData> gameDataHashMap = new HashMap<>();

    public void deleteAllGameData() {
        gameDataHashMap.clear();
    }

    public void createGame(GameData game) {
        gameDataHashMap.put(game.gameID(), game);
    }

    public GameData readGame(Integer gameID) {
        return gameDataHashMap.get(gameID);
    }

    public ArrayList<GameData> allGames() {
        return new ArrayList<>(gameDataHashMap.values());
    }

    public void deleteGame(Integer gameID) {
        gameDataHashMap.remove(gameID);
    }
}
