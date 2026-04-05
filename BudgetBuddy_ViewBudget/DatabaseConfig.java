import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL = "jdbc:mysql://localhost:3306/budgetbuddyproject";
    private static final String USER = "root";       // your MySQL username
    private static final String PASSWORD = "";       // your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}