package dataAccess;

import java.sql.*;
import com.google.gson.Gson;
import chess.*;
import model.*;

public class WebSocketAccess implements WebSocketDAO {
    public GameData makeMove(int gameID, String authToken, ChessMove move) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM game WHERE gameID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                String whiteUsername = resultSet.getString("whiteUsername");
                String blackUsername = resultSet.getString("blackUsername");
                String username = getAuth(authToken).username();
                if (!username.equals(whiteUsername) && !username.equals(blackUsername)) {
                    throw new DataAccessException("Unauthorized, Observer cannot make move");
                }
                ChessGame game = convertJsonToChessGame(resultSet.getString("game"));
                try {
                    if (game.getTeamTurn() == ChessGame.TeamColor.FINISHED) {
                        throw new DataAccessException("Bad Request, game is over");
                    }
                    String pieceColor = game.getBoard().getPiece(move.getStartPosition()).getTeamColor().toString();
                    if (pieceColor.equals("WHITE") && !username.equals(whiteUsername)) {
                        throw new DataAccessException("Unauthorized, invalid color");
                    } else if (pieceColor.equals("BLACK") && !username.equals(blackUsername)) {
                        throw new DataAccessException("Unauthorized, invalid color");
                    }
                    game.makeMove(move);
                } catch (InvalidMoveException e) {
                    throw new DataAccessException("Bad Request, invalid move");
                }
                String gameJson = new Gson().toJson(game);
                String sql2 = "UPDATE game SET game = ? WHERE gameID = ?";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setString(1, gameJson);
                stmt2.setInt(2, gameID);
                stmt2.executeUpdate();
            }
            return new GameData(resultSet.getInt("gameID"), resultSet.getString("whiteUsername"), resultSet.getString("blackUsername"), resultSet.getString("gameName"), convertJsonToChessGame(resultSet.getString("game")));
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Internal Server Error");
        }
    }

    public String leaveGame(int gameID, String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM game WHERE gameID = ? AND blackUsername = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            var username = getAuth(authToken).username();
            stmt.setInt(1, gameID);
            stmt.setString(2, username);
            stmt.executeUpdate();
            String sql2 = "SELECT * FROM game WHERE gameID = ?";
            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            stmt2.setInt(1, gameID);
            ResultSet resultSet = stmt2.executeQuery();
            if (resultSet.next()) {
                String whiteUsername = resultSet.getString("whiteUsername");
                String blackUsername = resultSet.getString("blackUsername");
                if (whiteUsername != null && whiteUsername.equals(username)) {
                    whiteUsername = null;
                } else if (blackUsername != null && blackUsername.equals(username)) {
                    blackUsername = null;
                }
                String sql3 = "UPDATE game SET whiteUsername = ?, blackUsername = ? WHERE gameID = ?";
                PreparedStatement stmt3 = conn.prepareStatement(sql3);
                stmt3.setString(1, whiteUsername);
                stmt3.setString(2, blackUsername);
                stmt3.setInt(3, gameID);
                stmt3.executeUpdate();
            }
            return username;
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: Internal Server Error");
        }
    }

    public String resignGame(int gameID, String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM game WHERE gameID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                ChessGame game = convertJsonToChessGame(resultSet.getString("game"));
                if (game.getTeamTurn() == ChessGame.TeamColor.FINISHED) {
                    throw new DataAccessException("Bad Request, game is over");
                }
                game.setTeamTurn(ChessGame.TeamColor.FINISHED);
                String whiteUsername = resultSet.getString("whiteUsername");
                String blackUsername = resultSet.getString("blackUsername");
                if (whiteUsername.equals(getAuth(authToken).username())) {
                    whiteUsername = null;
                } else if (blackUsername.equals(getAuth(authToken).username())) {
                    blackUsername = null;
                } else {
                    throw new DataAccessException("Unauthorized, not a player in the game");
                }
                String sql2 = "UPDATE game SET whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID = ?";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setString(1, whiteUsername);
                stmt2.setString(2, blackUsername);
                stmt2.setString(3, new Gson().toJson(game));
                stmt2.setInt(4, gameID);
                stmt2.executeUpdate();
            }
            return getAuth(authToken).username();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: Internal Server Error");
        }
    }

    public GameData getGameData(int gameID, String authToken) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM game WHERE gameID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) { return createGameDataFromResultSet(resultSet); }
            return null;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsernameFromAuthToken(String authToken) {
        try {
            return getAuth(authToken).username();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthData getAuth(String authToken) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM auth WHERE authToken = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, authToken);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return new AuthData(resultSet.getString("username"), resultSet.getString("authToken"));
            } else {
                return null;
            }
        }
    }

    private ChessGame convertJsonToChessGame(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ChessGame.class);
    }

    private GameData createGameDataFromResultSet(ResultSet resultSet) throws SQLException {
        return new GameData( resultSet.getInt("gameID"), resultSet.getString("whiteUsername"), resultSet.getString("blackUsername"), resultSet.getString("gameName"),  convertJsonToChessGame(resultSet.getString("game")));
    }
}
