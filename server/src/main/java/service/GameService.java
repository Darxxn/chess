package service;

import dataAccess.MemoryGameDAO;

public class GameService {

    private final MemoryGameDAO gameDAO = new MemoryGameDAO();

    public void clear() {
        gameDAO.deleteAllGameData();
    }
}
