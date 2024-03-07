package dataAccess;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class mySQLUser implements UserDataDAO{
    public void createUser(UserData user) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
            try (var statement = conn.prepareStatement(sql)) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String hashedPassword = encoder.encode(user.password());
                statement.setString(1,user.username());
                statement.setString(2,hashedPassword);
                statement.setString(3,user.email());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public UserData readUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM user WHERE username = ?";
            try (var statement = conn.prepareStatement(sql)) {
                statement.setString(1,username);
                ResultSet rs = statement.executeQuery();
                if(rs.next()){
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void deleteAllUsers() throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM user";
            try (var statement = conn.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}