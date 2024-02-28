package dataAccess;

import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO {
    private final HashMap<String, GameData> gameDataHashMap = new HashMap<>();

    public void deleteAllGameData() {
        gameDataHashMap.clear();
    }
}
