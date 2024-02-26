package dataAccess;

public interface MemoryUserDAO {
    String createUser(String username, String password, String email);
}