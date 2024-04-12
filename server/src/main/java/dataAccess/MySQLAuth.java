package dataAccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLAuth implements AuthDataDAO {
    public String createAuth(String username) throws DataAccessException {
        UUID uuid = UUID.randomUUID();
        try (Connection conn = DatabaseManager.getConnection()) {
            String create = "INSERT INTO auth (username,authToken) VALUES (?, ?)";
            try (var statement = conn.prepareStatement(create)) {
                statement.setString(1, username);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
                return uuid.toString();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String delete = "DELETE FROM auth WHERE authToken = ?";
            try (var statement = conn.prepareStatement(delete)) {
                statement.setString(1, authToken);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public AuthData readAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String read = "SELECT * FROM auth WHERE authToken = ?";
            try (var statement = conn.prepareStatement(read)) {
                statement.setString(1, authToken);
                ResultSet rs = statement.executeQuery();
                if(rs.next()){
                    return new AuthData(
                            rs.getString("username"),
                            rs.getString("authToken")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAllAuth() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String deleteAll = "DELETE FROM auth";
            try (var statement = conn.prepareStatement(deleteAll)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
