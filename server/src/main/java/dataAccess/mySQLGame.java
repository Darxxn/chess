package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;

public class mySQLGame implements GameDataDAO {
    public void createGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String create = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
            try (var statement = conn.prepareStatement(create)) {
                String json = new Gson().toJson(game.game());
                statement.setInt(1, game.gameID());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, game.gameName());
                statement.setString(5, json);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAllGameData() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM game";
            try (var statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteGame(Integer gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM game WHERE gameID = ?";
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1,gameID.toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public GameData readGame(Integer gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM game WHERE gameID = ?";
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1,gameID.toString());
                ResultSet rs = statement.executeQuery();
                if(rs.next()){
                    ChessGame chessGame = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                    return new GameData(
                            Integer.parseInt(rs.getString("gameID")),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            chessGame
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public ArrayList<GameData> allGames() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM game";
            try (var statement = conn.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();
                ArrayList<GameData> gameList = new ArrayList<>();
                while(rs.next()){
                    ChessGame chessGame = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                    gameList.add(new GameData(
                            Integer.parseInt(rs.getString("gameID")),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            chessGame
                    ));
                }
                if(!gameList.isEmpty()){
                    return gameList;
                }
                return new ArrayList<>();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
