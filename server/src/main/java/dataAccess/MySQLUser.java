package dataAccess;

import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUser implements UserDataDAO {
    public void createUser(UserData user) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String create = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
            try (var statement = conn.prepareStatement(create)) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String hashedPassword = encoder.encode(user.password());
                statement.setString(1, user.username());
                statement.setString(2, hashedPassword);
                statement.setString(3, user.email());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public UserData readUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String read = "SELECT * FROM user WHERE username = ?";
            try (var statement = conn.prepareStatement(read)) {
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

    public void deleteAllUsers() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String delete = "DELETE FROM user";
            try (var statement = conn.prepareStatement(delete)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
