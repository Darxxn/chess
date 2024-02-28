package dataAccess;

import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO {
    private final HashMap<Integer, GameData> gameDataHashMap = new HashMap<>();

    public void deleteAllGameData() {
        gameDataHashMap.clear();
    }

    public void createGame(GameData game) {
        gameDataHashMap.put(game.gameID(), game);
    }
}
